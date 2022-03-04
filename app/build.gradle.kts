import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    application
}

dependencies {
    implementation("io.ktor:ktor-server-core:1.6.7")
    implementation("io.ktor:ktor-server-netty:1.6.7")
    implementation("io.ktor:ktor-metrics-micrometer:1.6.7")
    implementation("io.micrometer:micrometer-registry-prometheus:1.8.3")

    implementation("ch.qos.logback:logback-classic:1.2.10")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.0.1")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:1.6.7")
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
