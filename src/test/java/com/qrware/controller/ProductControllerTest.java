package com.qrware.controller;

import com.qrware.domain.product.Category;
import com.qrware.domain.product.Product;
import com.qrware.exception.ResourceNotFoundException;
import com.qrware.repository.product.CategoryRepository;
import com.qrware.repository.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductController productController;

    @Test
    void getProductById_ShouldReturnProduct_WhenFound() {
        Long id = 1L;
        Product product = new Product();
        product.setId(id);
        product.setName("Test Product");
        
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        ResponseEntity<ProductController.ProductDTO> response = productController.getProductById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(id, response.getBody().id());
        assertEquals("Test Product", response.getBody().name());
    }

    @Test
    void getProductById_ShouldThrowException_WhenNotFound() {
        Long id = 1L;
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productController.getProductById(id));
    }

    @Test
    void createProduct_ShouldCreateAndReturnProduct() {
        ProductController.CreateProductRequest request = new ProductController.CreateProductRequest();
        request.setSku("SKU-001");
        request.setName("New Product");
        request.setPrice(BigDecimal.TEN);
        request.setCategoryId(1L);

        Category category = new Category();
        category.setId(1L);

        when(productRepository.existsBySku("SKU-001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        ResponseEntity<ProductController.ProductDTO> response = productController.createProduct(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("SKU-001", response.getBody().sku());
        assertEquals("New Product", response.getBody().name());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_ShouldReturnBadRequest_WhenSkuExists() {
        ProductController.CreateProductRequest request = new ProductController.CreateProductRequest();
        request.setSku("EXISTING-SKU");

        when(productRepository.existsBySku("EXISTING-SKU")).thenReturn(true);

        ResponseEntity<ProductController.ProductDTO> response = productController.createProduct(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_ShouldUpdateAndReturnProduct() {
        Long id = 1L;
        ProductController.UpdateProductRequest request = new ProductController.UpdateProductRequest();
        request.setName("Updated Name");

        Product existingProduct = new Product();
        existingProduct.setId(id);
        existingProduct.setName("Old Name");

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<ProductController.ProductDTO> response = productController.updateProduct(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Name", response.getBody().name());
        verify(productRepository).save(existingProduct);
    }

    @Test
    void deleteProduct_ShouldDeactivateProduct() {
        Long id = 1L;
        Product product = new Product();
        product.setId(id);
        product.setActive(true);

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Void> response = productController.deleteProduct(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(product.getActive());
        verify(productRepository).save(product);
    }
}
