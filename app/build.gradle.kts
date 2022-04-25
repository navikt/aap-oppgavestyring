import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    application
}

dependencies {
    implementation("io.ktor:ktor-server-core:1.6.8")
    implementation("io.ktor:ktor-server-netty:1.6.8")
    implementation("io.ktor:ktor-jackson:1.6.8")
    implementation("io.ktor:ktor-auth:1.6.8")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")
    implementation("io.ktor:ktor-metrics-micrometer:1.6.8")
    implementation("io.micrometer:micrometer-registry-prometheus:1.8.5")

    implementation("no.nav.security:token-validation-ktor:2.0.14")

    implementation("com.sksamuel.hoplite:hoplite-yaml:2.1.2")

    implementation("ch.qos.logback:logback-classic:1.2.11")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.1.1")

    implementation("org.apache.kafka:kafka-streams:3.1.0")
    implementation("org.apache.kafka:kafka-clients:3.1.0")
    implementation("io.confluent:kafka-streams-avro-serde:7.0.1") {
        exclude("org.apache.kafka", "kafka-clients")
    }

    implementation("org.postgresql:postgresql:42.3.4")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.flywaydb:flyway-core:8.5.9")
    implementation("com.github.seratch:kotliquery:1.7.0")

    implementation("com.github.navikt.aap-avro:manuell:3.0.8")
    implementation("com.github.navikt.aap-avro:sokere:3.0.8")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:1.6.8")
    testImplementation("no.nav.security:mock-oauth2-server:0.4.5")
    // used to override env var runtime
    testImplementation("uk.org.webcompere:system-stubs-jupiter:2.0.1")
    testImplementation("org.apache.kafka:kafka-streams-test-utils:7.1.1-ce")

    testImplementation("org.testcontainers:postgresql:1.17.1")
}

application {
    mainClass.set("no.nav.aap.app.AppKt")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("PASSED", "SKIPPED", "FAILED")
        }
    }
}
