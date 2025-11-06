# syntax=docker/dockerfile:1.6

FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw -B -ntp clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
ENV SPRING_PROFILES_ACTIVE=prod \
    TZ=UTC
WORKDIR /app
COPY --from=builder /workspace/target/notification-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Xms256m","-Xmx512m","-jar","/app/app.jar"]
