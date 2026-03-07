package com.qwerty.telemetry.dlq_reprocessor;

public class DlqMessage {

    private Meta meta;
    private Source source;
    private Raw raw;

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Raw getRaw() {
        return raw;
    }

    public void setRaw(Raw raw) {
        this.raw = raw;
    }

    public static class Meta {
        private Integer version;
        private String failedAt;
        private String errorType;
        private String errorMessage;
        private String consumer;

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        public String getFailedAt() {
            return failedAt;
        }

        public void setFailedAt(String failedAt) {
            this.failedAt = failedAt;
        }

        public String getErrorType() {
            return errorType;
        }

        public void setErrorType(String errorType) {
            this.errorType = errorType;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getConsumer() {
            return consumer;
        }

        public void setConsumer(String consumer) {
            this.consumer = consumer;
        }
    }

    public static class Source {
        private String topic;
        private Integer partition;
        private Long offset;
        private Long timestamp;
        private String key;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public Integer getPartition() {
            return partition;
        }

        public void setPartition(Integer partition) {
            this.partition = partition;
        }

        public Long getOffset() {
            return offset;
        }

        public void setOffset(Long offset) {
            this.offset = offset;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    public static class Raw {
        private String payload;
        private String contentType;

        public String getPayload() {
            return payload;
        }

        public void setPayload(String payload) {
            this.payload = payload;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
    }
}