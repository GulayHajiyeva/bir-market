package com.birmarket.service.impl;

import com.birmarket.dto.ProductRequest;
import com.birmarket.dto.ProductResponse;
import com.birmarket.entity.Category;
import com.birmarket.entity.Product;
import com.birmarket.entity.User;
import com.birmarket.exception.BadRequestException;
import com.birmarket.exception.ForbiddenException;
import com.birmarket.exception.NotFoundException;
import com.birmarket.repository.CategoryRepository;
import com.birmarket.repository.ProductRepository;
import com.birmarket.service.interfaces.ProductService;
import com.birmarket.service.interfaces.StorageService;
import com.birmarket.util.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StorageService storageService;

    @Value("${minio.bucket}")
    private String minioBucket;

    @Override
    public Page<ProductResponse> searchProducts(Long categoryId, BigDecimal minPrice,
                                                BigDecimal maxPrice, String keyword, Pageable pageable) {
        log.info("ActionLog.searchProducts.start");
        Page<ProductResponse> response = productRepository
                .searchProducts(categoryId, minPrice, maxPrice, keyword, pageable)
                .map(ProductResponse::fromEntity);
        log.info("ActionLog.searchProducts.end");
        return response;
    }

    @Override
    public ProductResponse getProduct(Long id) {
        log.info("ActionLog.getProduct.start");
        Product p = findActiveProduct(id);
        ProductResponse response = ProductResponse.fromEntity(p);
        log.info("ActionLog.getProduct.end");
        return response;
    }

    @Override
    public ProductResponse createProduct(ProductRequest req) {
        log.info("ActionLog.createProduct.start");

        User seller = SecurityHelper.getCurrentUser();

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: " + req.getCategoryId()));

        if (!category.isActive()) {
            throw new BadRequestException("Cannot list product in an inactive category");
        }

        Product product = new Product();
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setStockQuantity(req.getStockQuantity() != null ? req.getStockQuantity() : 0);
        product.setImageUrl(req.getImageUrl());
        product.setCategory(category);
        product.setSeller(seller);

        Product saved = productRepository.save(product);
        ProductResponse response = ProductResponse.fromEntity(saved);

        log.info("ActionLog.createProduct.end");
        return response;
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest req) {
        log.info("ActionLog.updateProduct.start");

        Product product = findProductById(id);
        checkOwnership(product);

        if (req.getName() != null) product.setName(req.getName());
        if (req.getDescription() != null) product.setDescription(req.getDescription());
        if (req.getPrice() != null) product.setPrice(req.getPrice());
        if (req.getStockQuantity() != null) product.setStockQuantity(req.getStockQuantity());
        if (req.getImageUrl() != null) product.setImageUrl(req.getImageUrl());

        if (req.getCategoryId() != null) {
            Category newCategory = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found: " + req.getCategoryId()));
            product.setCategory(newCategory);
        }

        ProductResponse response = ProductResponse.fromEntity(productRepository.save(product));

        log.info("ActionLog.updateProduct.end");
        return response;
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("ActionLog.deleteProduct.start");

        Product product = findProductById(id);
        checkOwnership(product);
        product.setActive(false);
        productRepository.save(product);

        log.info("ActionLog.deleteProduct.end");
    }

    @Override
    @Transactional
    public ProductResponse updateStock(Long id, int quantity) {
        log.info("ActionLog.updateStock.start");

        User currentUser = SecurityHelper.getCurrentUser();

        Product product = productRepository
                .findByIdAndSeller(id, currentUser)
                .orElseThrow(() -> new NotFoundException("Product not found or access denied"));

        product.setStockQuantity(quantity);
        ProductResponse response = ProductResponse.fromEntity(productRepository.save(product));

        log.info("ActionLog.updateStock.end");
        return response;
    }

    @Override
    public Page<ProductResponse> getMyProducts(Pageable pageable) {
        log.info("ActionLog.getMyProducts.start");
        User seller = SecurityHelper.getCurrentUser();
        Page<ProductResponse> response = productRepository.findBySellerAndActiveTrue(seller, pageable)
                .map(ProductResponse::fromEntity);
        log.info("ActionLog.getMyProducts.end");
        return response;
    }

    @Override
    @Transactional
    public ProductResponse uploadProductImage(Long productId, MultipartFile file) {
        log.info("ActionLog.uploadProductImage.start");

        User seller = SecurityHelper.getCurrentUser();

        Product product = productRepository.findByIdAndSeller(productId, seller)
                .orElseThrow(() -> new NotFoundException("Product not found or access denied"));

        String objectName = "products/" + productId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        String imageUrl = storageService.uploadFile(minioBucket, objectName, file);

        product.setImageUrl(imageUrl);
        ProductResponse response = ProductResponse.fromEntity(productRepository.save(product));

        log.info("ActionLog.uploadProductImage.end");
        return response;
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    private Product findActiveProduct(Long id) {
        Product p = findProductById(id);
        if (!p.isActive()) {
            throw new NotFoundException("Product not found: " + id);
        }
        return p;
    }

    private void checkOwnership(Product product) {
        User currentUser = SecurityHelper.getCurrentUser();
        if (!product.getSeller().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only modify your own products");
        }
    }
}
