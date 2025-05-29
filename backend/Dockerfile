FROM eclipse-temurin:21-jdk

# 작업 디렉토리 설정
WORKDIR /app

# 프로젝트 복사
COPY . .

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

# 환경 변수 설정 (Java 실행 시 사용됨)
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
    EC2_PUBLIC_URL_3=$EC2_PUBLIC_URL_3

# 빌드 (테스트 생략)
RUN ./gradlew build -x test -x compileTestJava -x processTestResources

# Spring Boot 실행 - shell form (환경변수 치환 가능)
CMD java \
  -Dspring.datasource.url="$DB_URL" \
  -Dspring.datasource.username="$DB_USERNAME" \
  -Dspring.datasource.password="$DB_PASSWORD" \
  -Dspring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver \
  -jar build/libs/backend-0.0.1-SNAPSHOT.jar

