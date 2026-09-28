package org.example.orderservice.models.services.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.clients.ProductClient;
import org.example.orderservice.exceptions.ProductNotFoundException;
import org.example.orderservice.exceptions.ProductServiceException;
import org.example.orderservice.models.dto.responses.ProductResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductGatewayService {

    private final ProductClient productClient;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductByIdFallback")
    public ProductResponse getProductById(Long productId) {
        ProductResponse product = productClient.getProductById(productId);
        if (product == null) {
            throw new ProductNotFoundException(productId);
        }
        return product;
    }

    public ProductResponse getProductByIdFallback(Long productId, Exception exception) {
        if (exception instanceof ProductNotFoundException) {
            throw (ProductNotFoundException) exception;
        }
        throw new ProductServiceException("Product service is unavailable", exception);
    }
}
