package com.birmarket.service.impl;

import com.birmarket.dto.CategoryRequest;
import com.birmarket.dto.CategoryResponse;
import com.birmarket.entity.Category;
import com.birmarket.exception.AlreadyExistsException;
import com.birmarket.exception.BadRequestException;
import com.birmarket.exception.NotFoundException;
import com.birmarket.mapper.CategoryMapper;
import com.birmarket.repository.CategoryRepository;
import com.birmarket.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

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
                .map(categoryMapper::toResponse)
                .toList();

        log.info("ActionLog.getAll.end");

        return response;
    }

    @Override
    public CategoryResponse getById(Long id) {

        log.info("ActionLog.getById.start");

        Category category = findById(id);

        CategoryResponse response =
                categoryMapper.toResponse(category);

        log.info("ActionLog.getById.end");

        return response;
    }

    @Override
    public CategoryResponse create(CategoryRequest req) {

        log.info("ActionLog.create.start");

        if (categoryRepository.existsByName(req.getName())) {
            throw new AlreadyExistsException(
                    "Category already exists: " + req.getName()
            );
        }

        Category category = categoryMapper.toEntity(req);

        Category saved = categoryRepository.save(category);

        CategoryResponse response =
                categoryMapper.toResponse(saved);

        log.info("ActionLog.create.end");

        return response;
    }

    @Transactional
    @Override
    public CategoryResponse update(Long id, CategoryRequest req) {

        log.info("ActionLog.update.start");

        Category category = findById(id);

        if (req.getName() != null
                && !req.getName().equals(category.getName())) {

            if (categoryRepository.existsByName(req.getName())) {
                throw new AlreadyExistsException(
                        "Category name taken: " + req.getName()
                );
            }

            category.setName(req.getName());
        }

        if (req.getDescription() != null) {
            category.setDescription(req.getDescription());
        }

        Category saved = categoryRepository.save(category);

        CategoryResponse response =
                categoryMapper.toResponse(saved);

        log.info("ActionLog.update.end");

        return response;
    }

    @Override
    public void delete(Long id) {

        log.info("ActionLog.delete.start");

        Category category = findById(id);

        long activeProducts = category.getProducts()
                .stream()
                .filter(p -> p.isActive())
                .count();

        if (activeProducts > 0) {
            throw new BadRequestException(
                    "Cannot delete category that has "
                            + activeProducts
                            + " active products"
            );
        }

        categoryRepository.delete(category);

        log.info("ActionLog.delete.end");
    }

    @Transactional
    @Override
    public CategoryResponse deactivate(Long id) {

        log.info("ActionLog.deactivate.start");

        Category category = findById(id);

        category.setActive(false);

        Category saved = categoryRepository.save(category);

        CategoryResponse response =
                categoryMapper.toResponse(saved);

        log.info("ActionLog.deactivate.end");

        return response;
    }

    @Transactional
    @Override
    public CategoryResponse activate(Long id) {

        log.info("ActionLog.activate.start");

        Category category = findById(id);

        category.setActive(true);

        Category saved = categoryRepository.save(category);

        CategoryResponse response =
                categoryMapper.toResponse(saved);

        log.info("ActionLog.activate.end");

        return response;
    }

    private Category findById(Long id) {

        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Category not found with id: " + id
                        ));
    }
}