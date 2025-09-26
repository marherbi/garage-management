package com.renault.garage.service;

import com.renault.garage.dao.entity.Garage;
import com.renault.garage.dao.entity.Vehicle;
import com.renault.garage.dao.repository.GarageRepository;
import com.renault.garage.dao.repository.VehicleRepository;
import com.renault.garage.dto.CreateVehicleDto;
import com.renault.garage.dto.VehicleDto;
import com.renault.garage.event.VehicleCreatedEvent;
import com.renault.garage.exception.GarageCapacityExceededException;
import com.renault.garage.mapper.VehicleMapper;
import jakarta.persistence.EntityNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VehicleService {

    private final ApplicationEventPublisher publisher;

    private final VehicleRepository vehicleRepository;
    private final GarageRepository garageRepository;

    private final VehicleMapper vehicleMapper = VehicleMapper.INSTANCE;

    private int maxVehiclesPerGarage;

    public VehicleService(ApplicationEventPublisher publisher, VehicleRepository vehicleRepository,
                          GarageRepository garageRepository) {
        this.publisher = publisher;
        this.vehicleRepository = vehicleRepository;
        this.garageRepository = garageRepository;
    }

    @Autowired
    public void setMaxVehiclesPerGarage(@Value("${garage.vehicle.max-per-garage:5}") int maxVehiclesPerGarage) {
        this.maxVehiclesPerGarage = maxVehiclesPerGarage;
    }

    public VehicleDto create(Long garageId, CreateVehicleDto dto) throws GarageCapacityExceededException {
        if (dto == null) {
            return null;
        }
        Garage garage = garageRepository.findById(garageId)
                .orElseThrow(() -> new EntityNotFoundException("Garage not found with id=" + garageId));
        assertGarageCapacityNotExceeded(garage);

        Vehicle vehicleEntity = vehicleMapper.dto2entity(dto);

        garage.addVehicle(vehicleEntity);
        vehicleEntity.addGarage(garage);

        Vehicle newVehicle = vehicleRepository.save(vehicleEntity);
        publisher.publishEvent(new VehicleCreatedEvent(newVehicle.getId()));
        return vehicleMapper.entity2dto(newVehicle);
    }

    public VehicleDto update(long vehicleId, CreateVehicleDto dto) {
        if (dto == null) return null;
        Optional<Vehicle> opt = vehicleRepository.findById(vehicleId);
        if (opt.isEmpty()) return null;
        Vehicle vehicleEntity = opt.get();
        vehicleMapper.updateEntityFromDto(dto, vehicleEntity);
        return vehicleMapper.entity2dto(vehicleRepository.save(vehicleEntity));
    }

    public boolean delete(Long vehicleId) {
        if (vehicleId == null || !vehicleRepository.existsById(vehicleId)) return false;
        vehicleRepository.deleteById(vehicleId);
        return true;
    }

    @Transactional(readOnly = true)
    public VehicleDto get(Long vehicleId) {
        return vehicleRepository.findById(vehicleId).map(vehicleMapper::entity2dto).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> getAll() {
        return vehicleRepository.findAll().stream().map(vehicleMapper::entity2dto).toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> getByGarage(Long garageId) {
        return vehicleRepository.findByGarageId(garageId).stream().map(vehicleMapper::entity2dto).toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> getByBrand(String brand) {
        return vehicleRepository.findByBrandIs(brand).stream().map(vehicleMapper::entity2dto).toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> getByAccessory(String accessoryName) {
        return vehicleRepository.findByAccessoryName(accessoryName).stream().map(vehicleMapper::entity2dto).toList();
    }

    public List<VehicleDto> linkVehicleToGarage(Long garageId, Long vehicleId) throws GarageCapacityExceededException {
        Garage garage = garageRepository.findById(garageId)
                .orElseThrow(() -> new EntityNotFoundException("Garage not found with id=" + garageId));
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id=" + vehicleId));

        if (garage.getVehicles() != null && garage.getVehicles().stream().anyMatch(v -> v.getId().equals(vehicleId))) {
            return getByGarage(garageId);
        }

        assertGarageCapacityNotExceeded(garage);

        garage.addVehicle(vehicle);
        vehicle.addGarage(garage);

        vehicleRepository.save(vehicle);
        return getByGarage(garageId);
    }

    private void assertGarageCapacityNotExceeded(Garage garage) throws GarageCapacityExceededException {
        int currentSize = CollectionUtils.size(garage.getVehicles());
        if (currentSize >= maxVehiclesPerGarage) {
            throw new GarageCapacityExceededException("Capacité maximale de " + maxVehiclesPerGarage + " véhicules atteinte pour le garage id=" + garage.getId());
        }
    }
}
