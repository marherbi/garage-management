package com.renault.garage.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.renault.garage.dto.AccessoryDto;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AccessoryIntegrationTest {

    @Autowired
    TestRestTemplate rest;

    @Autowired
    ObjectMapper mapper;

    private Long createGarage() {
        CreateGarageDto dto = new CreateGarageDto("AccGarage","Addr","0102","mail@x", Collections.emptyMap());
        ResponseEntity<GarageDto> resp = rest.postForEntity("/api/v1/garages", dto, GarageDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertNotNull(resp.getBody());
        return resp.getBody().id();
    }

    private Long createVehicle(Long garageId) {
        Map<String,Object> payload = Map.of(
                "brand","VehAcc",
                "yearOfManufacture",2024,
                "fuelType","GASOLINE"
        );
        ResponseEntity<VehicleDto> resp = rest.postForEntity("/api/v1/garages/"+garageId+"/vehicles", payload, VehicleDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertNotNull(resp.getBody());
        return resp.getBody().id();
    }

    @Test
    @DisplayName("Créer un accessoire et le lister pour le véhicule")
    void createAndListAccessory() throws Exception {
        Long garageId = createGarage();
        Long vehicleId = createVehicle(garageId);

        Map<String,Object> accPayload = Map.of(
                "name","GPS Test",
                "description","Desc",
                "price",123.45,
                "type","NAV"
        );

        ResponseEntity<AccessoryDto> created = rest.postForEntity("/api/v1/vehicles/"+vehicleId+"/accessories", accPayload, AccessoryDto.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertNotNull(created.getBody());
        long accessoryId = created.getBody().id();

        ResponseEntity<AccessoryDto> getResp = rest.getForEntity("/api/v1/accessories/"+accessoryId, AccessoryDto.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(getResp.getBody());
        assertThat(getResp.getBody().name()).isEqualTo("GPS Test");

        ResponseEntity<String> listResp = rest.getForEntity("/api/v1/vehicles/"+vehicleId+"/accessories", String.class);
        assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String,Object>> list = mapper.readValue(listResp.getBody(), new TypeReference<>(){});
        assertThat(list).anySatisfy(m -> assertThat(m.get("name")).isEqualTo("GPS Test"));
    }
}

