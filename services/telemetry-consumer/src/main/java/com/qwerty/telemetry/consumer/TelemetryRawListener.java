package com.qwerty.telemetry.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryRawListener {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.topics.clean}")
    private String cleanTopic;

    @Value("${app.topics.dlq}")
    private String dlqTopic;

    @KafkaListener(
            topics = "${app.topics.raw}",
            groupId = "telemetry-consumer"
    )
    public void listen(
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            String payload
    ) {

        try {
            log.info("[RAW] key={} payload={}", key, payload);

            // TODO: JSON 파싱 + 검증
            kafkaTemplate.send(cleanTopic, key, payload);

        } catch (Exception e) {
            log.error("Processing failed", e);
            kafkaTemplate.send(dlqTopic, key, payload);
        }
    }
}