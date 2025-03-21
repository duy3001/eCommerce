package com.project.shopapp.services;

import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.ProductImage;

public interface IProductImageService {
    public ProductImage deleteProductImage(Long productId) throws DataNotFoundException;
}
