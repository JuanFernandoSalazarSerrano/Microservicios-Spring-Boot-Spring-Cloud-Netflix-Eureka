package com.fsalazar.springcloud.msvc.items.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.fsalazar.springcloud.msvc.items.models.ProductDTO;

@FeignClient(name = "msvc-products")

public interface ProductFeignClient {

    @GetMapping
    List<ProductDTO> findAll();

    @GetMapping("/{id}")
    public ProductDTO details(@PathVariable Long id);

}
