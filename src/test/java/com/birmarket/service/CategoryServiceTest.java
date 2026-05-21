package com.birmarket.service;

import com.birmarket.dto.CategoryRequest;
import com.birmarket.dto.CategoryResponse;
import com.birmarket.entity.Category;
import com.birmarket.entity.Product;
import com.birmarket.exception.AlreadyExistsException;
import com.birmarket.exception.BadRequestException;
import com.birmarket.exception.NotFoundException;
import com.birmarket.mapper.CategoryMapper;
import com.birmarket.repository.CategoryRepository;
import com.birmarket.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setup() {

        category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setDescription("Electronic items");
        category.setActive(true);
        category.setProducts(new ArrayList<>());

        categoryResponse = new CategoryResponse();
        categoryResponse.setId(1L);
        categoryResponse.setName("Electronics");
        categoryResponse.setDescription("Electronic items");
        categoryResponse.setActive(true);
    }

    @Test
    void getAll_returns_active_only() {

        when(categoryRepository.findByActiveTrue())
                .thenReturn(List.of(category));

        when(categoryMapper.toResponse(any(Category.class)))
                .thenReturn(categoryResponse);

        List<CategoryResponse> result = categoryService.getAll(true);

        assertEquals(1, result.size());

        verify(categoryRepository).findByActiveTrue();
        verify(categoryRepository, never()).findAll();
    }

    @Test
    void getAll_returns_all_categories() {

        Category inactive = new Category();
        inactive.setId(2L);
        inactive.setName("Old");
        inactive.setActive(false);
        inactive.setProducts(new ArrayList<>());

        when(categoryRepository.findAll())
                .thenReturn(List.of(category, inactive));

        when(categoryMapper.toResponse(any(Category.class)))
                .thenReturn(categoryResponse);

        List<CategoryResponse> result = categoryService.getAll(false);

        assertEquals(2, result.size());
    }

    @Test
    void getById_returns_category() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(categoryMapper.toResponse(category))
                .thenReturn(categoryResponse);

        CategoryResponse result = categoryService.getById(1L);

        assertEquals("Electronics", result.getName());
        assertTrue(result.isActive());
    }

    @Test
    void getById_throws_when_not_found() {

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> categoryService.getById(99L)
        );
    }

    @Test
    void create_success() {

        CategoryRequest req = new CategoryRequest();
        req.setName("Books");
        req.setDescription("All books");

        Category saved = new Category();
        saved.setId(2L);
        saved.setName("Books");
        saved.setDescription("All books");
        saved.setActive(true);
        saved.setProducts(new ArrayList<>());

        CategoryResponse response = new CategoryResponse();
        response.setId(2L);
        response.setName("Books");
        response.setDescription("All books");
        response.setActive(true);

        when(categoryRepository.existsByName("Books"))
                .thenReturn(false);

        when(categoryMapper.toEntity(any(CategoryRequest.class)))
                .thenReturn(saved);

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(saved);

        when(categoryMapper.toResponse(any(Category.class)))
                .thenReturn(response);

        CategoryResponse result = categoryService.create(req);

        assertEquals("Books", result.getName());

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void create_fails_when_name_taken() {

        CategoryRequest req = new CategoryRequest();
        req.setName("Electronics");

        when(categoryRepository.existsByName("Electronics"))
                .thenReturn(true);

        assertThrows(
                AlreadyExistsException.class,
                () -> categoryService.create(req)
        );

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_changes_name() {

        CategoryRequest req = new CategoryRequest();
        req.setName("New Electronics");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.existsByName("New Electronics"))
                .thenReturn(false);

        when(categoryRepository.save(any()))
                .thenReturn(category);

        when(categoryMapper.toResponse(any(Category.class)))
                .thenReturn(categoryResponse);

        categoryService.update(1L, req);

        assertEquals("New Electronics", category.getName());
    }

    @Test
    void update_fails_when_new_name_is_taken() {

        CategoryRequest req = new CategoryRequest();
        req.setName("Taken Name");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.existsByName("Taken Name"))
                .thenReturn(true);

        assertThrows(
                AlreadyExistsException.class,
                () -> categoryService.update(1L, req)
        );
    }

    @Test
    void delete_works_when_no_active_products() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        categoryService.delete(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void delete_fails_when_category_has_active_products() {

        Product activeProduct = new Product();
        activeProduct.setActive(true);

        category.getProducts().add(activeProduct);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        assertThrows(
                BadRequestException.class,
                () -> categoryService.delete(1L)
        );

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deactivate_sets_active_false() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.save(any()))
                .thenReturn(category);

        when(categoryMapper.toResponse(any(Category.class)))
                .thenReturn(categoryResponse);

        categoryService.deactivate(1L);

        assertFalse(category.isActive());
    }

    @Test
    void activate_sets_active_true() {

        category.setActive(false);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.save(any()))
                .thenReturn(category);

        when(categoryMapper.toResponse(any(Category.class)))
                .thenReturn(categoryResponse);

        categoryService.activate(1L);

        assertTrue(category.isActive());
    }
}