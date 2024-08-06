import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    kotlin("jvm") version "2.0.10"
    id("io.ktor.plugin") version "2.3.12"
}

val aapLibVersion = "5.0.23"
val ktorVersion = "2.3.12"
val exposedVersion = "0.53.0"
val flywayVersion = "10.17.0"
val hikariVersion = "5.1.0"
val postgresqlVersion = "42.7.3"
val testcontainersVersion = "1.20.1"
val prometheusVersion= "1.13.2"
val logbackVersion = "1.5.6"
val jacksonVersion = "2.17.2"
val logstashVersion = "8.0"
val assertjVersion = "3.26.3"
val mockkVersion = "1.13.12"


application {
    mainClass.set("oppgavestyring.AppKt")
}

dependencies {
    implementation("dev.forst:ktor-openapi-generator:0.6.1")
    implementation("com.github.navikt.aap-libs:ktor-auth:$aapLibVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    // persistence
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    runtimeOnly("org.postgresql:postgresql:$postgresqlVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")

    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheusVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:$logstashVersion")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
}

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

//tasks.named<KotlinCompilationTask<*>>("compileKotlin").configure {
//    compilerOptions.javaClass.getMethod("set").invoke(JvmTarget.JVM_21)
//}
tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget = JvmTarget.JVM_21
    }

    // https://stackoverflow.com/questions/77228513/flyway-unrecognised-migration-name-format-when-run-in-docker
    withType<ShadowJar> {
        mergeServiceFiles()
    }

    withType<Test> {
        useJUnitPlatform()
    }
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDirs("main")
sourceSets["test"].resources.srcDirs("test")
