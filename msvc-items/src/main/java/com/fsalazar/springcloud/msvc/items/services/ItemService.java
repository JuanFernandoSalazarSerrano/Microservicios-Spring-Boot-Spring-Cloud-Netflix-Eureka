package com.fsalazar.springcloud.msvc.items.services;

import java.util.List;
import java.util.Optional;

import com.fsalazar.springcloud.msvc.items.models.Item;
import com.fsalazar.springcloud.msvc.items.models.ProductDTO;

public interface ItemService {
    
    List<Item> findAll();
    
    Optional<Item> findById(Long id);

    ProductDTO save(ProductDTO product); // if the product already exits it just updates under the table

    ProductDTO update(ProductDTO product, Long id); // if the product already exits it just updates under the table

    void deleteById(Long id);



}
