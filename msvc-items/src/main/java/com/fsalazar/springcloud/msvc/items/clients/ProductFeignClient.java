package com.fsalazar.springcloud.msvc.items.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fsalazar.springcloud.msvc.items.models.ProductDTO;

// this is just another controller but instead of use webclient it uses feign
@FeignClient(name = "msvc-products")
public interface ProductFeignClient {

    @GetMapping
    List<ProductDTO> findAll();

    @GetMapping("/{id}")
    public ProductDTO details(@PathVariable Long id);

    @PostMapping
    public ProductDTO create(@RequestBody ProductDTO productDTO);

    @PutMapping("/{id}")
    public ProductDTO update(@RequestBody ProductDTO productDTO, @PathVariable Long id);

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id);
}
