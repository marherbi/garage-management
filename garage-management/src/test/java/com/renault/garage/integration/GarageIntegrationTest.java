package com.renault.garage.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.renault.garage.dao.entity.OpeningTime;
import com.renault.garage.dto.CreateGarageDto;
import com.renault.garage.dto.GarageDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GarageIntegrationTest {

    @Autowired
    TestRestTemplate rest;

    @Autowired
    ObjectMapper mapper;

    @Test
    @DisplayName("Créer un garage puis le récupérer")
    void createAndGetGarage() {
        Map<DayOfWeek, List<OpeningTime>> opening = Map.of(
                DayOfWeek.MONDAY, List.of(new OpeningTime(LocalTime.of(8, 0), LocalTime.of(12, 0)))
        );
        CreateGarageDto payload = new CreateGarageDto("IntGarage", "Addr", "0102", "mail@test", opening);

        ResponseEntity<GarageDto> createdResp = rest.postForEntity("/api/v1/garages", payload, GarageDto.class);
        assertThat(createdResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertNotNull(createdResp.getBody());
        Long id = createdResp.getBody().id();

        ResponseEntity<GarageDto> getResp = rest.getForEntity("/api/v1/garages/" + id, GarageDto.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(getResp.getBody());
        assertThat(getResp.getBody().name()).isEqualTo("IntGarage");
        assertThat(getResp.getBody().openingHours()).containsKey(DayOfWeek.MONDAY);
    }

    @Test
    @DisplayName("Pagination garages par défaut")
    void listGaragesPaged() throws Exception {
        ResponseEntity<String> resp = rest.getForEntity("/api/v1/garages?page=0&size=3", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode tree = mapper.readTree(resp.getBody());
        assertThat(tree.get("size").asInt()).isEqualTo(3);
    }
}

