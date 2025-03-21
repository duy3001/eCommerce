package com.project.shopapp.services;

import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.repositories.ProductImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductImageService implements IProductImageService{
    private final ProductImageRepository productImageRepository;

    @Override
    @Transactional
    public ProductImage deleteProductImage(Long productId)
            throws DataNotFoundException {
        Optional<ProductImage> productImage = productImageRepository.findById(productId);
        if( productImage == null) {
            throw new DataNotFoundException(String.format("Cannot find product image with id: %ld", productId));
        }
        productImageRepository.deleteById(productId);
        return null;
    }
}
