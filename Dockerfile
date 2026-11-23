# 실행 스테이지
FROM eclipse-temurin:17.0.21.1_2-jre-jammy@sha256:0197d17ef5317724fda570ba2263b5e011fa32663339a880414fdb186884a611

RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

# CD에서 빌드된 JAR 복사
COPY build/libs/turnin-api.jar app.jar

RUN mkdir -p /app/secrets /app/ml /app/agents /logs/normal /logs/privacy && \
    chown -R appuser:appgroup /app /logs

USER appuser

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar /app/app.jar"]