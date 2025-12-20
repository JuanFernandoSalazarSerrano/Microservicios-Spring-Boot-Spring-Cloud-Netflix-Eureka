package com.fsalazar.springcloud.msvc.items.services;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fsalazar.springcloud.msvc.items.clients.ProductFeignClient;
import com.fsalazar.springcloud.msvc.items.models.Item;
import com.fsalazar.springcloud.msvc.items.models.ProductDTO;

@Service
public class ItemServiceFeign implements ItemService {

    private final ProductFeignClient client;

    ItemServiceFeign(ProductFeignClient client){
        this.client = client;
    }

    @Override
    public List<Item> findAll() {
        return client.findAll().stream().map(eachProduct -> {
            Random random = new Random();
            return new Item(eachProduct, random.nextInt(10) + 1);
        }).collect(Collectors.toList());
    }

    @Override
    public Optional<Item> findById(Long id) {
        ProductDTO product = client.details(id);
        if (product == null) {
            return Optional.empty();
        }
        else{
            return Optional.of(new Item(product, new Random().nextInt(10) + 1));
        }
    }
}
