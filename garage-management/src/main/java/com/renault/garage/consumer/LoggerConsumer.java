package com.renault.garage.consumer;

import com.renault.garage.event.VehicleCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggerConsumer {

    /**
     * Method to consume VehicleCreatedEvent and log the vehicle ID.
     * @param vehicleCreatedEvent the event containing vehicle details
     */
    @EventListener
    public void consumeCreatedVehicleEvent(VehicleCreatedEvent vehicleCreatedEvent) {
        log.info("Received Vehicle Created Event: {}", vehicleCreatedEvent.vehicleId());
    }

}
