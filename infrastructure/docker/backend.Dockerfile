FROM maven:3.9.11-eclipse-temurin-11 AS build
WORKDIR /workspace

COPY backend/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B

COPY backend/src ./src
RUN mvn clean package -B

FROM eclipse-temurin:11-jre-alpine
WORKDIR /app

RUN addgroup -S settlement && adduser -S settlement -G settlement
COPY --from=build --chown=settlement:settlement /workspace/target/trade-settlement-backend-*.jar app.jar

USER settlement
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
