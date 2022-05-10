plugins {
    id("com.github.johnrengelman.shadow")
    application
}

application {
    mainClass.set("no.nav.aap.app.AppKt")
}

dependencies {
    implementation("io.ktor:ktor-server-auth:2.0.1")
    implementation("io.ktor:ktor-server-auth-jwt:2.0.1")
    implementation("io.ktor:ktor-server-content-negotiation:2.0.1")
    implementation("io.ktor:ktor-server-metrics-micrometer:2.0.1")
    implementation("io.ktor:ktor-server-netty:2.0.1")

    implementation("io.ktor:ktor-serialization-jackson:2.0.1")

    implementation("com.github.navikt.aap-libs:kafka:0.0.43")
    implementation("com.github.navikt.aap-libs:ktor-utils:0.0.43")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")
    implementation("io.micrometer:micrometer-registry-prometheus:1.8.5")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.11")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.1.1")

    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("com.github.seratch:kotliquery:1.7.0")
    implementation("org.flywaydb:flyway-core:8.5.10")
    runtimeOnly("org.postgresql:postgresql:42.3.4")

    testImplementation(kotlin("test"))
    testImplementation("com.github.navikt.aap-libs:kafka-test:0.0.43")
    testImplementation("io.ktor:ktor-server-test-host:2.0.1")
    testImplementation("io.ktor:ktor-client-content-negotiation:2.0.1")
    testImplementation("com.nimbusds:nimbus-jose-jwt:9.22")
    testImplementation("org.testcontainers:postgresql:1.17.1")
}
