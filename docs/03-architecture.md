# Architecture

이 프로젝트는 이벤트 기반 데이터 파이프라인을 실험하기 위한 플랫폼입니다.

## Data Flow

1. Device → MQTT Broker로 telemetry 전송
2. mqtt-producer가 MQTT 메시지를 Kafka `telemetry.raw`로 전달
3. telemetry-consumer가 메시지를 소비
4. validation 수행
5. 정상 데이터 → `telemetry.clean`
6. 오류 데이터 → `telemetry.dlq`
7. dlq-reprocessor가 DLQ 메시지 재처리

## Monitoring

Prometheus와 Grafana를 사용하여 다음을 모니터링합니다.

* telemetry processing rate
* DLQ 발생률
* processing latency
* Kafka consumer lag

## Failure Handling

* Validation 실패 데이터는 DLQ로 전송
* DLQ 메시지는 dlq-reprocessor가 재처리


Device
 ↓
MQTT Broker
 ↓
mqtt-producer
 ↓
Kafka telemetry.raw
 ↓
telemetry-consumer
 ├ telemetry.clean
 └ telemetry.dlq
 ↓
dlq-reprocessor
 ↓
Kafka telemetry.raw

Monitoring
Prometheus
Grafana
Kafka exporter