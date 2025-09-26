package com.renault.garage.service;

import com.renault.garage.dao.entity.Accessory;
import com.renault.garage.dao.entity.Vehicle;
import com.renault.garage.dao.repository.AccessoryRepository;
import com.renault.garage.dao.repository.VehicleRepository;
import com.renault.garage.dto.AccessoryDto;
import com.renault.garage.dto.CreateAccessoryDto;
import com.renault.garage.mapper.AccessoryMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AccessoryService {

    private final AccessoryRepository accessoryRepository;
    private final VehicleRepository vehicleRepository;
    AccessoryMapper accessoryMapper = AccessoryMapper.INSTANCE;

    public AccessoryService(AccessoryRepository accessoryRepository, VehicleRepository vehicleRepository) {
        this.accessoryRepository = accessoryRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public AccessoryDto getAccessoryById(long id) {
        return accessoryRepository.findById(id)
                .map(accessoryMapper::entity2dto)
                .orElse(null);
    }

    @Transactional
    public boolean deleteAccessory(long id) {
        if (!accessoryRepository.existsById(id)) {
            return false;
        }
        accessoryRepository.deleteById(id);
        return true;
    }

    @Transactional
    public AccessoryDto createAccessory(long vehicleId, CreateAccessoryDto accessoryDto) {
        if (accessoryDto == null) {
            return null;
        }
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id=" + vehicleId));

        Accessory accessory = accessoryMapper.dto2entity(accessoryDto);

        vehicle.addAccessory(accessory);
        accessory.addVehicle(vehicle);

        Accessory newAccessory = accessoryRepository.save(accessory);
        return accessoryMapper.entity2dto(newAccessory);
    }

    @Transactional
    public AccessoryDto updateAccessory(long id, CreateAccessoryDto accessoryDto) {
        Optional<Accessory> opt = accessoryRepository.findById(id);
        if (opt.isEmpty()) return null;
        Accessory entity = opt.get();
        accessoryMapper.updateEntityFromDto(accessoryDto, entity);
        Accessory saved = accessoryRepository.save(entity);
        return accessoryMapper.entity2dto(saved);
    }

    @Transactional(readOnly = true)
    public List<AccessoryDto> getAllByVehicleId(long vehicleId) {
        return accessoryRepository.findByVehicleId(vehicleId).stream().map(accessoryMapper::entity2dto).collect(Collectors.toList());
    }
}
