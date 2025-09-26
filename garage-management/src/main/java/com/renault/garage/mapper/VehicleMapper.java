package com.renault.garage.mapper;

import com.renault.garage.dao.entity.Vehicle;
import com.renault.garage.dto.CreateVehicleDto;
import com.renault.garage.dto.VehicleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface VehicleMapper {

    VehicleMapper INSTANCE = Mappers.getMapper(VehicleMapper.class);

    VehicleDto entity2dto(Vehicle vehicle);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "garages", ignore = true)
    @Mapping(target = "accessories", ignore = true)
    Vehicle dto2entity(CreateVehicleDto vehicleDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "garages", ignore = true)
    @Mapping(target = "accessories", ignore = true)
    void updateEntityFromDto(CreateVehicleDto dto, @MappingTarget Vehicle entity);

}
