# real-time-telemetry-platform

실시간 IoT 텔레메트리 데이터를 수집하고, 스트리밍 처리하며, 정제(clean) 토픽으로 가공하는 이벤트 기반 플랫폼입니다.

MQTT → Kafka → Consumer → Clean Topic 구조를 기반으로 동작하며, Docker 환경에서 전체 인프라가 실행됩니다.

---

## 📌 프로젝트 개요

이 프로젝트는 다음과 같은 흐름으로 동작합니다:

1. MQTT 브로커를 통해 디바이스에서 텔레메트리 데이터 수신
2. MQTT Producer가 Kafka `telemetry.raw` 토픽으로 전달
3. Kafka Consumer가 raw 데이터를 소비
4. 검증 및 가공 후 `telemetry.clean` 토픽으로 재전송

---

## 🏗 아키텍처 구조
Device
↓
MQTT (Mosquitto)
↓
mqtt-producer (Spring Boot)
↓
Kafka (telemetry.raw)
↓
telemetry-consumer
↓
Kafka (telemetry.clean)


---

## 🛠 기술 스택

- Java 17
- Spring Boot 4.x
- Spring Kafka
- Eclipse Mosquitto (MQTT)
- Apache Kafka
- Zookeeper
- PostgreSQL
- Docker & Docker Compose
- Kafka UI (Provectus)

---

## 🚀 실행 방법

### 1. 인프라 실행

```bash
cd infra
docker compose up -d
```
2. 토픽 확인 (Kafka UI)

http://localhost:8080

3. 서비스 빌드
./gradlew :services:mqtt-producer:bootJar
./gradlew :services:telemetry-consumer:bootJar
📂 주요 토픽

telemetry.raw : 디바이스 원본 데이터


telemetry.clean : 검증/가공된 데이터

telemetry.dlq : 처리 실패 데이터 (Dead Letter Queue)
