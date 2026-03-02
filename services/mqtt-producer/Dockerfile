# ---- build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY settings.gradle* .
COPY build.gradle* .

# 🔥 여기: mqtt-producer만 말고 services 전체 복사
COPY services services

RUN chmod +x gradlew && ./gradlew :services:mqtt-producer:bootJar -x test

# ---- run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드 산출물 복사
COPY --from=build /workspace/services/mqtt-producer/build/libs/*.jar /app/app.jar

# 컨테이너 기본 프로파일
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]