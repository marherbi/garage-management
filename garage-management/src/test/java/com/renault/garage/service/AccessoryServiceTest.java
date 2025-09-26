package com.renault.garage.service;

import com.renault.garage.dao.entity.Accessory;
import com.renault.garage.dao.entity.Vehicle;
import com.renault.garage.dao.repository.AccessoryRepository;
import com.renault.garage.dao.repository.VehicleRepository;
import com.renault.garage.dto.AccessoryDto;
import com.renault.garage.dto.CreateAccessoryDto;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessoryServiceTest {

    @Mock
    AccessoryRepository accessoryRepository;
    @Mock
    VehicleRepository vehicleRepository;

    @InjectMocks
    AccessoryService accessoryService;

    @Test
    @DisplayName("getAccessoryById - présent -> retourne DTO")
    void getAccessoryById_ok() {
        Accessory accessory = new Accessory();
        accessory.setId(10L);
        accessory.setName("GPS");
        when(accessoryRepository.findById(10L)).thenReturn(Optional.of(accessory));

        AccessoryDto dto = accessoryService.getAccessoryById(10L);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.name()).isEqualTo("GPS");
    }

    @Test
    @DisplayName("getAccessoryById - absent -> null")
    void getAccessoryById_notFound() {
        when(accessoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(accessoryService.getAccessoryById(99L)).isNull();
    }

    @Test
    @DisplayName("deleteAccessory - absent -> false")
    void deleteAccessory_absent() {
        when(accessoryRepository.existsById(5L)).thenReturn(false);
        assertThat(accessoryService.deleteAccessory(5L)).isFalse();
        verify(accessoryRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteAccessory - présent -> true & delete called")
    void deleteAccessory_present() {
        when(accessoryRepository.existsById(6L)).thenReturn(true);
        assertThat(accessoryService.deleteAccessory(6L)).isTrue();
        verify(accessoryRepository).deleteById(6L);
    }

    @Test
    @DisplayName("createAccessory - DTO null -> null")
    void createAccessory_nullDto() {
        AccessoryDto dto = accessoryService.createAccessory(1L, null);
        assertThat(dto).isNull();
        verify(vehicleRepository, never()).findById(anyLong());
        verify(accessoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("createAccessory - véhicule inexistant -> exception")
    void createAccessory_vehicleNotFound() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> accessoryService.createAccessory(1L, new CreateAccessoryDto("GPS", "desc", 100.0, "NAV")));
    }

    @Test
    @DisplayName("createAccessory - ok -> save et relation bidirectionnelle")
    void createAccessory_ok() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(2L);
        when(vehicleRepository.findById(2L)).thenReturn(Optional.of(vehicle));

        when(accessoryRepository.save(any(Accessory.class))).thenAnswer(invocation -> {
            Accessory a = invocation.getArgument(0);
            a.setId(50L);
            return a;
        });

        CreateAccessoryDto dto = new CreateAccessoryDto("GPS", "desc", 120.0, "NAV");
        AccessoryDto created = accessoryService.createAccessory(2L, dto);

        assertThat(created).isNotNull();
        assertThat(created.id()).isEqualTo(50L);
        assertThat(created.name()).isEqualTo("GPS");

        ArgumentCaptor<Accessory> captor = ArgumentCaptor.forClass(Accessory.class);
        verify(accessoryRepository).save(captor.capture());
        Accessory saved = captor.getValue();
        assertThat(saved.getVehicles()).hasSize(1);
        assertThat(saved.getVehicles().getFirst().getId()).isEqualTo(2L);
        assertThat(vehicle.getAccessories()).hasSize(1);
    }

    @Test
    @DisplayName("updateAccessory - absent -> null")
    void updateAccessory_absent() {
        when(accessoryRepository.findById(77L)).thenReturn(Optional.empty());
        assertThat(accessoryService.updateAccessory(77L, new CreateAccessoryDto("a", "b", 1.0, "t"))).isNull();
    }

    @Test
    @DisplayName("updateAccessory - présent -> champs mis à jour")
    void updateAccessory_present() {
        Accessory accessory = new Accessory();
        accessory.setId(88L);
        accessory.setName("OLD");
        when(accessoryRepository.findById(88L)).thenReturn(Optional.of(accessory));
        when(accessoryRepository.save(any(Accessory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccessoryDto updated = accessoryService.updateAccessory(88L, new CreateAccessoryDto("NEW", "desc", 10.0, "TYPE"));
        assertThat(updated).isNotNull();
        assertThat(updated.name()).isEqualTo("NEW");
    }

    @Test
    @DisplayName("getAllByVehicleId - retourne liste mappée")
    void getAllByVehicleId_ok() {
        Accessory a1 = new Accessory();
        a1.setId(1L);
        a1.setName("A1");
        Accessory a2 = new Accessory();
        a2.setId(2L);
        a2.setName("A2");
        when(accessoryRepository.findByVehicleId(10L)).thenReturn(List.of(a1, a2));
        List<AccessoryDto> list = accessoryService.getAllByVehicleId(10L);
        assertThat(list).hasSize(2);
        assertThat(list.stream().map(AccessoryDto::name)).containsExactlyInAnyOrder("A1", "A2");
    }
}
