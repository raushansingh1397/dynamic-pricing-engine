package com.example.dps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DpsApplication {

	public static void main(String[] args) {
		SpringApplication.run(DpsApplication.class, args);
	}

}
