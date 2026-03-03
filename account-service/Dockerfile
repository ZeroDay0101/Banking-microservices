FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy the full multi-module project (context is repository root)
COPY . .

# Build only the account-service module (and its dependencies)
RUN mvn -pl account-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the executable Spring Boot jar for the account-service module
COPY --from=build /build/account-service/target/*-exec.jar app.jar

EXPOSE 8081 5006

ENTRYPOINT ["java","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006", "-jar", "/app/app.jar"]
