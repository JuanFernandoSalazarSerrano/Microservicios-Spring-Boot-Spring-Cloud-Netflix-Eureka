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

        return this.client // this whole thing is going to gives us an instance of List<Item>

        .build() // create a new instance of the class (from the webclient bean)
        .get() // GET method
        .accept(MediaType.APPLICATION_JSON) // headers and stuff
        .retrieve() // now go fetch the data, this is the trigger to work with all the previous info
        .bodyToFlux(ProductDTO.class) // the body you get back in the response convert it to an instance of ProductDTO
        .map(eachProduct -> {
            Random random = new Random();
            return new Item(eachProduct, random.nextInt(10) + 1);
        }) // map eachproduct and convert it to an item
        .collectList()
        .block(); // blocks execution until the flux is complete
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