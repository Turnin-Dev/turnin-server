# 실행 스테이지
FROM eclipse-temurin:17.0.20.1_1-jre-jammy@sha256:c35794d1348777e6bc310b9b7f420098ef8f37e988199549b545fded79a895a2

RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

# CD에서 빌드된 JAR 복사
COPY build/libs/turnin-api.jar app.jar

RUN mkdir -p /app/secrets /app/ml /app/agents /logs/normal /logs/privacy && \
    chown -R appuser:appgroup /app /logs

USER appuser

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar /app/app.jar"]