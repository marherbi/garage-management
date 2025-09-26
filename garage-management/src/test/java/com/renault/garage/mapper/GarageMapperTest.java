package com.renault.garage.mapper;

import com.renault.garage.dao.entity.Garage;
import com.renault.garage.dao.entity.OpeningSlot;
import com.renault.garage.dao.entity.OpeningTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GarageMapperTest {

    GarageMapper mapper = GarageMapper.INSTANCE;

    @Test
    @DisplayName("entity2dto - regroupe les OpeningSlot par DayOfWeek")
    void entity2dto_grouping() {
        Garage g = new Garage();
        g.setId(1L);
        g.setName("G1");
        OpeningSlot s1 = new OpeningSlot(null, DayOfWeek.MONDAY, new OpeningTime(LocalTime.of(8, 0), LocalTime.of(12, 0)));
        OpeningSlot s2 = new OpeningSlot(null, DayOfWeek.MONDAY, new OpeningTime(LocalTime.of(14, 0), LocalTime.of(18, 0)));
        OpeningSlot s3 = new OpeningSlot(null, DayOfWeek.TUESDAY, new OpeningTime(LocalTime.of(9, 0), LocalTime.of(17, 0)));
        g.setOpeningSlots(List.of(s1, s2, s3));

        var dto = mapper.entity2dto(g);
        assertThat(dto.openingHours()).hasSize(2);
        assertThat(dto.openingHours().get(DayOfWeek.MONDAY)).hasSize(2);
        assertThat(dto.openingHours().get(DayOfWeek.TUESDAY)).hasSize(1);
    }

    @Test
    @DisplayName("dto2entity - crée OpeningSlot pour chaque entrée du map")
    void dto2entity_mapping() {
        Map<DayOfWeek, List<OpeningTime>> map = Map.of(
                DayOfWeek.WEDNESDAY, List.of(new OpeningTime(LocalTime.of(7, 0), LocalTime.of(11, 0)), new OpeningTime(LocalTime.of(13, 0), LocalTime.of(16, 0)))
        );
        var createDto = new com.renault.garage.dto.CreateGarageDto("G2", "a", "t", "m", map);
        Garage entity = mapper.dto2entity(createDto);
        assertThat(entity.getOpeningSlots()).hasSize(2);
        assertThat(entity.getOpeningSlots().stream().allMatch(s -> s.getDayOfWeek() == DayOfWeek.WEDNESDAY)).isTrue();
    }
}

