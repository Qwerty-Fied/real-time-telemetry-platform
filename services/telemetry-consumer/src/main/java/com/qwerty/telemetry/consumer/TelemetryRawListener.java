package com.qwerty.telemetry.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qwerty.telemetry.consumer.dlq.DlqEnvelopeV1;
import com.qwerty.telemetry.consumer.metrics.TelemetryMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryRawListener {

    private static final String CONSUMER_NAME = "telemetry-consumer";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final TelemetryMetrics metrics; // 메트릭 주입 추가

    @Value("${app.topics.clean}")
    private String cleanTopic;

    @Value("${app.topics.dlq}")
    private String dlqTopic;

    @Value("${app.kafka.send-timeout-ms:3000}")
    private long sendTimeoutMs;

    @KafkaListener(topics = "${app.topics.raw}", groupId = "telemetry-consumer")
    public void listen(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String key = record.key();
        String payload = record.value();

        int sourcePartition = record.partition();
        long sourceOffset = record.offset();
        long start = System.nanoTime();

        try {
            log.info("[RAW] key={} partition={} offset={} payload={}",
                    key, sourcePartition, sourceOffset, payload);

            JsonNode node = objectMapper.readTree(payload);

            JsonNode tempNode = node.get("temp");
            if (tempNode == null || tempNode.isNull() || !tempNode.isNumber()) {
                throw new IllegalArgumentException("temp is required and must be a number");
            }

            String deviceId = node.hasNonNull("device_id")
                    ? node.get("device_id").asText()
                    : (key != null ? key : "unknown");

            ObjectNode clean = objectMapper.createObjectNode();
            clean.put("deviceId", deviceId);
            clean.put("temperature", tempNode.asInt());
            clean.put("processedAt", Instant.now().toString());
            clean.put("sourcePartition", sourcePartition);
            clean.put("sourceOffset", sourceOffset);

            String cleanJson = objectMapper.writeValueAsString(clean);

            kafkaTemplate.send(cleanTopic, key, cleanJson)
                    .get(sendTimeoutMs, TimeUnit.MILLISECONDS);

            metrics.processed.increment();   // ✅ clean 성공 카운트
            ack.acknowledge();

        } catch (Exception e) {
            metrics.errors.increment();      // ✅ 처리 실패(=catch 진입) 카운트

            try {
                String errorType = classify(e);

                DlqEnvelopeV1 env = DlqEnvelopeV1.builder()
                        .meta(DlqEnvelopeV1.Meta.builder()
                                .version(1)
                                .failedAt(Instant.now().toString())
                                .errorType(errorType)
                                .errorMessage(safeMsg(e))
                                .consumer(CONSUMER_NAME)
                                .build())
                        .source(DlqEnvelopeV1.Source.builder()
                                .topic(record.topic())
                                .partition(record.partition())
                                .offset(record.offset())
                                .timestamp(record.timestamp())
                                .key(key)
                                .build())
                        .raw(DlqEnvelopeV1.Raw.builder()
                                .payload(payload)
                                .contentType("application/json")
                                .build())
                        .build();

                String dlqJson = objectMapper.writeValueAsString(env);

                log.error("Processing failed. send to DLQ. key={} partition={} offset={}",
                        key, sourcePartition, sourceOffset, e);

                kafkaTemplate.send(dlqTopic, key, dlqJson)
                        .get(sendTimeoutMs, TimeUnit.MILLISECONDS);

                metrics.dlq.increment();     // ✅ DLQ 성공 카운트
                ack.acknowledge();

            } catch (Exception dlqEx) {
                metrics.dlqProduceFail.increment();  // ✅ DLQ 발행 실패 카운트
                log.error("DLQ produce failed; will retry by not committing offset. key={} partition={} offset={}",
                        key, sourcePartition, sourceOffset, dlqEx);

                throw new RuntimeException("DLQ produce failed", dlqEx);
            }
        } finally {
            metrics.processing.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }

    private static String classify(Exception e) {
        if (e instanceof IllegalArgumentException) return "VALIDATION_ERROR";
        if (e.getClass().getName().contains("Json")) return "JSON_PARSE_ERROR";
        if (e.getClass().getName().contains("Timeout") || e.getClass().getName().contains("Kafka")) return "PRODUCE_ERROR";
        return "UNKNOWN_ERROR";
    }

    private static String safeMsg(Exception e) {
        String m = e.getMessage();
        return (m == null || m.isBlank()) ? e.getClass().getSimpleName() : m;
    }
}