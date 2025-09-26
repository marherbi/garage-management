package com.renault.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renault.garage.dto.CreateGarageDto;
import com.renault.garage.dto.GarageDto;
import com.renault.garage.service.GarageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GarageController.class)
class GarageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    GarageService garageService;

    private GarageDto garageDto(long id, String name) {
        return new GarageDto(id, name, "addr", "tel", "mail", null);
    }

    @Test
    @DisplayName("GET /garages/{id} -> 200")
    void findById_ok() throws Exception {
        given(garageService.getGarageById(1L)).willReturn(garageDto(1L, "G1"));
        mockMvc.perform(get("/api/v1/garages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("G1"));
    }

    @Test
    @DisplayName("GET /garages/{id} -> 404")
    void findById_notFound() throws Exception {
        given(garageService.getGarageById(1L)).willReturn(null);
        mockMvc.perform(get("/api/v1/garages/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /garages/{id} -> 204")
    void delete_ok() throws Exception {
        given(garageService.deleteGarage(2L)).willReturn(true);
        mockMvc.perform(delete("/api/v1/garages/2"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /garages/{id} -> 404")
    void delete_notFound() throws Exception {
        given(garageService.deleteGarage(2L)).willReturn(false);
        mockMvc.perform(delete("/api/v1/garages/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /garages -> 201")
    void create_created() throws Exception {
        CreateGarageDto payload = new CreateGarageDto("G1", "a", "t", "m", Map.of());
        given(garageService.createGarage(any(CreateGarageDto.class))).willReturn(garageDto(10L, "G1"));
        mockMvc.perform(post("/api/v1/garages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/garages/10"));
    }

    @Test
    @DisplayName("POST /garages -> 400 (null dto)")
    void create_badRequest() throws Exception {
        given(garageService.createGarage(any())).willReturn(null);
        mockMvc.perform(post("/api/v1/garages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /garages/{id} -> 200")
    void update_ok() throws Exception {
        given(garageService.updateGarage(eq(5L), any(CreateGarageDto.class)))
                .willReturn(garageDto(5L, "G5"));
        mockMvc.perform(put("/api/v1/garages/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateGarageDto("G5", null, null, null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @DisplayName("PUT /garages/{id} -> 404")
    void update_notFound() throws Exception {
        given(garageService.updateGarage(eq(5L), any(CreateGarageDto.class)))
                .willReturn(null);
        mockMvc.perform(put("/api/v1/garages/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateGarageDto("G5", null, null, null, null))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /garages -> page 200")
    void listGarages() throws Exception {
        Page<GarageDto> page = new PageImpl<>(List.of(garageDto(1L, "G1"), garageDto(2L, "G2")), PageRequest.of(0, 2), 2);
        given(garageService.getGarages(any())).willReturn(page);
        mockMvc.perform(get("/api/v1/garages?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("GET /garages/byVehicleFuelType/{fuel} -> page 200")
    void listByFuelType() throws Exception {
        Page<GarageDto> page = new PageImpl<>(List.of(garageDto(3L, "G3")));
        given(garageService.getGaragesHavingVehiclesWithFuelType(eq("GASOLINE"), any())).willReturn(page);
        mockMvc.perform(get("/api/v1/garages/byVehicleFuelType/GASOLINE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(3));
    }

    @Test
    @DisplayName("GET /garages/byVehicleFuelType/{fuel} -> empty page 200")
    void listByFuelType_empty() throws Exception {
        Page<GarageDto> page = Page.empty();
        given(garageService.getGaragesHavingVehiclesWithFuelType(eq("UNKNOWN"), any())).willReturn(page);
        mockMvc.perform(get("/api/v1/garages/byVehicleFuelType/UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }
}

