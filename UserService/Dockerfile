#base image with the Runtime Environment
FROM eclipse-temurin:21-jdk-alpine

#working directory inside the container
WORKDIR /app

# 3. Copy the compiled JAR file from your target folder to the container
# (Assuming you've run 'mvn package' or 'gradle build')
COPY target/UserService-0.0.1-SNAPSHOT.jar  app.jar

# 4. Expose the port your service runs on (e.g., 8080)
EXPOSE 8080

# 5. The command to run your application
ENTRYPOINT ["java", "-jar", "app.jar"]