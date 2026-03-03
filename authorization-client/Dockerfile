FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy the full multi-module project (context is repository root)
COPY . .

# Build only the authorization-client module (and its dependencies)
RUN mvn -pl authorization-client -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the executable Spring Boot jar for the authorization-client module
COPY --from=build /build/authorization-client/target/*-exec.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
