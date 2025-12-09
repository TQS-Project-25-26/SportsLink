FROM maven:3.9-eclipse-temurin-21

WORKDIR /app

# Copy everything
COPY . .

# Expose your Spring Boot port
EXPOSE 8080

# Run Spring Boot using Maven
CMD ["mvn", "spring-boot:run"]
