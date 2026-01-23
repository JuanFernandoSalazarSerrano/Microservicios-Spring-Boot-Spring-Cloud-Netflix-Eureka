package com.example.fsalazar.springcloud.msvc.products.repositories;

import org.springframework.data.repository.CrudRepository;

import com.fsalazar.libs.msvc.commons.entities.Product;


public interface ProductRepository extends CrudRepository<Product, Long>{

}