package com.landmark.hlss_java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class HlssJavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(HlssJavaApplication.class, args);

	}

}
