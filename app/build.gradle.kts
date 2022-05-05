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

    implementation("com.github.navikt.aap-libs:kafka:0.0.43")

    implementation("no.nav.security:token-validation-ktor:2.0.14")

    implementation("com.sksamuel.hoplite:hoplite-yaml:2.1.2")

    implementation("ch.qos.logback:logback-classic:1.2.11")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.1.1")

    implementation("org.postgresql:postgresql:42.3.4")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.flywaydb:flyway-core:8.5.10")
    implementation("com.github.seratch:kotliquery:1.7.0")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:1.6.8")
    testImplementation("no.nav.security:mock-oauth2-server:0.4.5")
    testImplementation("uk.org.webcompere:system-stubs-jupiter:2.0.1")
    testImplementation("com.github.navikt.aap-libs:kafka-test:0.0.43")

    testImplementation("org.testcontainers:postgresql:1.17.1")
}

application {
    mainClass.set("no.nav.aap.app.AppKt")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "18"
    }

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("PASSED", "SKIPPED", "FAILED")
        }
    }
}
