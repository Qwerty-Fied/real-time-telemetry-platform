package com.qwerty.telemetry.consumer.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class TelemetryMetrics {
    public final Counter processed;
    public final Counter dlq;
    public final Counter errors;
    public final Counter dlqProduceFail;
    public final Timer processing;

    public TelemetryMetrics(MeterRegistry registry) {
        this.processed = Counter.builder("telemetry_processed_total")
                .register(registry);

        this.dlq = Counter.builder("telemetry_dlq_total")
                .register(registry);

        this.errors = Counter.builder("telemetry_errors_total")
                .register(registry);

        this.dlqProduceFail = Counter.builder("telemetry_dlq_produce_fail_total")
                .register(registry);

        this.processing = Timer.builder("telemetry_processing_seconds")
                .publishPercentileHistogram()
                .register(registry);
    }
}