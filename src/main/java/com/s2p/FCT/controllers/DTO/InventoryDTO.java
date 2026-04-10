package com.s2p.FCT.controllers.DTO;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class InventoryDTO {
    private UUID id;
    private String name;
    private String description;
    private String category;
    private Double price;
    private Integer stockCount;
    private String imagePaths; // Comma-separated paths for images
    private Boolean inStock;
    private Boolean isNew;
    private Boolean isOnSale;

    // Add more fields as needed for frontend

    public InventoryDTO() {}

    public InventoryDTO(UUID id, String name, String description, String category,
                        Double price, Integer stockCount, String imagePaths,
                        Boolean inStock, Boolean isNew, Boolean isOnSale) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stockCount = stockCount;
        this.imagePaths = imagePaths;
        this.inStock = inStock;
        this.isNew = isNew;
        this.isOnSale = isOnSale;
    }

    // Getters and setters
    // ...
}
