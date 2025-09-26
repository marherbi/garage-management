package com.renault.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renault.garage.dto.AccessoryDto;
import com.renault.garage.dto.CreateAccessoryDto;
import com.renault.garage.service.AccessoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccessoryController.class)
class AccessoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AccessoryService accessoryService;

    @Test
    @DisplayName("POST create accessory -> 201")
    void createAccessory_created() throws Exception {
        CreateAccessoryDto payload = new CreateAccessoryDto("GPS", "desc", 100.0, "NAV");
        given(accessoryService.createAccessory(eq(1L), any(CreateAccessoryDto.class)))
                .willReturn(new AccessoryDto(10L, "GPS", "desc", 100.0, "NAV"));

        mockMvc.perform(post("/api/v1/vehicles/1/accessories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/accessories/10"))
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("GET accessories by vehicle -> 200 list")
    void getAccessoriesByVehicle() throws Exception {
        given(accessoryService.getAllByVehicleId(1L))
                .willReturn(List.of(new AccessoryDto(1L, "A1", null, null, null)));

        mockMvc.perform(get("/api/v1/vehicles/1/accessories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("A1"));
    }

    @Test
    @DisplayName("GET accessory by id -> 200")
    void getAccessory_ok() throws Exception {
        given(accessoryService.getAccessoryById(5L))
                .willReturn(new AccessoryDto(5L, "A", "d", 10.0, "T"));
        mockMvc.perform(get("/api/v1/accessories/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @DisplayName("GET accessory by id -> 404")
    void getAccessory_notFound() throws Exception {
        given(accessoryService.getAccessoryById(5L)).willReturn(null);
        mockMvc.perform(get("/api/v1/accessories/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT update accessory -> 200")
    void updateAccessory_ok() throws Exception {
        given(accessoryService.updateAccessory(eq(9L), any(CreateAccessoryDto.class)))
                .willReturn(new AccessoryDto(9L, "U", "d", 1.0, "T"));
        mockMvc.perform(put("/api/v1/accessories/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAccessoryDto("U", "d", 1.0, "T"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9));
    }

    @Test
    @DisplayName("PUT update accessory -> 404")
    void updateAccessory_notFound() throws Exception {
        given(accessoryService.updateAccessory(eq(9L), any(CreateAccessoryDto.class)))
                .willReturn(null);
        mockMvc.perform(put("/api/v1/accessories/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAccessoryDto("U", "d", 1.0, "T"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE accessory -> 204")
    void deleteAccessory_ok() throws Exception {
        given(accessoryService.deleteAccessory(3L)).willReturn(true);
        mockMvc.perform(delete("/api/v1/accessories/3"))
                .andExpect(status().isNoContent());
        verify(accessoryService).deleteAccessory(3L);
    }

    @Test
    @DisplayName("DELETE accessory -> 404")
    void deleteAccessory_notFound() throws Exception {
        given(accessoryService.deleteAccessory(3L)).willReturn(false);
        mockMvc.perform(delete("/api/v1/accessories/3"))
                .andExpect(status().isNotFound());
    }
}

