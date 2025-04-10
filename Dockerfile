#FROM ubuntu:latest
#LABEL authors="wilcz"
#
#ENTRYPOINT ["top", "-b"]



# Use OpenJDK 21 as the base image
FROM openjdk:21-jdk-slim as build

# Set the working directory in the container
WORKDIR /kontenerki

# Copy the local application jar file to the container
COPY build/libs/main-all.jar /kontenerki/api.jar
#COPY build/libs/main-0.0.1.jar /kontenerki/api.jar

# Expose the port your Ktor app runs on
EXPOSE 100

# Evirnomental variables
ENV DB_URL=jdbc:postgresql://localhost:5431/db1 DB_USER=postgres POSTGRES_PASSWORD=postgres

# Command to run the app
CMD ["java", "-jar", "api.jar"]



## Use OpenJDK 21 as the base image
#FROM openjdk:21-jdk-slim as builder
#
## Set the working directory
#WORKDIR /app
#
## Copy Gradle files first to leverage Docker cache
#COPY gradlew .
#COPY gradle gradle
#COPY build.gradle.kts .
#COPY settings.gradle.kts .
#COPY src src
#
## Build the application
#RUN chmod +x gradlew && ./gradlew build --no-daemon
#
## Runtime stage
#FROM openjdk:21-jdk-slim
#
#WORKDIR /app
#
## Copy the built application from builder stage
#COPY --from=builder /app/build/libs/*-all.jar app.jar
#
## Expose the port (should match your application.yaml)
#EXPOSE 100
#
## Environment variables (can be overridden at runtime)
#ENV DB_URL=jdbc:postgresql://postgres_db1:5432/db1 \
#    DB_USER=postgres \
#    POSTGRES_PASSWORD=postgres \
#    PORT=100
#
## Health check (optional)
#HEALTHCHECK --interval=30s --timeout=3s \
#  CMD curl -f http://localhost:100/health || exit 1
#
## Command to run the app
#CMD ["java", "-jar", "app.jar"]