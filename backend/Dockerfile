FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY . .

# 1. GitHub Actions에서 전달받을 ARG 정의
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

# 2. ENV로 굽기 (선택적으로 유지)
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

# 3. 빌드
RUN ./gradlew build -x test -x compileTestJava -x processTestResources

# 4. 실행 시 JVM 시스템 속성으로 명시적으로 전달
CMD bash -c "java \
    -Dspring.datasource.url=$DB_URL \
    -Dspring.datasource.username=$DB_USERNAME \
    -Dspring.datasource.password=$DB_PASSWORD \
    -Dkakao.client-id=$KAKAO_CLIENT_ID \
    -Dkakao.redirect-uri=$KAKAO_REDIRECT_URI \
    -Djwt.secret=$JWT_SECRET_CODE \
    -Dfe.base_url=$FE_BASE_URL \
    -Dbe.base_url=$BE_BASE_URL \
    -Dai.base-url=$AI_BASE_URL \
    -Dec2.public_url_1=$EC2_PUBLIC_URL_1 \
    -Dec2.public_url_2=$EC2_PUBLIC_URL_2 \
    -Dec2.public_url_3=$EC2_PUBLIC_URL_3 \
    -jar build/libs/backend-0.0.1-SNAPSHOT.jar"
