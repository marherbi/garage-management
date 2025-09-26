package com.renault.garage.mapper;

import com.renault.garage.dao.entity.Accessory;
import com.renault.garage.dto.AccessoryDto;
import com.renault.garage.dto.CreateAccessoryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccessoryMapper {

    AccessoryMapper INSTANCE = Mappers.getMapper(AccessoryMapper.class);

    AccessoryDto entity2dto(Accessory accessory);

    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "id", ignore = true)
    Accessory dto2entity(CreateAccessoryDto accessoryDto);

    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(CreateAccessoryDto accessoryDto, @MappingTarget Accessory accessoryEntity);

}
