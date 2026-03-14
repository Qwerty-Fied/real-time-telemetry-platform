package com.qwerty.telemetry.api.controller;

import com.qwerty.telemetry.api.dto.TelemetryStatusResponse;
import com.qwerty.telemetry.api.service.TelemetryStatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TelemetryStatusController {

    private final TelemetryStatusService statusService;

    public TelemetryStatusController(TelemetryStatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/api/telemetry/status")
    public TelemetryStatusResponse status() {
        return statusService.getStatus();
    }
}