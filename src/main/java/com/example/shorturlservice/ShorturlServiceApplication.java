package com.example.shorturlservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShorturlServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShorturlServiceApplication.class, args);
	}

}
