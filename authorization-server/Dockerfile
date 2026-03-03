FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy the full multi-module project (context is repository root)
COPY . .

# Build only the authorization-server module (and its dependencies)
RUN mvn -pl authorization-server -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the executable Spring Boot jar for the authorization-server module
COPY --from=build /build/authorization-server/target/*-exec.jar app.jar

EXPOSE 9000 5005

ENTRYPOINT ["java","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "/app/app.jar"]
