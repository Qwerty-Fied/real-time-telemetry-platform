# Real-time Telemetry Platform

실시간 IoT 텔레메트리 데이터를 수집하고 처리하는 이벤트 기반 플랫폼입니다.

## Architecture

```
Device
 ↓
MQTT Broker
 ↓
mqtt-producer
 ↓
Kafka (telemetry.raw)
 ↓
telemetry-consumer
 ├ telemetry.clean
 └ telemetry.dlq
 ↓
dlq-reprocessor
 ↓
Kafka (telemetry.raw 재투입)
```

## Observability

* Prometheus
* Grafana
* Kafka exporter (consumer lag monitoring)

## Components

| Service            | Description                |
| ------------------ | -------------------------- |
| mqtt-producer      | MQTT → Kafka bridge        |
| telemetry-consumer | validation 및 clean/DLQ 분기  |
| dlq-reprocessor    | DLQ 메시지 재처리                |
| Prometheus         | metrics 수집                 |
| Grafana            | monitoring dashboard       |
| kafka-exporter     | Kafka consumer lag metrics |

## Topics

| Topic           | Description       |
| --------------- | ----------------- |
| telemetry.raw   | MQTT에서 수신한 원본 데이터 |
| telemetry.clean | 정상 데이터            |
| telemetry.dlq   | validation 실패 데이터 |

## Monitoring

Grafana dashboard에서 다음 메트릭을 확인할 수 있습니다.

* processing rate
* DLQ rate
* processing latency
* Kafka consumer lag

## Run

```bash
docker compose up -d
```
