package com.example.dotdot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class DotdotApplication {

	public static void main(String[] args) {
		SpringApplication.run(DotdotApplication.class, args);
	}

}
