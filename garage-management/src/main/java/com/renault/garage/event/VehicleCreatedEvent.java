package com.renault.garage.event;

/**
 * Event triggered when a vehicle is created.
 * @param vehicleId
 */
public record VehicleCreatedEvent(Long vehicleId) {
}
