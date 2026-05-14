# 실행 스테이지
FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# CD에서 빌드된 JAR 복사
COPY build/libs/turnin-api.jar app.jar

RUN mkdir -p /app/secrets /app/ml /app/agents && \
    chown -R appuser:appgroup /app

USER appuser

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]