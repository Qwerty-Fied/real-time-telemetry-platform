package com.qwerty.telemetry.api.dto;

import java.util.List;

public class PrometheusQueryResponse {

    private String status;
    private Data data;

    public String getStatus() {
        return status;
    }

    public Data getData() {
        return data;
    }

    public static class Data {
        private String resultType;
        private List<Result> result;

        public String getResultType() {
            return resultType;
        }

        public List<Result> getResult() {
            return result;
        }
    }

    public static class Result {
        private List<Object> value;

        public List<Object> getValue() {
            return value;
        }
    }
}