package com.smartfridge.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "food_categories")
public class FoodCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "storage_location")
    private String storageLocation;

    @Column(name = "optimal_temperature_min")
    private Double optimalTemperatureMin;

    @Column(name = "optimal_temperature_max")
    private Double optimalTemperatureMax;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public Double getOptimalTemperatureMin() {
        return optimalTemperatureMin;
    }

    public void setOptimalTemperatureMin(Double optimalTemperatureMin) {
        this.optimalTemperatureMin = optimalTemperatureMin;
    }

    public Double getOptimalTemperatureMax() {
        return optimalTemperatureMax;
    }

    public void setOptimalTemperatureMax(Double optimalTemperatureMax) {
        this.optimalTemperatureMax = optimalTemperatureMax;
    }
}
