package com.qwerty.telemetry.mqtt_producer;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.springframework.beans.factory.annotation.Value;
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
	CommandLineRunner runner(
			MqttToKafkaBridge bridge,
			@Value("${app.mqtt.broker}") String broker,
			@Value("${app.mqtt.topicFilter:telemetry/#}") String topicFilter
	) {
		return args -> {
			System.out.println("[BOOT] mqttRunner started");
			System.out.println("[MQTT] connecting to " + broker);

			String clientId = "mqtt-producer-" + System.currentTimeMillis();

			MqttClient client = new MqttClient(broker, clientId, null);
			client.connect();

			client.subscribe(topicFilter, (topic, msg) -> {
				String payload = new String(msg.getPayload());
				bridge.handleMessage(topic, payload);
				System.out.println("[MQTT] " + topic + " => " + payload);
			});

			System.out.println("Subscribed to " + topicFilter);

			Thread.currentThread().join();
		};
	}
}