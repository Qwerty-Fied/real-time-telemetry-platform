package com.qwerty.telemetry.api.dto;

public class TelemetryStatusResponse {

    public String service;
    public String status;
    public long processed;
    public long dlq;
    public long errors;

    public TelemetryStatusResponse(String service, String status,
                                   long processed, long dlq, long errors) {
        this.service = service;
        this.status = status;
        this.processed = processed;
        this.dlq = dlq;
        this.errors = errors;
    }
}