# ---- Build stage ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 앱 JAR
COPY --from=build /workspace/target/*.jar app.jar

# 외부 설정 (원하면 이미지 빌드 시 덮어씀)
COPY ./src/main/resources/application.yml /application.yml
COPY ./src/main/resources/logback-spring-local.xml /logback-spring-local.xml

# 로컬 파일 저장 위치
RUN mkdir -p /app/storage
VOLUME ["/app/storage"]

ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]