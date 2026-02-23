# Local Infrastructure (Docker Compose)

## Start
docker compose up -d

## Services
- Kafka: localhost:9092
- Kafka UI: http://localhost:8080
- Postgres: localhost:5432 (telemetry/telemetry)
- Mosquitto: localhost:1883

## Kafka topics
- telemetry.raw (3 partitions)
- telemetry.dlq (1 partition)