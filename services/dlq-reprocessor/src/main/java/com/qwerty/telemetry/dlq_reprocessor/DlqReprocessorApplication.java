package com.qwerty.telemetry.dlq_reprocessor;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

@SpringBootApplication
public class DlqReprocessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(DlqReprocessorApplication.class, args);
	}

}