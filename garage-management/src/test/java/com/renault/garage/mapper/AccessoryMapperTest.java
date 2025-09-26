package com.renault.garage.mapper;

import com.renault.garage.dao.entity.Accessory;
import com.renault.garage.dto.AccessoryDto;
import com.renault.garage.dto.CreateAccessoryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccessoryMapperTest {

    AccessoryMapper mapper = AccessoryMapper.INSTANCE;

    @Test
    @DisplayName("dto2entity - id ignoré & vehicles null")
    void dto2entity() {
        CreateAccessoryDto dto = new CreateAccessoryDto("GPS", "desc", 10.0, "NAV");
        Accessory entity = mapper.dto2entity(dto);
        assertThat(entity.getId()).isNull();
        assertThat(entity.getVehicles()).isNull();
        assertThat(entity.getName()).isEqualTo("GPS");
    }

    @Test
    @DisplayName("entity2dto - simple mapping")
    void entity2dto() {
        Accessory a = new Accessory();
        a.setId(5L);
        a.setName("Caméra");
        a.setDescription("desc");
        a.setPrice(99.5);
        a.setType("SEC");
        AccessoryDto dto = mapper.entity2dto(a);
        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.name()).isEqualTo("Caméra");
    }

    @Test
    @DisplayName("updateEntityFromDto - champs mis à jour")
    void updateEntity() {
        Accessory a = new Accessory();
        a.setId(9L);
        a.setName("OLD");
        mapper.updateEntityFromDto(new CreateAccessoryDto("NEW", "d", 1.0, "T"), a);
        assertThat(a.getName()).isEqualTo("NEW");
        assertThat(a.getId()).isEqualTo(9L); // id untouched
    }
}

