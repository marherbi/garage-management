package com.renault.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renault.garage.dao.entity.Vehicle;
import com.renault.garage.dto.CreateVehicleDto;
import com.renault.garage.dto.VehicleDto;
import com.renault.garage.exception.GarageCapacityExceededException;
import com.renault.garage.service.VehicleService;
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

@WebMvcTest(controllers = VehicleController.class)
class VehicleControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    VehicleService vehicleService;

    private CreateVehicleDto payload() {
        return new CreateVehicleDto("Renault", 2024, Vehicle.FuelType.GASOLINE);
    }

    @Test
    @DisplayName("POST create vehicle -> 201")
    void createVehicle_created() throws Exception {
        given(vehicleService.create(eq(1L), any(CreateVehicleDto.class)))
                .willReturn(new VehicleDto(10L, "Renault", 2024, Vehicle.FuelType.GASOLINE));
        mockMvc.perform(post("/api/v1/garages/1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location","/api/v1/vehicles/10"))
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("POST create vehicle -> 400 (null)")
    void createVehicle_badRequest() throws Exception {
        given(vehicleService.create(eq(1L), any(CreateVehicleDto.class))).willReturn(null);
        mockMvc.perform(post("/api/v1/garages/1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST create vehicle -> 422 (capacity)")
    void createVehicle_capacityExceeded() throws Exception {
        given(vehicleService.create(eq(1L), any(CreateVehicleDto.class)))
                .willAnswer(invocation -> { throw new GarageCapacityExceededException("Capacité"); });
        mockMvc.perform(post("/api/v1/garages/1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload())))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("PATCH link vehicle -> 200")
    void linkVehicle_ok() throws Exception {
        given(vehicleService.linkVehicleToGarage(1L,2L))
                .willReturn(List.of(new VehicleDto(2L,"B",2020, Vehicle.FuelType.DIESEL)));
        mockMvc.perform(patch("/api/v1/garages/1/vehicles/linking?vehicleId=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    @DisplayName("PATCH link vehicle -> 422")
    void linkVehicle_capacity() throws Exception {
        given(vehicleService.linkVehicleToGarage(1L,2L))
                .willAnswer(i -> { throw new GarageCapacityExceededException("capacité"); });
        mockMvc.perform(patch("/api/v1/garages/1/vehicles/linking?vehicleId=2"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("GET vehicle -> 200")
    void getVehicle_ok() throws Exception {
        given(vehicleService.get(5L)).willReturn(new VehicleDto(5L,"B",2020, Vehicle.FuelType.DIESEL));
        mockMvc.perform(get("/api/v1/vehicles/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @DisplayName("GET vehicle -> 404")
    void getVehicle_notFound() throws Exception {
        given(vehicleService.get(5L)).willReturn(null);
        mockMvc.perform(get("/api/v1/vehicles/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET vehicles -> list 200")
    void listVehicles() throws Exception {
        given(vehicleService.getAll()).willReturn(List.of(new VehicleDto(1L,"B",2020, Vehicle.FuelType.DIESEL)));
        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("PUT update vehicle -> 200")
    void updateVehicle_ok() throws Exception {
        given(vehicleService.update(eq(7L), any(CreateVehicleDto.class)))
                .willReturn(new VehicleDto(7L,"U",2022, Vehicle.FuelType.GASOLINE));
        mockMvc.perform(put("/api/v1/vehicles/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    @DisplayName("PUT update vehicle -> 404")
    void updateVehicle_notFound() throws Exception {
        given(vehicleService.update(eq(7L), any(CreateVehicleDto.class)))
                .willReturn(null);
        mockMvc.perform(put("/api/v1/vehicles/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE vehicle -> 204")
    void deleteVehicle_ok() throws Exception {
        given(vehicleService.delete(9L)).willReturn(true);
        mockMvc.perform(delete("/api/v1/vehicles/9"))
                .andExpect(status().isNoContent());
        verify(vehicleService).delete(9L);
    }

    @Test
    @DisplayName("DELETE vehicle -> 404")
    void deleteVehicle_notFound() throws Exception {
        given(vehicleService.delete(9L)).willReturn(false);
        mockMvc.perform(delete("/api/v1/vehicles/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET vehicles by garage -> 200")
    void listByGarage() throws Exception {
        given(vehicleService.getByGarage(3L)).willReturn(List.of(new VehicleDto(1L,"B",2020, Vehicle.FuelType.DIESEL)));
        mockMvc.perform(get("/api/v1/garage/3/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET vehicles by brand -> 200")
    void listByBrand() throws Exception {
        given(vehicleService.getByBrand("Renault")).willReturn(List.of(new VehicleDto(1L,"Renault",2020, Vehicle.FuelType.DIESEL)));
        mockMvc.perform(get("/api/v1/vehicles/byBrand/Renault"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].brand").value("Renault"));
    }

    @Test
    @DisplayName("GET vehicles by accessory -> 200")
    void listByAccessory_ok() throws Exception {
        given(vehicleService.getByAccessory("GPS")).willReturn(List.of(new VehicleDto(1L,"Renault",2020, Vehicle.FuelType.DIESEL)));
        mockMvc.perform(get("/api/v1/vehicles/byAccessory/GPS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET vehicles by accessory -> 404 (empty)")
    void listByAccessory_notFound() throws Exception {
        given(vehicleService.getByAccessory("GPS")).willReturn(List.of());
        mockMvc.perform(get("/api/v1/vehicles/byAccessory/GPS"))
                .andExpect(status().isNotFound());
    }
}

