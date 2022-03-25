package com.ec.kruger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KrugecorpApplication {

	public static void main(String[] args) {
		SpringApplication.run(KrugecorpApplication.class, args);
	}
	

}
