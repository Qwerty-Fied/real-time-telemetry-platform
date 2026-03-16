package com.qwerty.telemetry.api.service;

import com.qwerty.telemetry.api.dto.PrometheusQueryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PrometheusQueryService {

    private final RestClient restClient;

    public PrometheusQueryService(@Value("${prometheus.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public double queryValue(String promql) {
        try {
            PrometheusQueryResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/query")
                            .queryParam("query", promql)
                            .build())
                    .retrieve()
                    .body(PrometheusQueryResponse.class);

            if (response == null ||
                    response.getData() == null ||
                    response.getData().getResult() == null ||
                    response.getData().getResult().isEmpty()) {
                return 0.0;
            }

            var value = response.getData().getResult().get(0).getValue();
            if (value == null || value.size() < 2) {
                return 0.0;
            }

            return Double.parseDouble(String.valueOf(value.get(1)));
        } catch (Exception e) {
            return 0.0;
        }
    }
}