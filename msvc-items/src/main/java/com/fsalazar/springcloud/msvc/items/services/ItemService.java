package com.fsalazar.springcloud.msvc.items.services;

import java.util.List;
import java.util.Optional;

import com.fsalazar.libs.msvc.commons.entities.Product;
import com.fsalazar.springcloud.msvc.items.models.Item;

public interface ItemService {
    
    List<Item> findAll();
    
    Optional<Item> findById(Long id);

    Product save(Product product); // if the product already exits it just updates under the table

    Product update(Product product, Long id); // if the product already exits it just updates under the table

    void deleteById(Long id);



}
