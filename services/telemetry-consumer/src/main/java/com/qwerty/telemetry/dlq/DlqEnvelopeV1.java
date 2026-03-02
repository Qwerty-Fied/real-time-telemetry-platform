package com.qwerty.telemetry.consumer.dlq;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DlqEnvelopeV1 {
    private Meta meta;
    private Source source;
    private Raw raw;

    @Data @Builder
    public static class Meta {
        private int version;          // 1
        private String failedAt;      // ISO-8601 UTC
        private String errorType;     // VALIDATION_ERROR, JSON_PARSE_ERROR, PRODUCE_ERROR, UNKNOWN_ERROR
        private String errorMessage;
        private String consumer;      // telemetry-consumer
    }

    @Data @Builder
    public static class Source {
        private String topic;
        private int partition;
        private long offset;
        private Long timestamp;       // record.timestamp()
        private String key;
    }

    @Data @Builder
    public static class Raw {
        private String payload;
        private String contentType;   // application/json
    }
}