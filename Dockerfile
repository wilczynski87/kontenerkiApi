## Use OpenJDK 21 as the base image
#FROM openjdk:21-jdk-slim as build
#
## Set the working directory in the container
#WORKDIR /kontenerki
#
## Copy the local application jar file to the container
#COPY build/libs/main-all.jar /kontenerki/api.jar
##COPY build/libs/main-0.0.1.jar /kontenerki/api.jar
#
## Expose the port your Ktor app runs on
#EXPOSE 8100
#
## Evirnomental variables
#ENV DB_HOST=localhost
#ENV DB_NAME=db1
#ENV DB_PORT=5432
#ENV DB_USER=admin_user
#ENV DB_PASSWORD=postgres
#ENV POSTGRES_DB=db1
#ENV POSTGRES_PASSWORD=postgres
#ENV EMAIL_PORT=8200
#ENV EMAIL_NAME=email
#
## Command to run the app
#CMD ["java", "-jar", "api.jar"]

FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Copy only files needed to download dependencies first (for caching)
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Download dependencies
RUN gradle build --no-daemon --stacktrace || true

# Copy rest of the project and build it
COPY . .

# Build the project
RUN gradle clean shadowJar --no-daemon

# === Runtime stage ===
FROM openjdk:25-ea-21-jdk-slim
WORKDIR /kontenerki

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar ./api.jar

# Expose the port the Ktor app runs on
EXPOSE 8100

# Evirnomental variables
ENV DB_HOST=localhost
ENV DB_NAME=db1
ENV DB_PORT=5432
ENV DB_USER=admin_user
ENV DB_PASSWORD=postgres
ENV POSTGRES_DB=db1
ENV POSTGRES_PASSWORD=postgres
ENV EMAIL_PORT=8200
ENV EMAIL_NAME=email

# Run the app
CMD ["java", "-jar", "api.jar"]
