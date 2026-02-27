package com.qwerty.telemetry.mqtt_producer;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MqttProducerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MqttProducerApplication.class, args);
	}

	@Bean
	CommandLineRunner runner(MqttToKafkaBridge bridge) {
		return args -> {
			System.out.println("[BOOT] mqttRunner started");
			String broker = "tcp://localhost:1883";
			String clientId = "mqtt-producer-" + System.currentTimeMillis();
			String topicFilter = "telemetry/#";

			MqttClient client = new MqttClient(broker, clientId, null);
			client.connect();

			client.subscribe(topicFilter, (topic, msg) -> {
				String payload = new String(msg.getPayload());
				bridge.handleMessage(topic, payload);
				System.out.println("[MQTT] " + topic + " => " + payload);
			});

			System.out.println("Subscribed to " + topicFilter);

			// keep process alive
			Thread.currentThread().join();
		};
	}
}