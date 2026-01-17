package com.example.fsalazar.springcloud.msvc.products.controllers;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.fsalazar.springcloud.msvc.products.entities.Product;
import com.example.fsalazar.springcloud.msvc.products.services.ProductService;


@RestController
public class ProductController {

    private final ProductService service;


    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(this.service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable Long id) throws InterruptedException {

         // just for testing resilience4j
        if(id.equals(33L)){
            throw new IllegalStateException("No product found man idk");
        }

        if(id.equals(7L)){
            TimeUnit.SECONDS.sleep(3L);
        }

        Optional<Product> productOptional = service.findById(id);
        if(productOptional.isPresent()){
            return ResponseEntity.ok(productOptional.orElseThrow());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.save(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modify(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        product.setName(product.getName());
        product.setPrice(product.getPrice());
        product.setCreateAt(product.getCreateAt());
        return ResponseEntity.ok(this.service.save(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        this.service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}