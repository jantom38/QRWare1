package com.qrware.dto;

import com.qrware.domain.warehouse.ZoneType;
import java.time.LocalDateTime; // Upewnij się, że to jest LocalDateTime

public record ZoneDTO(
        Long id,
        String name,
        String code,
        String description,
        ZoneType type,
        Boolean active,
        Boolean temperatureControlled,
        Integer temperatureMin,
        Integer temperatureMax,
        Boolean humidityControlled,
        Integer humidityMin,
        Integer humidityMax,
        Integer securityLevel,
        Boolean hazardousMaterials,
        Boolean fragileItems,
        Integer pickingPriority,
        String manager,
        String contactInfo,
        String color,
        LocalDateTime createdAt, // Sprawdź ten typ
        LocalDateTime updatedAt, // Sprawdź ten typ
        String createdBy,
        String updatedBy,
        int locationCount,
        long activeLocationCount,
        long occupiedLocationCount,
        double occupancyRate
) {
}