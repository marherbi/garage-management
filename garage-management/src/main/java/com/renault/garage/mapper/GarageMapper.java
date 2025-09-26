package com.renault.garage.mapper;

import com.renault.garage.dao.entity.Garage;
import com.renault.garage.dao.entity.OpeningSlot;
import com.renault.garage.dao.entity.OpeningTime;
import com.renault.garage.dto.CreateGarageDto;
import com.renault.garage.dto.GarageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.mapping;

@Mapper
public interface GarageMapper {

    GarageMapper INSTANCE = Mappers.getMapper(GarageMapper.class);


    @Mapping(target = "openingHours", source = "openingSlots")
    GarageDto entity2dto(Garage garage);

    default Map<DayOfWeek, List<OpeningTime>> map(List<OpeningSlot> openingSlots) {
        return openingSlots.stream()
                .collect(
                        java.util.stream.Collectors.groupingBy(
                                OpeningSlot::getDayOfWeek,
                                mapping(OpeningSlot::getOpeningTime, java.util.stream.Collectors.toList())
                        )
                );
    }

    @Mapping(target = "openingSlots", source = "openingHours")
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "id", ignore = true)
    Garage dto2entity(CreateGarageDto createGarageDto);

    default List<OpeningSlot> map(Map<DayOfWeek, List<OpeningTime>> openingHours) {
        return openingHours.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(openingTime -> {
                            OpeningSlot slot = new OpeningSlot();
                            slot.setDayOfWeek(entry.getKey());
                            slot.setOpeningTime(openingTime);
                            return slot;
                        }))
                .toList();
    }



}
