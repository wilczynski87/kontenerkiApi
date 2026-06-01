plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

kotlin {
    jvmToolchain(21)
}

group = "com.kontenery"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.postgresql)
    implementation(libs.h2)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
//    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    // TESTY:
    testImplementation(libs.ktor.server.test.host)
//    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlin.test) // alias na kotlin-test + JUnit support
    testImplementation(libs.kotlin.test.junit.jupiter)

    // https://mvnrepository.com/artifact/org.jetbrains.exposed/exposed-dao
    implementation(libs.exposed.dao)
    implementation(libs.ktor.server.request.validation)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
//    implementation(libs.kontenerki.library)
    implementation(files("libs/library-1.0.1.jar"))
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.status.pages)

    //Test
    implementation(libs.io.mockk)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.ktor.client.mock)

}

private fun String.trimEnvQuotes(): String {
    val trimmed = trim()
    if (trimmed.length >= 2) {
        val quote = trimmed.first()
        if ((quote == '"' || quote == '\'') && trimmed.last() == quote) {
            return trimmed.substring(1, trimmed.length - 1)
        }
    }
    return trimmed
}

tasks.named<JavaExec>("run") {
    val envFilePath = System.getenv("ENV_FILE")
    val envFile = when {
        envFilePath != null -> file(envFilePath).takeIf { it.exists() }
        else -> file(".env").takeIf { it.exists() }
    }

    if (envFile != null) {
        envFile.readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .mapNotNull { line ->
                val parts = line.split("=", limit = 2)
                if (parts.size == 2) parts[0].trim() to parts[1].trim()
                else null
            }
            .forEach { (key, value) ->
                environment(key, value.trimEnvQuotes())
            }
        println("Loaded env from: ${envFile.absolutePath}")
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "io.ktor.server.netty.EngineMain"
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveFileName.set("api.jar")
    manifest {
        attributes["Main-Class"] = "io.ktor.server.netty.EngineMain"
    }
}
