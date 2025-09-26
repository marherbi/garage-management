package com.renault.garage.mapper;

import com.renault.garage.dao.entity.Vehicle;
import com.renault.garage.dto.CreateVehicleDto;
import com.renault.garage.dto.VehicleDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleMapperTest {

    VehicleMapper mapper = VehicleMapper.INSTANCE;

    @Test
    @DisplayName("dto2entity - ignore id & relations")
    void dto2entity() {
        CreateVehicleDto dto = new CreateVehicleDto("Renault", 2023, Vehicle.FuelType.GASOLINE);
        Vehicle entity = mapper.dto2entity(dto);
        assertThat(entity.getId()).isNull();
        assertThat(entity.getBrand()).isEqualTo("Renault");
        assertThat(entity.getGarages()).isNull();
        assertThat(entity.getAccessories()).isNull();
    }

    @Test
    @DisplayName("entity2dto - simple mapping")
    void entity2dto() {
        Vehicle v = new Vehicle();
        v.setId(10L);
        v.setBrand("Peugeot");
        v.setYearOfManufacture(2019);
        v.setFuelType(Vehicle.FuelType.DIESEL);
        VehicleDto dto = mapper.entity2dto(v);
        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.brand()).isEqualTo("Peugeot");
    }

    @Test
    @DisplayName("updateEntityFromDto - conserve id")
    void updateEntity() {
        Vehicle v = new Vehicle();
        v.setId(5L);
        v.setBrand("Old");
        v.setYearOfManufacture(2000);
        v.setFuelType(Vehicle.FuelType.ELECTRIC);
        mapper.updateEntityFromDto(new CreateVehicleDto("New", 2024, Vehicle.FuelType.HYBRID), v);
        assertThat(v.getId()).isEqualTo(5L);
        assertThat(v.getBrand()).isEqualTo("New");
        assertThat(v.getYearOfManufacture()).isEqualTo(2024);
        assertThat(v.getFuelType()).isEqualTo(Vehicle.FuelType.HYBRID);
    }
}

