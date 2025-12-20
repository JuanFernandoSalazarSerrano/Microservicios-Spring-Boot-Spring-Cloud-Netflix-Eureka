package com.example.fsalazar.springcloud.msvc.products.services;


import java.util.List;
import java.util.Optional;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fsalazar.springcloud.msvc.products.entities.Product;
import com.example.fsalazar.springcloud.msvc.products.repositories.ProductRepository;


@Service
public class ProductServiceImpl implements ProductService {

    final private ProductRepository repository;

    final private Environment environment;

    public ProductServiceImpl(ProductRepository repository, Environment environment) {
        this.repository = repository;
        this.environment = environment;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return ((List<Product>) repository.
        findAll())// call all the products from mysql
        .stream() // stream the list of products
        .map(product -> { // for each product set the port
            product.setPort(Integer.parseInt(environment.getProperty("local.server.port")));
            return product;
        })
        .toList(); // gathers all transformed Products into a List<Product>
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return repository.findById(id).map(product -> {
            product.setPort(Integer.parseInt(environment.getProperty("local.server.port")));
            return product;
        });
    }

}
