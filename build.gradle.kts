plugins {
    kotlin("jvm") version "1.6.20" apply false
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
}

subprojects {
    repositories {
        maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
        maven("https://packages.confluent.io/maven/")
        maven("https://jitpack.io")
        mavenCentral()
    }

    configurations.all {
        resolutionStrategy {
            force(
                "org.apache.kafka:kafka-clients:3.1.0",
                "org.rocksdb:rocksdbjni:6.29.4.1"
            )
        }
    }
}
