package com.gsm.service;

import com.gsm.dto.ProductDto;
import java.util.List;

/**
 * Service layer interface for managing Product-related business logic.
 * This interface defines the public contract for all operations that can be performed
 * on products, abstracting the implementation details from the controller layer.
 *
 * @author ThanhDX
 * @version 1.0.0
 */
public interface ProductService {
    /**
     * Retrieves a list of all products.
     *
     * @return A list of {@link ProductDto} objects.
     */
    List<ProductDto> findAll();

    /**
     * Finds a single product by its unique identifier.
     *
     * @param id The ID of the product to find.
     * @return The {@link ProductDto} corresponding to the given ID.
     * @throws com.gsm.exception.ResourceNotFoundException if no product is found with the given ID.
     */
    ProductDto findById(Long id);

    /**
     * Saves a new product or updates an existing one.
     * This method contains the core logic for creating/updating products and their variants.
     *
     * @param productDto The DTO containing the product data to be saved.
     * @return The saved {@link ProductDto}, including any database-generated values like IDs.
     */
    ProductDto save(ProductDto productDto);

    /**
     * Searches for products based on a keyword and a category name.
     *
     * @param keyword The keyword to search for in the product code and name. Can be null.
     * @param categoryName The category to filter by. Can be null.
     * @return A list of {@link ProductDto}s that match the criteria.
     */
    List<ProductDto> search(String keyword, String categoryName);

    /**
     * Deletes one or more products based on a list of IDs.
     *
     * @param ids A list of product IDs to be deleted.
     */
    void deleteByIds(List<Long> ids);

}