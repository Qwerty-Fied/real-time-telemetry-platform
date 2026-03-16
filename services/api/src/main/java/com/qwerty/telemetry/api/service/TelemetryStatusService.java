package com.qwerty.telemetry.api.service;

import com.qwerty.telemetry.api.dto.TelemetryStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class TelemetryStatusService {

    private final PrometheusQueryService prometheusQueryService;

    public TelemetryStatusService(PrometheusQueryService prometheusQueryService) {
        this.prometheusQueryService = prometheusQueryService;
    }

    public TelemetryStatusResponse getStatus() {
        double processed = prometheusQueryService.queryValue("sum(telemetry_processed_total)");
        double dlq = prometheusQueryService.queryValue("sum(telemetry_dlq_total)");
        double errors = prometheusQueryService.queryValue("sum(telemetry_errors_total)");
        double consumerLag = prometheusQueryService.queryValue("sum(kafka_consumergroup_lag_sum)");

        String status = "UP";
        if (consumerLag > 100) {
            status = "DEGRADED";
        }
        if (errors > 0) {
            status = "WARN";
        }

        return new TelemetryStatusResponse(
                "real-time-telemetry-platform",
                status,
                processed,
                dlq,
                errors,
                consumerLag
        );
    }
}