package com.example.fsalazar.springcloud.msvc.products;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan({"com.fsalazar.libs.msvc.commons.entities"})
@EnableJpaRepositories(basePackages = {"com.example.fsalazar.springcloud.msvc.products.repositories"})
public class MsvcProductsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcProductsApplication.class, args);
	}

}
