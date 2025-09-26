package com.renault.garage.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.renault.garage.dto.CreateGarageDto;
import com.renault.garage.dto.GarageDto;
import com.renault.garage.dto.VehicleDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class VehicleIntegrationTest {

    @Autowired
    TestRestTemplate rest;

    @Autowired
    ObjectMapper mapper;

    private Long createEmptyGarage() {
        CreateGarageDto dto = new CreateGarageDto("CapGarage","Addr","0123","mail@x", Collections.emptyMap());
        ResponseEntity<GarageDto> resp = rest.postForEntity("/api/v1/garages", dto, GarageDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertNotNull(resp.getBody());
        return resp.getBody().id();
    }

    private ResponseEntity<VehicleDto> createVehicle(Long garageId, String brand, int year, String fuelType) {
        Map<String,Object> payload = new HashMap<>();
        payload.put("brand", brand);
        payload.put("yearOfManufacture", year);
        payload.put("fuelType", fuelType);
        return rest.postForEntity("/api/v1/garages/"+garageId+"/vehicles", payload, VehicleDto.class);
    }

    @Test
    @DisplayName("Créer 2 véhicules puis échec à la 3e (capacité=2)")
    void capacityExceededOnThirdVehicle() {
        Long garageId = createEmptyGarage();

        ResponseEntity<VehicleDto> v1 = createVehicle(garageId, "Renault 1", 2024, "GASOLINE");
        ResponseEntity<VehicleDto> v2 = createVehicle(garageId, "Renault 2", 2024, "DIESEL");
        assertThat(v1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(v2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> v3 = rest.postForEntity(
                "/api/v1/garages/"+garageId+"/vehicles",
                Map.of("brand","Renault 3","yearOfManufacture",2024,"fuelType","ELECTRIC"),
                String.class);
        assertThat(v3.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("Lister les véhicules d'un garage après création")
    void listVehiclesInGarage() throws Exception {
        Long garageId = createEmptyGarage();
        createVehicle(garageId, "Car A", 2023, "GASOLINE");
        createVehicle(garageId, "Car B", 2023, "DIESEL");

        ResponseEntity<String> listResp = rest.getForEntity("/api/v1/garage/"+garageId+"/vehicles", String.class);
        assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String,Object>> vehicles = mapper.convertValue(mapper.readTree(listResp.getBody()), new TypeReference<>(){});
        assertThat(vehicles).hasSize(2);
    }

    @Test
    @DisplayName("Recherche par marque")
    void listByBrand() {
        Long garageId = createEmptyGarage();
        createVehicle(garageId, "ModelX", 2024, "GASOLINE");
        ResponseEntity<String> resp = rest.getForEntity("/api/v1/vehicles/byBrand/ModelX", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains("ModelX");
    }
}
