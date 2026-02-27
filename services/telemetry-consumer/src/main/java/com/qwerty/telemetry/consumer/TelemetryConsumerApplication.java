package com.qwerty.telemetry.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class TelemetryConsumerApplication {
	public static void main(String[] args) {
		SpringApplication.run(TelemetryConsumerApplication.class, args);
	}
}