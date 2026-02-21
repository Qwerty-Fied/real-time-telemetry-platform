만들고자 하는것
실시간 이벤트 수집, 처리, 모니터링이 가능한 표준 스트리밍 백엔드 플랫폼

흐름
디바이스에서 MQTT로 들어오는 데이터를
Kafka로 안전하게 처리하고
DB에 저장하고
API로 조회 가능하고
운영 지표까지 볼 수 있는 시스템

목적
메시징 시스템의 이해
장애 복구 설계
중복 처리 전략
성능 튜닝
모니터링 구축
CI/CD
클라우드 배포
를 하는 실서비스 레벨 아키텍처 재현 프로젝트 (라고 쓰고 탈출 버튼이라고 읽는다)

Ingestion
MQTT → Kafka Producer

Streaming
Kafka → Consumer

Storage
Postgres 저장 + Redis 캐시

API
조회 / 집계

Observability
Prometheus + Grafana

DevOps
Docker + CI + AWS 배포