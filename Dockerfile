# 실행 스테이지
FROM eclipse-temurin:17.0.21_9-jre-jammy@sha256:57587209f7b7f291c66fe80c9779efa9dde4ec199160cbb5c38ec2fe27a78bc4

RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

# CD에서 빌드된 JAR 복사
COPY build/libs/turnin-api.jar app.jar

RUN mkdir -p /app/secrets /app/ml /app/agents /logs/normal /logs/privacy && \
    chown -R appuser:appgroup /app /logs

USER appuser

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar /app/app.jar"]