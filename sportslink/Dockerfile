FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the jar file (we assume it's placed here by the CI pipeline)
COPY app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
