package com.birmarket.service.impl;

import com.birmarket.dto.CategoryRequest;
import com.birmarket.dto.CategoryResponse;
import com.birmarket.entity.Category;
import com.birmarket.exception.AlreadyExistsException;
import com.birmarket.exception.BadRequestException;
import com.birmarket.exception.NotFoundException;
import com.birmarket.repository.CategoryRepository;
import com.birmarket.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getAll(boolean activeOnly) {
        log.info("ActionLog.getAll.start");

        List<Category> categories;
        if (activeOnly) {
            categories = categoryRepository.findByActiveTrue();
        } else {
            categories = categoryRepository.findAll();
        }

        List<CategoryResponse> response = categories.stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());

        log.info("ActionLog.getAll.end");
        return response;
    }

    @Override
    public CategoryResponse getById(Long id) {
        log.info("ActionLog.getById.start");
        CategoryResponse response = CategoryResponse.fromEntity(findById(id));
        log.info("ActionLog.getById.end");
        return response;
    }

    @Override
    public CategoryResponse create(CategoryRequest req) {
        log.info("ActionLog.create.start");

        if (categoryRepository.existsByName(req.getName())) {
            throw new AlreadyExistsException("Category already exists: " + req.getName());
        }

        Category category = new Category();
        category.setName(req.getName());
        category.setDescription(req.getDescription());

        Category saved = categoryRepository.save(category);
        CategoryResponse response = CategoryResponse.fromEntity(saved);

        log.info("ActionLog.create.end");
        return response;
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest req) {
        log.info("ActionLog.update.start");

        Category category = findById(id);

        if (req.getName() != null && !req.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(req.getName())) {
                throw new AlreadyExistsException("Category name taken: " + req.getName());
            }
            category.setName(req.getName());
        }
        if (req.getDescription() != null) {
            category.setDescription(req.getDescription());
        }

        Category saved = categoryRepository.save(category);
        CategoryResponse response = CategoryResponse.fromEntity(saved);

        log.info("ActionLog.update.end");
        return response;
    }

    @Override
    public void delete(Long id) {
        log.info("ActionLog.delete.start");

        Category category = findById(id);

        long activeProducts = category.getProducts().stream().filter(p -> p.isActive()).count();
        if (activeProducts > 0) {
            throw new BadRequestException("Cannot delete category that has " + activeProducts + " active products");
        }

        categoryRepository.delete(category);

        log.info("ActionLog.delete.end");
    }

    @Override
    public CategoryResponse deactivate(Long id) {
        log.info("ActionLog.deactivate.start");

        Category category = findById(id);
        category.setActive(false);
        CategoryResponse response = CategoryResponse.fromEntity(categoryRepository.save(category));

        log.info("ActionLog.deactivate.end");
        return response;
    }

    @Override
    public CategoryResponse activate(Long id) {
        log.info("ActionLog.activate.start");

        Category category = findById(id);
        category.setActive(true);
        CategoryResponse response = CategoryResponse.fromEntity(categoryRepository.save(category));

        log.info("ActionLog.activate.end");
        return response;
    }

    private Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
    }
}
