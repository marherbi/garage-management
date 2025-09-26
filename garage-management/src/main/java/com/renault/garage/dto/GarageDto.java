package com.renault.garage.dto;

import com.renault.garage.dao.entity.OpeningTime;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

public record GarageDto(Long id,
                        String name,
                        String address,
                        String telephone,
                        String email,
                        Map<DayOfWeek, List<OpeningTime>> openingHours) {
}
