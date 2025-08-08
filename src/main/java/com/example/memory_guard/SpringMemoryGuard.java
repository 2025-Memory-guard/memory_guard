package com.example.memory_guard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SpringMemoryGuard {

	public static void main(String[] args) {
		SpringApplication.run(SpringMemoryGuard.class, args);
	}

}
