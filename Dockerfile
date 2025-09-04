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
EXPOSE 8100

# Evirnomental variables
#ENV DB_URL=jdbc:postgresql://localhost:5431/db1 DB_USER=postgres POSTGRES_PASSWORD=postgres
#ENV DB_URL=jdbc:postgresql://localhost:5431/db1; DB_HOST=localhost; DB_NAME=db1; DB_PORT=5431; DB_USER=postgres; POSTGRES_DB=db1; POSTGRES_PASSWORD=postgres; EMAIL_PORT=200; EMAIL_NAME=email
ENV DB_HOST=localhost; DB_NAME=db1; DB_PORT=5432; DB_USER=admin_user; POSTGRES_DB=db1; POSTGRES_PASSWORD=postgres; EMAIL_PORT=8200; EMAIL_NAME=email


# Command to run the app
CMD ["java", "-jar", "api.jar"]
