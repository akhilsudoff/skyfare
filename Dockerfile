FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /app/otel-agent.jar
RUN chmod 644 /app/otel-agent.jar
COPY --from=build /build/target/*.jar /app/app.jar
USER app
EXPOSE 8080
ENV OTEL_SERVICE_NAME=skyfare \
    OTEL_TRACES_EXPORTER=none \
    OTEL_METRICS_EXPORTER=none \
    OTEL_LOGS_EXPORTER=none
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-javaagent:/app/otel-agent.jar", "-jar", "/app/app.jar"]
