package com.fsalazar.springcloud.msvc.items.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fsalazar.springcloud.msvc.items.models.Item;
import com.fsalazar.springcloud.msvc.items.models.ProductDTO;


@Primary
@Service
public class itemServiceWebClient implements ItemService {

    private final WebClient.Builder client;
    
    public itemServiceWebClient(Builder client) {
        this.client = client;
    }

    @Override
    public List<Item> findAll() {

        return this.client
        .build()
        .get()
        .uri("http://msvc-products")
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToFlux(ProductDTO.class)
        .map(eachProduct -> {
            Random random = new Random();
            return new Item(eachProduct, random.nextInt(10) + 1);
        })
        .collectList()
        .block()
        ;
    }

    @Override
    public Optional<Item> findById(Long id) {
        Map<String, Long> params = new HashMap<>();
        params.put("id", id);
        try {
            return Optional.of(client.build().get().uri("/{id}", params)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(ProductDTO.class)
                    .map(product -> new Item(product, new Random().nextInt(10) + 1))
                    .block());
        } catch (WebClientResponseException e) {
            return Optional.empty();
        }
    }
}