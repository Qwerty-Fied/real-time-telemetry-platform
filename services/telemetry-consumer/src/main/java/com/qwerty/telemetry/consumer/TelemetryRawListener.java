package com.qwerty.telemetry.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryRawListener {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.topics.clean}")
    private String cleanTopic;

    @Value("${app.topics.dlq}")
    private String dlqTopic;

    @KafkaListener(topics = "${app.topics.raw}", groupId = "telemetry-consumer")
    public void listen(ConsumerRecord<String, String> record) {

        String key = record.key();          // null 가능
        String payload = record.value();
        int sourcePartition = record.partition();
        long sourceOffset = record.offset();

        try {
            log.info("[RAW] key={} partition={} offset={} payload={}",
                    key, sourcePartition, sourceOffset, payload);

            // 1) JSON 파싱
            JsonNode node = objectMapper.readTree(payload);

            // 2) 필수 필드 검증
            JsonNode tempNode = node.get("temp");
            if (tempNode == null || tempNode.isNull() || !tempNode.isNumber()) {
                throw new IllegalArgumentException("temp is required and must be a number");
            }

            String deviceId = node.hasNonNull("device_id")
                    ? node.get("device_id").asText()
                    : (key != null ? key : "unknown");

            // 3) clean payload 생성
            ObjectNode clean = objectMapper.createObjectNode();
            clean.put("deviceId", deviceId);
            clean.put("temperature", tempNode.asInt());
            clean.put("processedAt", Instant.now().toString());
            clean.put("sourcePartition", sourcePartition);
            clean.put("sourceOffset", sourceOffset);

            // 4) clean 발행
            kafkaTemplate.send(cleanTopic, key, objectMapper.writeValueAsString(clean));

        } catch (Exception e) {
            log.error("Processing failed. send to DLQ. key={} payload={}", key, payload, e);
            kafkaTemplate.send(dlqTopic, key, payload);
        }
    }
}