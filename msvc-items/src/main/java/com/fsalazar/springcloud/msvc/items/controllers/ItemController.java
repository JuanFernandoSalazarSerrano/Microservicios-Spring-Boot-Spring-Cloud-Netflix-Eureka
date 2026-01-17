package com.fsalazar.springcloud.msvc.items.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.fsalazar.springcloud.msvc.items.models.Item;
import com.fsalazar.springcloud.msvc.items.models.ProductDTO;
import com.fsalazar.springcloud.msvc.items.services.ItemService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;


@RefreshScope
@RestController
public class ItemController {

    private final ItemService service;
    
    private final CircuitBreakerFactory circuitBreakerFactory;

    private final Logger logger = LoggerFactory.getLogger(ItemController.class); 

    @Value("${configuration.text}")
    private String text;

    @Autowired
    private Environment env;

    public ItemController(@Qualifier("itemServiceWebClient") ItemService service, CircuitBreakerFactory circuitBreakerFactory) {
        this.service = service;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @GetMapping("/fetch-configs")
    public ResponseEntity<?> fetchConfigs(@Value("${server.port}") String port) {
        Map<String, String> json = new HashMap<>();
        json.put("port", port);
        json.put("text", text);

        if(env.getActiveProfiles().length > 0 && env.getActiveProfiles()[0].equals("dev")){
            json.put("port", port);
            json.put("text", text);
            json.put("author.email", env.getProperty("configuration.author.email"));
        }

        return ResponseEntity.ok(json);
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
            () -> service.findById(id) /*,
             
            exception -> {
            logger.error("Error fetching item with id: {}", id, exception);
            
            ProductDTO product = new ProductDTO();
            product.setId(2828L);
            product.setCreateAt(LocalDate.now());
            product.setName("pizza");
            product.setPrice(500.00);

            Item item = new Item(product, 28);
            return Optional.of(item);
            }*/ );

        if(itemOptional.isPresent()){
            return ResponseEntity.ok(itemOptional.get());
        }
        return ResponseEntity.status(404)
            .body(Collections.singletonMap(
                "message",
            "The product does not exist in the msvc-products microservice"));
    }   

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDTO create(@RequestBody ProductDTO productDTO) {
        return service.save(productDTO);        
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductDTO putMethodName(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        return service.update(productDTO, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        service.deleteById(id);
    }
}