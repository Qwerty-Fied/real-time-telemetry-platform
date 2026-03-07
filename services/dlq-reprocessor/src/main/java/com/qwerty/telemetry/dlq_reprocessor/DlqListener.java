package com.qwerty.telemetry.dlq_reprocessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DlqListener {

    private static final Logger log = LoggerFactory.getLogger(DlqListener.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    public DlqListener(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "telemetry.dlq", groupId = "dlq-reprocessor")
    public void listen(DlqMessage message) {
        if (message == null || message.getMeta() == null || message.getSource() == null || message.getRaw() == null) {
            log.warn("[DLQ-REPROCESSOR] invalid DLQ message structure. discard.");
            return;
        }

        String errorType = message.getMeta().getErrorType();
        String targetTopic = message.getSource().getTopic();
        String key = message.getSource().getKey();
        String rawPayload = message.getRaw().getPayload();

        log.info("[DLQ-REPROCESSOR] received DLQ message errorType={} sourceTopic={} key={} offset={}",
                errorType,
                targetTopic,
                key,
                message.getSource().getOffset());

        if (!"PRODUCE_ERROR".equals(errorType)) {
            log.info("[DLQ-REPROCESSOR] discard non-retriable message errorType={} key={}", errorType, key);
            return;
        }

        try {
            kafkaTemplate.send(targetTopic, key, rawPayload).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("[DLQ-REPROCESSOR] requeue FAIL targetTopic={} key={} payload={}",
                            targetTopic, key, rawPayload, ex);
                } else {
                    log.info("[DLQ-REPROCESSOR] requeue OK targetTopic={} partition={} offset={} key={}",
                            targetTopic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            key);
                }
            });
        } catch (Exception e) {
            log.error("[DLQ-REPROCESSOR] unexpected requeue error targetTopic={} key={}", targetTopic, key, e);
        }
    }
}