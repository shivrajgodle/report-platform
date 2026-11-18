package com.shiv.export_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExportServiceApplication {
	public static void main(String[] args) {
		System.setProperty("java.awt.headless", "true"); // also covers running from an IDE
		SpringApplication.run(ExportServiceApplication.class, args);
	}
}