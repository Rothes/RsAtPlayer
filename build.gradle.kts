plugins {
    kotlin("jvm") version "1.8.0"
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "kotlin-platform-jvm")
    apply(plugin = "com.github.johnrengelman.shadow")

    group = "io.github.rothes"
    version = properties["versionString"] ?: "Unknown"

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }

    tasks.test {
        useJUnitPlatform()
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    }
}

gradle.buildFinished {
    // Clean Gradle resources cache
    deleteDirectory(project.buildDir)
}

fun deleteDirectory(directory: File) {
    val contents = directory.listFiles();
    if (contents != null) {
        for (file in contents) {
            deleteDirectory(file);
        }
    }
    directory.delete()
}