package com.renault.garage.service;

import com.renault.garage.dao.entity.Garage;
import com.renault.garage.dao.entity.OpeningSlot;
import com.renault.garage.dao.entity.OpeningTime;
import com.renault.garage.dao.entity.Vehicle;
import com.renault.garage.dao.repository.GarageRepository;
import com.renault.garage.dto.CreateGarageDto;
import com.renault.garage.dto.GarageDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GarageServiceTest {

    @Mock
    GarageRepository garageRepository;

    @InjectMocks
    GarageService garageService;

    @Test
    @DisplayName("getGarageById - présent -> DTO")
    void getGarageById_present() {
        Garage g = new Garage();
        g.setId(1L);
        g.setName("GAR1");
        g.setOpeningSlots(new ArrayList<>(List.of(new OpeningSlot(1L, DayOfWeek.MONDAY, new OpeningTime(LocalTime.of(8, 0), LocalTime.of(12, 0))))));
        when(garageRepository.findById(1L)).thenReturn(Optional.of(g));
        GarageDto dto = garageService.getGarageById(1L);
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("GAR1");
    }

    @Test
    @DisplayName("getGarageById - absent -> null")
    void getGarageById_absent() {
        when(garageRepository.findById(9L)).thenReturn(Optional.empty());
        assertThat(garageService.getGarageById(9L)).isNull();
    }

    @Test
    @DisplayName("deleteGarage - absent -> false")
    void deleteGarage_absent() {
        when(garageRepository.existsById(5L)).thenReturn(false);
        assertThat(garageService.deleteGarage(5L)).isFalse();
        verify(garageRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteGarage - présent -> true")
    void deleteGarage_present() {
        when(garageRepository.existsById(5L)).thenReturn(true);
        assertThat(garageService.deleteGarage(5L)).isTrue();
        verify(garageRepository).deleteById(5L);
    }

    @Test
    @DisplayName("createGarage - null dto -> null")
    void createGarage_null() {
        assertThat(garageService.createGarage(null)).isNull();
        verify(garageRepository, never()).save(any());
    }

    @Test
    @DisplayName("createGarage - ok -> save avec slots mappés")
    void createGarage_ok() {
        Map<DayOfWeek, List<OpeningTime>> opening = Map.of(
                DayOfWeek.MONDAY, List.of(new OpeningTime(LocalTime.of(8, 0), LocalTime.of(12, 0)))
        );
        CreateGarageDto dto = new CreateGarageDto("GAR", "ADDR", "TEL", "MAIL", opening);
        when(garageRepository.save(any(Garage.class))).thenAnswer(invocation -> {
            Garage gar = invocation.getArgument(0);
            gar.setId(10L);
            return gar;
        });
        GarageDto created = garageService.createGarage(dto);
        assertThat(created).isNotNull();
        assertThat(created.id()).isEqualTo(10L);
        assertThat(created.openingHours()).containsKey(DayOfWeek.MONDAY);
    }

    @Test
    @DisplayName("updateGarage - absent -> null")
    void updateGarage_absent() {
        when(garageRepository.findById(1L)).thenReturn(Optional.empty());
        assertThat(garageService.updateGarage(1L, new CreateGarageDto(null, null, null, null, null))).isNull();
    }

    @Test
    @DisplayName("updateGarage - champ partiel -> modifie seulement non null")
    void updateGarage_partial() {
        Garage g = new Garage();
        g.setId(2L);
        g.setName("OLD");
        g.setAddress("ADDR1");
        g.setOpeningSlots(new ArrayList<>(List.of(new OpeningSlot())));
        when(garageRepository.findById(2L)).thenReturn(Optional.of(g));
        when(garageRepository.save(any(Garage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<DayOfWeek, List<OpeningTime>> newOpening = Map.of(DayOfWeek.TUESDAY, List.of(new OpeningTime(LocalTime.of(9, 0), LocalTime.of(13, 0))));
        GarageDto updated = garageService.updateGarage(2L, new CreateGarageDto("NEW", null, null, null, newOpening));
        assertThat(updated).isNotNull();
        assertThat(updated.name()).isEqualTo("NEW");
        assertThat(updated.address()).isEqualTo("ADDR1");
        assertThat(updated.openingHours()).containsKey(DayOfWeek.TUESDAY);
    }

    @Test
    @DisplayName("getGarages - page mapping")
    void getGarages_page() {
        Garage g1 = new Garage();
        g1.setId(1L);
        g1.setName("G1");
        g1.setOpeningSlots(new ArrayList<>(List.of(new OpeningSlot(1L, DayOfWeek.MONDAY, new OpeningTime(LocalTime.of(8, 0), LocalTime.of(12, 0))))));
        Garage g2 = new Garage();
        g2.setId(2L);
        g2.setName("G2");
        g2.setOpeningSlots(new ArrayList<>(List.of(new OpeningSlot(2L, DayOfWeek.TUESDAY, new OpeningTime(LocalTime.of(9, 0), LocalTime.of(13, 0))))));
        Page<Garage> page = new PageImpl<>(List.of(g1, g2), PageRequest.of(0, 2), 2);
        when(garageRepository.findAll(any(Pageable.class))).thenReturn(page);
        Page<GarageDto> result = garageService.getGarages(PageRequest.of(0, 2));
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("getGaragesHavingVehiclesWithFuelType - type invalide -> page vide")
    void getGaragesHavingVehiclesWithFuelType_invalid() {
        Page<GarageDto> page = garageService.getGaragesHavingVehiclesWithFuelType("UNKNOWN", PageRequest.of(0, 1));
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("getGaragesHavingVehiclesWithFuelType - type valide -> page mappée")
    void getGaragesHavingVehiclesWithFuelType_valid() {
        Garage g = new Garage();
        g.setId(3L);
        g.setName("G3");
        OpeningSlot slot = new OpeningSlot(1L, DayOfWeek.WEDNESDAY, new OpeningTime(LocalTime.of(8, 0), LocalTime.of(12, 0)));
        g.setOpeningSlots(new ArrayList<>(List.of(slot)));
        Page<Garage> page = new PageImpl<>(List.of(g));
        when(garageRepository.findDistinctByVehicles_FuelTypeIgnoreCase(eq(Vehicle.FuelType.GASOLINE), any(Pageable.class))).thenReturn(page);
        Page<GarageDto> result = garageService.getGaragesHavingVehiclesWithFuelType("GASOLINE", PageRequest.of(0, 5));
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("G3");
    }
}

