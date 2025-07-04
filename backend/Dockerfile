FROM eclipse-temurin:21-jdk

# 작업 디렉토리 설정
WORKDIR /app

# 전체 프로젝트 복사
COPY . .

# OTEL Agent JAR 복사
COPY opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar


# 실행 권한 부여
RUN chmod +x /app/dev-deploy.sh


# GitHub Actions에서 전달받을 ARG 정의
ARG DB_URL
ARG DB_USERNAME
ARG DB_PASSWORD
ARG KAKAO_CLIENT_ID
ARG KAKAO_REDIRECT_URI
ARG JWT_SECRET_CODE
ARG FE_BASE_URL
ARG BE_BASE_URL
ARG AI_BASE_URL
ARG EC2_PUBLIC_URL_1
ARG EC2_PUBLIC_URL_2
ARG EC2_PUBLIC_URL_3
ARG OTEL_EXPORTER_OTLP_ENDPOINT
ARG OTEL_SERVICE_NAME
ARG REDIS_HOST
ARG REDIS_PORT
ARG REDIS_PASSWORD

# 환경 변수 설정
ENV DB_URL=$DB_URL \
    DB_USERNAME=$DB_USERNAME \
    DB_PASSWORD=$DB_PASSWORD \
    KAKAO_CLIENT_ID=$KAKAO_CLIENT_ID \
    KAKAO_REDIRECT_URI=$KAKAO_REDIRECT_URI \
    JWT_SECRET_CODE=$JWT_SECRET_CODE \
    FE_BASE_URL=$FE_BASE_URL \
    BE_BASE_URL=$BE_BASE_URL \
    AI_BASE_URL=$AI_BASE_URL \
    EC2_PUBLIC_URL_1=$EC2_PUBLIC_URL_1 \
    EC2_PUBLIC_URL_2=$EC2_PUBLIC_URL_2 \
    EC2_PUBLIC_URL_3=$EC2_PUBLIC_URL_3 \
    OTEL_EXPORTER_OTLP_ENDPOINT=$OTEL_EXPORTER_OTLP_ENDPOINT \
    REDIS_HOST=localhost \
    REDIS_PORT=6379 \
    REDIS_PASSWORD=$REDIS_PASSWORD \
    OTEL_SERVICE_NAME=$OTEL_SERVICE_NAME

# Gradle 빌드 (테스트 생략)
#RUN ./gradlew build -x test -x compileTestJava -x processTestResources
#테스트 추가
RUN ./gradlew build -x checkstyleMain -x checkstyleTest -x test

# OTEL Agent와 함께 Spring Boot 실행
CMD java \
  -javaagent:/app/opentelemetry-javaagent.jar \
  -Dotel.service.name=$OTEL_SERVICE_NAME  \
  -Dotel.exporter.otlp.endpoint=$OTEL_EXPORTER_OTLP_ENDPOINT \
  -Dotel.metrics.exporter=none \
  -Dspring.datasource.url="$DB_URL" \
  -Dspring.datasource.username="$DB_USERNAME" \
  -Dspring.datasource.password="$DB_PASSWORD" \
  -Dmanagement.endpoints.web.exposure.include=prometheus,health,info \
  -Duser.timezone=Asia/Seoul \
  -Dspring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver \
  -jar build/libs/backend-0.0.1-SNAPSHOT.jar


