package com.renault.garage.service;

import com.renault.garage.dao.entity.Garage;
import com.renault.garage.dao.entity.OpeningSlot;
import com.renault.garage.dao.entity.Vehicle.FuelType;
import com.renault.garage.dao.repository.GarageRepository;
import com.renault.garage.dto.CreateGarageDto;
import com.renault.garage.dto.GarageDto;
import com.renault.garage.mapper.GarageMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GarageService {

    private final GarageRepository garageRepository;
    GarageMapper garageMapper = GarageMapper.INSTANCE;

    public GarageService(GarageRepository garageRepository) {
        this.garageRepository = garageRepository;
    }

    public GarageDto getGarageById(long id) {
        return garageRepository.findById(id)
                .map(garageMapper::entity2dto)
                .orElse(null);
    }

    @Transactional
    public boolean deleteGarage(long id) {
        if (!garageRepository.existsById(id)) {
            return false;
        }
        garageRepository.deleteById(id);
        return true;
    }

    @Transactional
    public GarageDto createGarage(CreateGarageDto dto) {
        if (dto == null) return null;
        Garage garageEntity = garageMapper.dto2entity(dto);
        Garage saved = garageRepository.save(garageEntity);
        return garageMapper.entity2dto(saved);
    }

    @Transactional
    public GarageDto updateGarage(long id, CreateGarageDto dto) {
        Optional<Garage> opt = garageRepository.findById(id);
        if (opt.isEmpty()) return null;
        Garage entity = opt.get();
        if (dto.name() != null) entity.setName(dto.name());
        if (dto.address() != null) entity.setAddress(dto.address());
        if (dto.telephone() != null) entity.setTelephone(dto.telephone());
        if (dto.email() != null) entity.setEmail(dto.email());
        if (dto.openingHours() != null) {
            List<OpeningSlot> newSlots = garageMapper.map(dto.openingHours());
            if (entity.getOpeningSlots() == null) {
                entity.setOpeningSlots(new ArrayList<>());
            } else {
                entity.getOpeningSlots().clear();
            }
            entity.getOpeningSlots().addAll(newSlots);
        }
        Garage saved = garageRepository.save(entity);
        return garageMapper.entity2dto(saved);
    }

    public Page<GarageDto> getGarages(Pageable pageable) {
        return garageRepository.findAll(pageable).map(garageMapper::entity2dto);
    }

    public Page<GarageDto> getGaragesHavingVehiclesWithFuelType(String vehicleFuelType, Pageable pageable) {
        FuelType fuelType = FuelType.fromString(vehicleFuelType);
        if (fuelType == null) {
            return Page.empty(pageable);
        }
        return garageRepository.findDistinctByVehicles_FuelTypeIgnoreCase(fuelType, pageable).map(garageMapper::entity2dto);
    }
}
