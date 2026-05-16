package com.birmarket.service.interfaces;

import com.birmarket.dto.CategoryRequest;
import com.birmarket.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAll(boolean activeOnly);
    CategoryResponse getById(Long id);
    CategoryResponse create(CategoryRequest req);
    CategoryResponse update(Long id, CategoryRequest req);
    void delete(Long id);
    CategoryResponse deactivate(Long id);
    CategoryResponse activate(Long id);
}
