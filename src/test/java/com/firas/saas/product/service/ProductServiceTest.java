package com.firas.saas.product.service;

import com.firas.saas.product.dto.*;
import com.firas.saas.product.entity.Category;
import com.firas.saas.product.entity.Product;
import com.firas.saas.product.entity.ProductVariant;
import com.firas.saas.product.repository.CategoryRepository;
import com.firas.saas.product.repository.ProductRepository;
import com.firas.saas.product.repository.ProductVariantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Nested
    @DisplayName("createProduct method")
    class CreateProductTests {

        @Test
        @DisplayName("should successfully create product with variants")
        void createProduct_Success() {
            // Arrange
            Long tenantId = 1L;
            ProductVariantRequest variantRequest = new ProductVariantRequest("Default", "SKU-001", new BigDecimal("10.00"), 100);
            ProductRequest request = new ProductRequest("Test Product", "test-product", "Description", null, Collections.singletonList(variantRequest));

            when(productRepository.existsBySlugAndTenantId(request.getSlug(), tenantId)).thenReturn(false);
            when(productVariantRepository.existsBySkuAndTenantId("SKU-001", tenantId)).thenReturn(false);

            Product savedProduct = Product.builder()
                    .name(request.getName())
                    .slug(request.getSlug())
                    .description(request.getDescription())
                    .active(true)
                    .build();
            savedProduct.setId(100L);
            savedProduct.setTenantId(tenantId);
            
            ProductVariant variant = ProductVariant.builder()
                    .name(variantRequest.getName())
                    .sku(variantRequest.getSku())
                    .price(variantRequest.getPrice())
                    .stockLevel(variantRequest.getStockLevel())
                    .build();
            variant.setId(200L);
            savedProduct.addVariant(variant);

            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            // Act
            ProductResponse response = productService.createProduct(request, tenantId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(100L);
            assertThat(response.getVariants()).hasSize(1);
            assertThat(response.getVariants().get(0).getSku()).isEqualTo("SKU-001");
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("should throw exception when SKU is duplicate for tenant")
        void createProduct_DuplicateSku() {
            // Arrange
            Long tenantId = 1L;
            ProductVariantRequest variantRequest = new ProductVariantRequest("Default", "EXISTING-SKU", new BigDecimal("10.00"), 100);
            ProductRequest request = new ProductRequest("Test Product", "test-product", "Description", null, Collections.singletonList(variantRequest));

            when(productRepository.existsBySlugAndTenantId(request.getSlug(), tenantId)).thenReturn(false);
            when(productVariantRepository.existsBySkuAndTenantId("EXISTING-SKU", tenantId)).thenReturn(true);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> productService.createProduct(request, tenantId));
            assertThat(exception.getMessage()).contains("SKU EXISTING-SKU already exists");
            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("createCategory method")
    class CreateCategoryTests {

        @Test
        @DisplayName("should create category successfully")
        void createCategory_Success() {
            // Arrange
            Long tenantId = 1L;
            CategoryRequest request = new CategoryRequest("Electronics", "electronics", "Desc");
            when(categoryRepository.existsByNameAndTenantId(request.getName(), tenantId)).thenReturn(false);

            Category savedCategory = Category.builder()
                    .name(request.getName())
                    .slug(request.getSlug())
                    .description(request.getDescription())
                    .build();
            savedCategory.setId(10L);
            savedCategory.setTenantId(tenantId);

            when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

            // Act
            CategoryResponse response = productService.createCategory(request, tenantId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(10L);
            assertThat(response.getName()).isEqualTo("Electronics");
        }
    }
}
