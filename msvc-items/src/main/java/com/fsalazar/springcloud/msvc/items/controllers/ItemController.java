package com.fsalazar.springcloud.msvc.items.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.fsalazar.springcloud.msvc.items.models.Item;
import com.fsalazar.springcloud.msvc.items.models.ProductDTO;
import com.fsalazar.springcloud.msvc.items.services.ItemService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class ItemController {

    private final ItemService service;
    
    private final CircuitBreakerFactory circuitBreakerFactory;

    private final Logger logger = LoggerFactory.getLogger(ItemController.class); 


    public ItemController(@Qualifier("itemServiceWebClient") ItemService service, CircuitBreakerFactory circuitBreakerFactory) {
        this.service = service;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @GetMapping
    public List<Item> list(@RequestParam(name = "name", required = false) String name,
     @RequestParam(name = "X-Request-red", required = false) String color) {
        System.out.println("IM HERE YOU CAN SEE HERE THE PARAMS TO THIS ENDPOINT"+ name + color);
        return service.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable Long id) {
        
        Optional<Item> itemOptional = circuitBreakerFactory.create("items").run(
            () -> service.findById(id),
             
            exception -> {
            logger.error("Error fetching item with id: {}", id, exception);
            
            ProductDTO product = new ProductDTO();
            product.setId(2828L);
            product.setCreateAt(LocalDate.now());
            product.setName("pizza");
            product.setPrice(500.00);

            Item item = new Item(product, 28);
            return Optional.of(item);
            });

        if(itemOptional.isPresent()){
            return ResponseEntity.ok(itemOptional.get());
        }
        return ResponseEntity.status(404)
            .body(Collections.singletonMap(
                "message",
            "The product does not exist in the msvc-products microservice"));
    }   
}