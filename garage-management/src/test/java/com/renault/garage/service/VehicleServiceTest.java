package com.renault.garage.service;

import com.renault.garage.dao.entity.Garage;
import com.renault.garage.dao.entity.Vehicle;
import com.renault.garage.dao.repository.GarageRepository;
import com.renault.garage.dao.repository.VehicleRepository;
import com.renault.garage.dto.CreateVehicleDto;
import com.renault.garage.dto.VehicleDto;
import com.renault.garage.exception.GarageCapacityExceededException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    VehicleRepository vehicleRepository;
    @Mock
    GarageRepository garageRepository;
    @Mock
    ApplicationEventPublisher publisher;

    @InjectMocks
    VehicleService vehicleService; // sera instancié avec le constructeur (publisher, vehicleRepository, garageRepository)

    @BeforeEach
    void setup() {
        // Configure la capacité maximale à 2 pour les tests
        vehicleService.setMaxVehiclesPerGarage(2);
    }

    private CreateVehicleDto sampleDto() {
        return new CreateVehicleDto("Renault", 2024, Vehicle.FuelType.GASOLINE);
    }

    @Test
    @DisplayName("create - garage inexistant -> EntityNotFoundException")
    void create_garageNotFound() {
        when(garageRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> vehicleService.create(1L, sampleDto()));
    }

    @Test
    @DisplayName("create - capacité dépassée -> GarageCapacityExceededException")
    void create_capacityExceeded() {
        Garage g = new Garage(); g.setId(5L);
        List<Vehicle> existing = new ArrayList<>();
        Vehicle v1 = new Vehicle(); v1.setId(10L); existing.add(v1);
        Vehicle v2 = new Vehicle(); v2.setId(11L); existing.add(v2);
        g.setVehicles(existing);
        when(garageRepository.findById(5L)).thenReturn(Optional.of(g));
        assertThrows(GarageCapacityExceededException.class, () -> vehicleService.create(5L, sampleDto()));
    }

    @Test
    @DisplayName("create - ok -> relation bidirectionnelle + save")
    void create_ok() throws Exception {
        Garage g = new Garage(); g.setId(3L); g.setVehicles(new ArrayList<>());
        when(garageRepository.findById(3L)).thenReturn(Optional.of(g));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> { Vehicle veh = invocation.getArgument(0); veh.setId(100L); return veh;});

        VehicleDto created = vehicleService.create(3L, sampleDto());
        assertThat(created).isNotNull();
        assertThat(created.id()).isEqualTo(100L);
        assertThat(g.getVehicles()).hasSize(1);
        assertThat(g.getVehicles().getFirst().getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("update - absent -> null")
    void update_absent() {
        when(vehicleRepository.findById(9L)).thenReturn(Optional.empty());
        assertThat(vehicleService.update(9L, sampleDto())).isNull();
    }

    @Test
    @DisplayName("update - présent -> champs mis à jour")
    void update_present() {
        Vehicle vehicle = new Vehicle(); vehicle.setId(7L); vehicle.setBrand("OLD"); vehicle.setYearOfManufacture(2000); vehicle.setFuelType(Vehicle.FuelType.DIESEL);
        when(vehicleRepository.findById(7L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));
        VehicleDto updated = vehicleService.update(7L, sampleDto());
        assertThat(updated.brand()).isEqualTo("Renault");
        assertThat(updated.yearOfManufacture()).isEqualTo(2024);
        assertThat(updated.fuelType()).isEqualTo(Vehicle.FuelType.GASOLINE);
    }

    @Test
    @DisplayName("delete - absent -> false")
    void delete_absent() {
        when(vehicleRepository.existsById(50L)).thenReturn(false);
        assertThat(vehicleService.delete(50L)).isFalse();
        verify(vehicleRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("delete - présent -> true")
    void delete_present() {
        when(vehicleRepository.existsById(51L)).thenReturn(true);
        assertThat(vehicleService.delete(51L)).isTrue();
        verify(vehicleRepository).deleteById(51L);
    }

    @Test
    @DisplayName("get - présent -> dto")
    void get_present() {
        Vehicle v = new Vehicle(); v.setId(60L); v.setBrand("B");
        when(vehicleRepository.findById(60L)).thenReturn(Optional.of(v));
        assertThat(vehicleService.get(60L)).isNotNull();
    }

    @Test
    @DisplayName("getAll -> mapping liste")
    void getAll() {
        Vehicle v1 = new Vehicle(); v1.setId(1L);
        Vehicle v2 = new Vehicle(); v2.setId(2L);
        when(vehicleRepository.findAll()).thenReturn(List.of(v1, v2));
        assertThat(vehicleService.getAll()).hasSize(2);
    }

    @Test
    @DisplayName("getByGarage -> repository délégué")
    void getByGarage() {
        when(vehicleRepository.findByGarageId(5L)).thenReturn(List.of(new Vehicle()));
        assertThat(vehicleService.getByGarage(5L)).hasSize(1);
    }

    @Test
    @DisplayName("getByBrand -> repository délégué")
    void getByBrand() {
        when(vehicleRepository.findByBrandIs("Renault")).thenReturn(List.of(new Vehicle()));
        assertThat(vehicleService.getByBrand("Renault")).hasSize(1);
    }

    @Test
    @DisplayName("getByAccessory -> repository délégué")
    void getByAccessory() {
        when(vehicleRepository.findByAccessoryName("GPS")).thenReturn(List.of(new Vehicle()));
        assertThat(vehicleService.getByAccessory("GPS")).hasSize(1);
    }

    @Test
    @DisplayName("linkVehicleToGarage - garage inexistant -> EntityNotFoundException")
    void link_garageNotFound() {
        when(garageRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> vehicleService.linkVehicleToGarage(1L, 2L));
    }

    @Test
    @DisplayName("linkVehicleToGarage - véhicule inexistant -> EntityNotFoundException")
    void link_vehicleNotFound() {
        Garage g = new Garage(); g.setId(1L); g.setVehicles(new ArrayList<>());
        when(garageRepository.findById(1L)).thenReturn(Optional.of(g));
        when(vehicleRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> vehicleService.linkVehicleToGarage(1L, 2L));
    }

    @Test
    @DisplayName("linkVehicleToGarage - déjà lié -> retourne liste existante")
    void link_alreadyLinked() throws Exception {
        Garage g = new Garage(); g.setId(1L); g.setVehicles(new ArrayList<>());
        Vehicle v = new Vehicle(); v.setId(2L);
        g.getVehicles().add(v);
        when(garageRepository.findById(1L)).thenReturn(Optional.of(g));
        when(vehicleRepository.findById(2L)).thenReturn(Optional.of(v));
        when(vehicleRepository.findByGarageId(1L)).thenReturn(List.of(v));
        List<VehicleDto> list = vehicleService.linkVehicleToGarage(1L,2L);
        assertThat(list).hasSize(1);
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    @DisplayName("linkVehicleToGarage - capacité ok -> lien créé")
    void link_ok() throws Exception {
        Garage g = new Garage(); g.setId(1L); g.setVehicles(new ArrayList<>());
        Vehicle v = new Vehicle(); v.setId(2L);
        when(garageRepository.findById(1L)).thenReturn(Optional.of(g));
        when(vehicleRepository.findById(2L)).thenReturn(Optional.of(v));
        when(vehicleRepository.findByGarageId(1L)).thenReturn(List.of(v));

        List<VehicleDto> list = vehicleService.linkVehicleToGarage(1L,2L);
        assertThat(list).hasSize(1);
        assertThat(g.getVehicles()).hasSize(1);
        assertThat(v.getGarages()).hasSize(1);
        verify(vehicleRepository).save(v);
    }

    @Test
    @DisplayName("linkVehicleToGarage - capacité dépassée -> exception")
    void link_capacityExceeded() {
        Garage g = new Garage(); g.setId(1L); g.setVehicles(new ArrayList<>());
        Vehicle v1 = new Vehicle(); v1.setId(10L); Vehicle v2 = new Vehicle(); v2.setId(11L);
        g.getVehicles().add(v1); g.getVehicles().add(v2); // atteint max (2)
        Vehicle v = new Vehicle(); v.setId(2L);
        when(garageRepository.findById(1L)).thenReturn(Optional.of(g));
        when(vehicleRepository.findById(2L)).thenReturn(Optional.of(v));
        assertThrows(GarageCapacityExceededException.class, () -> vehicleService.linkVehicleToGarage(1L,2L));
    }
}
