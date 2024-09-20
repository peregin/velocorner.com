val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val exposed_version: String by project
val flyway_version: String by project

group = "velocorner.weather"
version = "1.0.0-SNAPSHOT"

plugins {
    application
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20"
    id("name.remal.check-updates") version "1.5.0"
    id("io.ktor.plugin") version "2.3.12"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

application {
    mainClass.set("velocorner.weather.ServiceKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-java:$ktor_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-html-builder:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging:$ktor_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposed_version")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.flywaydb:flyway-core:$flyway_version")
    implementation("org.flywaydb:flyway-database-postgresql:$flyway_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.0.20")
}

ktor {
    fatJar {
        archiveFileName = "service.jar"
    }

    // it is generated from the script, deploy.sh
    docker {
        jreVersion.set(JavaVersion.VERSION_17)
        customBaseImage = "openjdk:17-slim-buster"
        localImageName.set("velocorner.weather")
        imageTag.set("latest")
        portMappings.set(listOf(
            io.ktor.plugin.features.DockerPortMapping(
                9015,
                9015,
                io.ktor.plugin.features.DockerPortMappingProtocol.TCP
            )
        ))

        externalRegistry.set(
            io.ktor.plugin.features.DockerImageRegistry.dockerHub(
                appName = provider { "velocorner.weather" },
                username = provider { "peregin" },
                password = providers.environmentVariable("DOCKER_HUB_PASSWORD")
            )
        )
    }
}

tasks {
    shadowJar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        mergeServiceFiles()
        archiveBaseName.set("WeatherApp")
        manifest {
            attributes["Main-Class"] = "velocorner.weather.ServiceKt"
        }
    }
}
