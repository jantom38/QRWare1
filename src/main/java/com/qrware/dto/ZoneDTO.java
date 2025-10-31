package com.qrware.dto;

import com.qrware.domain.warehouse.ZoneType;

public class ZoneDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private ZoneType type;
    private Integer temperature;
    private Integer humidity;
    private String color;

    // Constructors
    public ZoneDTO() {}

    // Gettery i settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ZoneType getType() { return type; }
    public void setType(ZoneType type) { this.type = type; }
    
    public Integer getTemperature() { return temperature; }
    public void setTemperature(Integer temperature) { this.temperature = temperature; }
    
    public Integer getHumidity() { return humidity; }
    public void setHumidity(Integer humidity) { this.humidity = humidity; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}