FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /app/target/*.jar app.jar

USER root
RUN mkdir -p uploads/images && chown -R spring:spring uploads

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

USER spring:spring

EXPOSE 8080
ENTRYPOINT ["/entrypoint.sh"]