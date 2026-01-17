package com.example.fsalazar.springcloud.msvc.products.services;

import java.util.List;
import java.util.Optional;

import com.example.fsalazar.springcloud.msvc.products.entities.Product;


public interface ProductService {

    List<Product> findAll();

    Optional<Product> findById(Long id);

    Product save(Product product); // if the product already exits it just updates under the table

    void deleteById(Long id);

}