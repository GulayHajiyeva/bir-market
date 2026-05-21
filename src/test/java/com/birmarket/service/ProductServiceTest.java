package com.birmarket.service;

import com.birmarket.dto.ProductRequest;
import com.birmarket.dto.ProductResponse;
import com.birmarket.entity.Category;
import com.birmarket.entity.Product;
import com.birmarket.entity.User;
import com.birmarket.enums.Role;
import com.birmarket.exception.ForbiddenException;
import com.birmarket.exception.NotFoundException;
import com.birmarket.mapper.ProductMapper;
import com.birmarket.repository.CategoryRepository;
import com.birmarket.repository.ProductRepository;
import com.birmarket.service.impl.ProductServiceImpl;
import com.birmarket.service.interfaces.StorageService;
import com.birmarket.util.SecurityHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private MockedStatic<SecurityHelper> securityHelperMock;

    private User seller;
    private User anotherSeller;
    private Category category;
    private Product product;

    @BeforeEach
    void setup() {

        seller = new User();
        seller.setId(1L);
        seller.setEmail("seller@test.com");
        seller.setRole(Role.SELLER);
        seller.setFullName("Test Seller");

        anotherSeller = new User();
        anotherSeller.setId(2L);
        anotherSeller.setEmail("other@test.com");
        anotherSeller.setRole(Role.SELLER);

        category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setActive(true);

        product = new Product();
        product.setId(1L);
        product.setName("Test Phone");
        product.setPrice(BigDecimal.valueOf(500));
        product.setStockQuantity(10);
        product.setActive(true);
        product.setCategory(category);
        product.setSeller(seller);

        securityHelperMock = mockStatic(SecurityHelper.class);

        securityHelperMock.when(SecurityHelper::getCurrentUser)
                .thenReturn(seller);
    }

    @AfterEach
    void teardown() {
        securityHelperMock.close();
    }

    @Test
    void createProduct_works() {

        ProductRequest req = new ProductRequest();
        req.setName("New Phone");
        req.setPrice(BigDecimal.valueOf(299));
        req.setStockQuantity(5);
        req.setCategoryId(1L);

        ProductResponse response = new ProductResponse();
        response.setId(1L);
        response.setName("Test Phone");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        when(productMapper.toResponse(any(Product.class)))
                .thenReturn(response);

        ProductResponse result = productService.createProduct(req);

        assertNotNull(result);

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_fails_when_category_not_found() {

        ProductRequest req = new ProductRequest();
        req.setName("Phone");
        req.setPrice(BigDecimal.valueOf(100));
        req.setCategoryId(99L);

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> productService.createProduct(req)
        );
    }

    @Test
    void deleteProduct_works_for_owner() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        productService.deleteProduct(1L);

        assertFalse(product.isActive());

        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_blocked_for_other_seller() {

        securityHelperMock.when(SecurityHelper::getCurrentUser)
                .thenReturn(anotherSeller);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        assertThrows(
                ForbiddenException.class,
                () -> productService.deleteProduct(1L)
        );

        verify(productRepository, never()).save(any());
    }

    @Test
    void getProduct_throws_when_not_found() {

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> productService.getProduct(999L)
        );
    }

    @Test
    void updateStock_changes_quantity() {

        ProductResponse response = new ProductResponse();
        response.setId(1L);
        response.setStockQuantity(50);

        when(productRepository.findByIdAndSeller(1L, seller))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        when(productMapper.toResponse(any(Product.class)))
                .thenReturn(response);

        ProductResponse result = productService.updateStock(1L, 50);

        assertNotNull(result);
        assertEquals(50, product.getStockQuantity());

        verify(productRepository).save(product);
    }
}