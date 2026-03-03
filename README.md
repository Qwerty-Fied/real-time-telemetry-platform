real-time-telemetry-platform

실시간 IoT 텔레메트리 데이터를 수집하고, 스트리밍 처리하며, 정제(clean) 토픽으로 가공하고 모니터링하는 이벤트 기반 플랫폼입니다.

MQTT → Kafka → Consumer → Clean Topic → Monitoring 구조를 기반으로 동작하며, Docker 환경에서 전체 인프라가 실행됩니다.

📌 프로젝트 개요

이 프로젝트는 다음과 같은 흐름으로 동작합니다:

MQTT 브로커를 통해 디바이스에서 텔레메트리 데이터 수신

MQTT Producer가 Kafka telemetry.raw 토픽으로 전달

Kafka Consumer가 raw 데이터를 소비

JSON 파싱 및 필수 필드 검증

정상 데이터는 telemetry.clean 토픽으로 전송

비정상 데이터는 telemetry.dlq 토픽으로 전송

Prometheus + Grafana를 통해 메트릭 시각화

🏗 전체 아키텍처
Device
   ↓
MQTT (Mosquitto)
   ↓
mqtt-producer (Spring Boot)
   ↓
Kafka (telemetry.raw)
   ↓
telemetry-consumer
   ├── 정상 → Kafka (telemetry.clean)
   └── 실패 → Kafka (telemetry.dlq)
   
Prometheus ← Spring Actuator Metrics
   ↓
Grafana Dashboard
📂 토픽 구조
Topic	설명
telemetry.raw	디바이스 원본 데이터
telemetry.clean	검증 및 가공 완료 데이터
telemetry.dlq	파싱/검증 실패 데이터
🛠 기술 스택

Java 17

Spring Boot 4.x

Spring Kafka

Spring Actuator + Micrometer

Eclipse Mosquitto (MQTT)

Apache Kafka

Zookeeper

Prometheus

Grafana

PostgreSQL

Docker & Docker Compose

Kafka UI (Provectus)

🚀 실행 방법
1️⃣ 인프라 실행
cd infra
docker compose up -d

실행 후 접속:

서비스	주소
Kafka UI	http://localhost:8080

Prometheus	http://localhost:9090

Grafana	http://localhost:3000
2️⃣ 서비스 빌드
./gradlew :services:mqtt-producer:bootJar
./gradlew :services:telemetry-consumer:bootJar
3️⃣ 서비스 실행
java -jar services/mqtt-producer/build/libs/*.jar
java -jar services/telemetry-consumer/build/libs/*.jar
📥 MQTT 테스트 예시
mosquitto_pub -h localhost -p 1883 \
-t telemetry/device1 \
-m '{"deviceId":"device1","temperature":23.5}'
✅ Consumer 검증 로직

Consumer는 다음 검증을 수행합니다:

JSON 파싱

필수 필드 존재 여부 확인

deviceId

temperature

정상 → clean 토픽

실패 → dlq 토픽

추가 정보 포함:

processedAt (처리 시간)

sourcePartition

sourceOffset

📊 모니터링 (Prometheus + Grafana)
📌 Spring Actuator Metrics

각 서비스는 /actuator/prometheus 엔드포인트를 통해 메트릭을 노출합니다.

예시 메트릭:

kafka_consumer_records_consumed_total

jvm_memory_used_bytes

http_server_requests_seconds_count

📈 Prometheus

Prometheus가 각 서비스의 메트릭을 scrape합니다.

확인 방법:

http://localhost:9090

쿼리 예시:

kafka_consumer_records_consumed_total
📊 Grafana
http://localhost:3000

Data Source: Prometheus

대시보드에서 Kafka/Consumer 처리량 시각화

🔥 현재 구현 상태
기능	상태
MQTT → Kafka 연동	✅ 완료
Raw Topic 수신	✅ 완료
Consumer 파싱	✅ 완료
Validation 로직	✅ 완료
DLQ 처리	✅ 완료
Clean Topic 전송	✅ 완료
Prometheus 연동	✅ 완료
Grafana 대시보드	✅ 완료

📌 다음 단계 (운영 안정성 단계)
Consumer Lag 모니터링
Alertmanager 연동
Dead Letter 재처리 로직
Kafka Partition 전략 개선
부하 테스트
스케일아웃 (컨슈머 그룹 확장)

🧠 설계 의도
이 플랫폼은 단순 메시지 전달이 아니라:
데이터 무결성 확보
장애 분리 (DLQ)
실시간 가시성 확보 (Observability)
운영 가능한 구조 설계
를 목표로 합니다.
