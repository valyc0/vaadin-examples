package io.bootify.my_app.service;

import io.bootify.my_app.domain.Product;
import io.bootify.my_app.repos.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(final ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Product> search(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAll(pageable);
        }
        return productRepository.searchProducts(searchTerm.trim(), pageable);
    }

    public Page<Product> findByCategory(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable);
    }

    public Page<Product> searchByCategory(String category, String name, Pageable pageable) {
        if (name == null || name.trim().isEmpty()) {
            return findByCategory(category, pageable);
        }
        return productRepository.findByCategoryAndNameContainingIgnoreCase(category, name.trim(), pageable);
    }

    @Transactional
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    public Product findById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public long count() {
        return productRepository.count();
    }
}
