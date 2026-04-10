package com.s2p.FCT.services.Impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.s2p.FCT.entity.Inventory;
import com.s2p.FCT.repositories.InventoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class InventoryServiceImpl {

    @Autowired
    private InventoryRepository inventoryRepository;

    // Base upload directory inside project folder
    // private final String BASE_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads";
    // Absolute path inside your project
private final String BASE_UPLOAD_DIR = "/home/santosh/Backend/NewBackend/uploads";


    /**
     * Save product with images
     * @param product Inventory object
     * @param images List of MultipartFile images
     * @return Saved Inventory
     * @throws IOException
     */
    public Inventory saveProductWithImages(Inventory product, List<MultipartFile> images) throws IOException {
        List<String> imagePaths = new ArrayList<>();

        // Sanitize folder name
        String safeFolderName = product.getName().replaceAll("[^a-zA-Z0-9-_]", "_");
        Path productUploadPath = Paths.get(BASE_UPLOAD_DIR, safeFolderName);

        // Create directories if not exist
        if (!Files.exists(productUploadPath)) {
            Files.createDirectories(productUploadPath);
        }

        // Save each image
        for (MultipartFile image : images) {
            if (image.isEmpty()) continue; // Skip empty files

            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path filePath = productUploadPath.resolve(fileName);

            // Copy image to target directory, replace if exists
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Store relative path for frontend
            String webPath = "/uploads/" + safeFolderName + "/" + fileName;
            imagePaths.add(webPath);
        }

        product.setImagePaths(String.join(",", imagePaths));

        // Save product to DB
        return inventoryRepository.save(product);
    }

    /**
     * Get all products
     * @return List of Inventory
     */
    public List<Inventory> getAllProducts() {
        return inventoryRepository.findAll();
    }


    public Page<Inventory> getProducts(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return inventoryRepository.findAll(pageable);
    }

    /**
     * Get product by ID
     * @param id UUID of product
     * @return Inventory object
     */
    public Inventory getCheckoutProductById(UUID id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
    }



    public  void InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<Inventory> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }

        String[] keywords = query.trim().split("\\s+");
        List<Inventory> results = new ArrayList<>();

        for (String keyword : keywords) {
            results.addAll(inventoryRepository.searchByName(keyword));
        }

        // Remove duplicates if multiple keywords match same product
        return results.stream().distinct().toList();
    }
}
