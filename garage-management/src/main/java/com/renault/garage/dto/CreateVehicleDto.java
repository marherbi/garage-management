package com.renault.garage.dto;

import com.renault.garage.dao.entity.Vehicle;

public record CreateVehicleDto(String brand,
                               Integer yearOfManufacture,
                               Vehicle.FuelType fuelType) {
}

