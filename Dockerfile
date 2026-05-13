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

# Run the app
CMD ["java", "-jar", "api.jar"]

# docker build -t wilczynski87/konteneryapi . && docker push wilczynski87/konteneryapi