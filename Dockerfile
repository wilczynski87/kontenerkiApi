FROM gradle:8.14-jdk21 AS build
WORKDIR /app

# Copy only files needed to download dependencies first (for caching)
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Copy rest of the project and build it
COPY . .

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the project
RUN ./gradlew clean shadowJar --no-daemon

# === Runtime stage ===
FROM eclipse-temurin:21-jre
WORKDIR /kontenerki

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/api.jar ./api.jar

# Expose the port the Ktor app runs on
EXPOSE 8100

# Evirnomental variables
ENV DB_HOST=localhost
ENV DB_NAME=db1
ENV DB_PASSWORD=postgres
ENV DB_PORT=5432
ENV DB_USER=admin_user
ENV EMAIL_HOST=email
ENV EMAIL_PORT=8200
ENV API_PORT=8100
ENV API_ENV=PROD

# Run the app
CMD ["java", "-jar", "api.jar"]

# docker build -t wilczynski87/konteneryapi . && docker push wilczynski87/konteneryapi