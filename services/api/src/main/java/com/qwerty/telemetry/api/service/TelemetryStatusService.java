package com.qwerty.telemetry.api.service;

import com.qwerty.telemetry.api.dto.TelemetryStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class TelemetryStatusService {

    public TelemetryStatusResponse getStatus() {

        // TODO: Prometheus 또는 DB에서 실제 값 조회
        return new TelemetryStatusResponse(
                "real-time-telemetry-platform",
                "UP",
                120,
                3,
                1
        );
    }
}