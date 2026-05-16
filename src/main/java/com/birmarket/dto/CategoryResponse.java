package com.birmarket.dto;

import com.birmarket.entity.Category;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private boolean active;
    private long productCount;

    public static CategoryResponse fromEntity(Category c) {
        CategoryResponse resp = new CategoryResponse();
        resp.setId(c.getId());
        resp.setName(c.getName());
        resp.setDescription(c.getDescription());
        resp.setActive(c.isActive());
        resp.setProductCount(c.getProducts().stream().filter(p -> p.isActive()).count());
        return resp;
    }
}
