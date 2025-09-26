package com.renault.garage.dto;

import com.renault.garage.dao.entity.Vehicle;

public record VehicleDto(Long id,
                         String brand,
                         int yearOfManufacture,
                         Vehicle.FuelType fuelType) {
}