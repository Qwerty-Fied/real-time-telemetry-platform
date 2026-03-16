package com.qwerty.telemetry.api.dto;

public class TelemetryStatusResponse {

    public String service;
    public String status;
    public double processed;
    public double dlq;
    public double errors;
    public double consumerLag;

    public TelemetryStatusResponse(String service, String status,
                                   double processed, double dlq,
                                   double errors, double consumerLag) {
        this.service = service;
        this.status = status;
        this.processed = processed;
        this.dlq = dlq;
        this.errors = errors;
        this.consumerLag = consumerLag;
    }
}