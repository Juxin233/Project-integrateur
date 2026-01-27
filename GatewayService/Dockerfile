FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copia cualquier jar compilado
COPY target/*.jar app.jar

# Gateway port
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
