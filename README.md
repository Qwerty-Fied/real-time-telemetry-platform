real-time-telemetry-platform

실시간 IoT 텔레메트리 데이터를 수집하고, 스트리밍 처리하며, 정제된 토픽으로 가공하고 모니터링하는 이벤트 기반 플랫폼입니다.

이 프로젝트는 MQTT → Kafka → Consumer → Clean Topic → Monitoring 구조를 기반으로 동작하며, 전체 인프라는 Docker 환경에서 실행됩니다.

프로젝트 개요

플랫폼의 데이터 흐름은 다음과 같습니다.

디바이스가 MQTT 브로커로 텔레메트리 데이터를 전송합니다.

mqtt-producer가 MQTT 메시지를 구독하여 Kafka telemetry.raw 토픽으로 전달합니다.

telemetry-consumer가 telemetry.raw를 소비합니다.

Consumer는 JSON 파싱 및 필수 필드 검증을 수행합니다.

정상 데이터는 telemetry.clean 토픽으로 전송합니다.

비정상 데이터는 telemetry.dlq 토픽으로 전송합니다.

각 서비스의 메트릭은 Prometheus가 수집하고, Grafana에서 시각화합니다.

전체 아키텍처
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
토픽 구조
Topic	설명
telemetry.raw	디바이스 원본 데이터
telemetry.clean	검증 및 가공 완료 데이터
telemetry.dlq	파싱 또는 검증 실패 데이터
기술 스택

Java 17

Spring Boot

Spring Kafka

Spring Actuator

Micrometer

Eclipse Mosquitto

Apache Kafka

Zookeeper

Prometheus

Grafana

Docker

Docker Compose

Kafka UI (Provectus)

실행 방법
1. 인프라 실행
cd infra
docker compose up -d

실행 후 접속 주소:

서비스	주소
Kafka UI	http://localhost:8080
Prometheus	http://localhost:9090
Grafana	http://localhost:3000
2. 서비스 빌드
./gradlew :services:mqtt-producer:bootJar
./gradlew :services:telemetry-consumer:bootJar
3. 서비스 실행
java -jar services/mqtt-producer/build/libs/*.jar
java -jar services/telemetry-consumer/build/libs/*.jar
MQTT 테스트 예시

정상 데이터 발행 예시:

mosquitto_pub -h localhost -p 1883 \
-t telemetry/device1 \
-m '{"deviceId":"device1","temperature":23.5}'

비정상 데이터 발행 예시:

mosquitto_pub -h localhost -p 1883 \
-t telemetry/device1 \
-m '{"deviceId":"device1","te":99378}'
Consumer 검증 로직

telemetry-consumer는 다음 검증을 수행합니다.

JSON 파싱

필수 필드 존재 여부 확인

deviceId

temperature

처리 결과:

정상 데이터 → telemetry.clean

실패 데이터 → telemetry.dlq

DLQ 메시지에는 장애 분석을 위한 메타데이터를 함께 포함합니다.

예시 포함 정보:

failedAt

errorType

errorMessage

consumer

source.topic

source.partition

source.offset

raw.payload

모니터링
Spring Actuator Metrics

각 서비스는 /actuator/prometheus 엔드포인트를 통해 메트릭을 노출합니다.

예시 메트릭:

telemetry_processed_total

telemetry_dlq_total

telemetry_errors_total

telemetry_dlq_produce_fail_total

telemetry_processing_seconds

jvm_memory_used_bytes

http_server_requests_seconds_count

Prometheus

Prometheus가 각 서비스 메트릭을 scrape 합니다.

접속:

http://localhost:9090

확인 예시:

telemetry_processed_total
telemetry_dlq_total
telemetry_errors_total
Grafana

Grafana에서 Consumer 처리량, DLQ 발생량, JVM 메모리 등 주요 지표를 시각화합니다.

접속:

http://localhost:3000

구성 예시:

Consumer processed count

DLQ count

Error count

Processing latency

JVM memory usage

현재 구현 상태
기능	상태
MQTT → Kafka 연동	✅ 완료
Raw Topic 수신	✅ 완료
Consumer JSON 파싱	✅ 완료
Validation 로직	✅ 완료
DLQ 처리	✅ 완료
Clean Topic 전송	✅ 완료
Prometheus 연동	✅ 완료
Grafana 대시보드 구성	✅ 완료
Consumer Lag 모니터링	🚧 진행 중
DLQ produce fail 테스트	⏳ 미검증
운영 안정화 TODO

다음 단계로는 운영 안정성을 강화하는 작업을 진행할 예정입니다.

Consumer Lag 모니터링 고도화

Alertmanager 연동

Dead Letter 재처리 로직

Kafka Partition 전략 개선

부하 테스트

컨슈머 그룹 확장 기반 스케일아웃

장애 상황별 테스트 케이스 보강

DLQ produce fail

Kafka broker 장애

Consumer 재시작/재처리 시나리오

설계 의도

이 플랫폼은 단순한 메시지 전달이 아니라, 다음을 목표로 설계되었습니다.

데이터 무결성 확보

장애 분리 및 분석 가능 구조 제공

실시간 가시성 확보

운영 가능한 이벤트 처리 구조 구현
