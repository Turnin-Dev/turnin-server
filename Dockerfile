# 실행 스테이지
FROM eclipse-temurin:26.0.2.1_1-jre-jammy@sha256:788e9c50c5946c0701c12d9e87a6c1399060b35f6d7bb574b529a0c5fb19002a

RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

# CD에서 빌드된 JAR 복사
COPY build/libs/turnin-api.jar app.jar

RUN mkdir -p /app/secrets /app/ml /app/agents /logs/normal /logs/privacy && \
    chown -R appuser:appgroup /app /logs

USER appuser

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar /app/app.jar"]