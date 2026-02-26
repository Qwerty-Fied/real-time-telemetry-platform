package com.qwerty.telemetry.mqtt_producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MqttToKafkaBridge {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public MqttToKafkaBridge(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void handleMessage(String topic, String payload) {
        String key = extractDeviceId(payload);
        kafkaTemplate.send("telemetry.raw", key, payload);
    }

    private String extractDeviceId(String payload) {
        return "device1"; // 일단 하드코딩 → 나중에 JSON 파싱
    }
}