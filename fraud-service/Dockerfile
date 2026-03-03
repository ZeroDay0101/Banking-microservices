FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy the full multi-module project (context is repository root)
COPY . .

# Build only the fraud-service module (and its dependencies)
RUN mvn -pl fraud-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the executable Spring Boot jar for the fraud-service module
COPY --from=build /build/fraud-service/target/*-exec.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
