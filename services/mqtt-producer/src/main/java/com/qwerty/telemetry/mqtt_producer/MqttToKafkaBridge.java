package com.qwerty.telemetry.mqtt_producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MqttToKafkaBridge {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public MqttToKafkaBridge(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void handleMessage(String topic, String payload) {
        String key = extractDeviceId(payload);
        kafkaTemplate.send("telemetry.raw", key, payload)
        .whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("[KAFKA] send FAIL topic=telemetry.raw key={} payload={}", key, payload, ex);
            } else {
                log.info("[KAFKA] send OK topic=telemetry.raw partition={} offset={}",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    private String extractDeviceId(String payload) {
        return "device1"; // 일단 하드코딩 → 나중에 JSON 파싱
    }
}