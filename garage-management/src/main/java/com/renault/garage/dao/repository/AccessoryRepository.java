package com.renault.garage.dao.repository;

import com.renault.garage.dao.entity.Accessory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AccessoryRepository extends CrudRepository<Accessory, Long> {

    /**
     * Find accessories by vehicle ID
     * @param vehicleId the vehicle ID
     * @return list of accessories
     */
    @Query("SELECT a FROM Accessory a JOIN a.vehicles v WHERE v.id = :vehicleId")
    List<Accessory> findByVehicleId(Long vehicleId);


}
