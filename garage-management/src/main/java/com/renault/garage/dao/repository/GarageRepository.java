package com.renault.garage.dao.repository;

import com.renault.garage.dao.entity.Garage;
import com.renault.garage.dao.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarageRepository extends PagingAndSortingRepository<Garage, Long>, JpaRepository<Garage, Long> {

    /**
     * Find garages by vehicle fuel type
     * @param vehicleFuelType the fuel type of the vehicles
     * @param pageable the pagination information
     * @return a page of garages
     */
    @Query("SELECT DISTINCT g FROM Garage g JOIN g.vehicles v WHERE v.fuelType = :vehicles_fuelType")
    Page<Garage> findDistinctByVehicles_FuelTypeIgnoreCase(Vehicle.FuelType vehicleFuelType, Pageable pageable);
}
