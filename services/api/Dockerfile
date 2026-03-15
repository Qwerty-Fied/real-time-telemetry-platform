FROM eclipse-temurin:17-jre
WORKDIR /app
COPY services/api/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]