package com.renault.garage.dao.repository;

import com.renault.garage.dao.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle,Long> {

    /**
     * Find all vehicles associated with a specific garage ID.
     * @param garageId the ID of the garage
     * @return list of vehicles in the specified garage
     */
    @Query("SELECT v FROM Vehicle v JOIN v.garages g WHERE g.id = :garageId")
    List<Vehicle> findByGarageId(Long garageId);

    /**
     * Find all vehicles of a specific brand.
     * @param brand the brand of the vehicles
     * @return list of vehicles of the specified brand
     */
    List<Vehicle> findByBrandIs(String brand);

    /**
     * Find all vehicles that have an accessory with a name matching the given pattern.
     * @param accessoryName the name pattern of the accessory
     * @return list of vehicles with accessories matching the name pattern
     */
    @Query("SELECT v FROM Vehicle v JOIN v.accessories a WHERE a.name LIKE %:accessoryName%")
    List<Vehicle> findByAccessoryName(String accessoryName);
}
