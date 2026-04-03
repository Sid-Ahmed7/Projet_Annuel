# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Optimisation du cache Maven : on copie d'abord le pom.xml pour télécharger les dépendances
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copie des sources et compilation
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Meilleure gestion de la sécurité : exécution en tant qu'utilisateur non-root
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

# Création du dossier pour les uploads (nécessite d'être root temporairement ou de changer les droits)
USER root
RUN mkdir -p uploads/images && chown -R spring:spring uploads
USER spring:spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]