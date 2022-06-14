plugins {
    id("com.github.johnrengelman.shadow")
    application
}

application {
    mainClass.set("no.nav.aap.app.AppKt")
}

dependencies {
    implementation("io.ktor:ktor-server-auth:2.0.2")
    implementation("io.ktor:ktor-server-auth-jwt:2.0.2")
    implementation("io.ktor:ktor-server-content-negotiation:2.0.2")
    implementation("io.ktor:ktor-server-metrics-micrometer:2.0.2")
    implementation("io.ktor:ktor-server-netty:2.0.2")

    implementation("io.ktor:ktor-client-logging:2.0.2")
    implementation("io.ktor:ktor-client-core:2.0.2")

    implementation("io.ktor:ktor-serialization-jackson:2.0.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")

    implementation("com.github.navikt.aap-libs:ktor-client-auth:2.0.2")
    implementation("com.github.navikt.aap-libs:kafka:2.0.2")
    implementation("com.github.navikt.aap-libs:ktor-utils:2.0.2")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")
    implementation("io.micrometer:micrometer-registry-prometheus:1.9.0")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.11")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.2")

    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("com.github.seratch:kotliquery:1.8.0")
    implementation("org.flywaydb:flyway-core:8.5.12")
    runtimeOnly("org.postgresql:postgresql:42.4.0")

    testImplementation(kotlin("test"))
    testImplementation("com.github.navikt.aap-libs:kafka-test:2.0.2")
    testImplementation("io.ktor:ktor-server-test-host:2.0.2")
    testImplementation("io.ktor:ktor-client-content-negotiation:2.0.2")
    testImplementation("com.nimbusds:nimbus-jose-jwt:9.23")
    testImplementation("org.testcontainers:postgresql:1.17.2")
}
