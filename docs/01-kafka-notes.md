Kafka는 이벤트 기록 노트다

기존 메시지큐
A가 B에게 "안녕"이라고 전달하면
B가 읽는순간 메시지는 사라진다

Kafka는 raw파일을 통해
[이벤트 로그 파일]
1번 이벤트
2번 이벤트
3번 이벤트
4번 이벤트
이렇게 지우지 않고 남긴다

Consumer의 역할
이벤트 로그 파일에서 "나 여기까지 읽었어요"라고 표시를 한다
표시 = offset

Consumer 그룹 : 여러 Consumer가 모여있는 그룹으로, 이 그룹에서 파티션을 읽으때는 역할을 분담해서 각각 맡은 파티션만 읽는다

Kafka를 쓰는 이유
1. 이벤트 유실 방지 (offset으로 어디까지 읽었는지 저장되어 Consumer가 죽었다 살아도 offset부터 읽는다)
2. 재처리 기능
3. 확장 가능
4. 스트리밍 처리 가능


mqtt의 토픽과 kafka 토픽의 차이

MQTT
telemetry/device1
telemetry/device2

퍼블리셔가 발행 -> 구독자가 구독 (메시지 즉시 전달)

== 라우팅 키에 가까움

Kafka
이벤트가 저장되는 로그 스트림
telemetry.dlq
telemetry.raw
 ├── partition 0
 ├── partition 1
 └── partition 2
kafka의 토픽은 파티션이 있어 순서가 보장되는 로그파일이다
내가 선언한 이벤트 로그 공간 = 토픽 (권한/메세지휘발/병렬처리 등 각각 설정 가능)

내 프로젝트에서의 흐름
1. 디바이스에서 mqtt 토픽을 퍼블리시
2. MQTT producer가 토픽을 구독, 수신
3. Kafka 토픽 ~.raw에 퍼블리시(저장)
4. Kafka Consumer가 읽음


내 프로젝트에서는 파티션 3개로 간다
1. 확장성
2. 병렬처리 학습

처음부터 파티션 수를 여유있게 잡아야하는 이유
1. 파티션은 늘릴수는 있어도 줄일 수는 없다
2. 늘리면 key분포가 바뀔 수 있다
3. 재정렬이 생길 수 있다

key 분포가 바뀐다는건 뭔가
partition = hash(key) % partition_count -> kafka의 기본 저장 파티션 계산공식
{
  "event_id": "...",
  "device_id": "device1",
  ...
}
이렇게 메시지를 보내고
key를 device_id로 잡으면
hash(device1)로 kafka가 문자열을 숫자로 바꾼다
hash("device1") = 8347291
hash("device2") = 1928472
hash("device3") = 5551212
공식에 대입하면 
8347291 % 3 = 1
1928472 % 3 = 0
5551212 % 3 = 2
여기서 나누는 값이 달라지기 때문에 
파티션 추가 이전과 이후 로그가 쌓이는 파티션이 달라질 수 있다
== 순서가 보장되어야 하는 이벤트에 무결성이 깨짐