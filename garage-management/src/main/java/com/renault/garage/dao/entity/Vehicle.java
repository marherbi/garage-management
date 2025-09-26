package com.renault.garage.dao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;

    private int yearOfManufacture;

    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "vehicles")
    private List<Accessory> accessories;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(joinColumns = @JoinColumn(name = "vehicle_id"), inverseJoinColumns = @JoinColumn(name = "garage_id"))
    private List<Garage> garages;

    public void addAccessory(Accessory accessory) {
        if (this.accessories == null) {
            this.accessories = new ArrayList<>();
        }
        this.accessories.add(accessory);
    }

    public void addGarage(Garage garage) {
        if (this.garages == null) {
            this.garages = new ArrayList<>();
        }
        this.garages.add(garage);
    }

    public enum FuelType {
        GASOLINE,
        DIESEL,
        ELECTRIC,
        HYBRID;

        public static FuelType fromString(String value) {
            for (FuelType type : FuelType.values()) {
                if (type.name().equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return null;
        }
    }
}
