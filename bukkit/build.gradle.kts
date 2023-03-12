repositories {
    maven {
        name = "spigot-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "papermc"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    maven {
        name = "jitpack.io"
        url = uri("https://jitpack.io/")
    }
    maven {
        name = "dmulloy2-repo"
        url = uri("https://repo.dmulloy2.net/repository/public/")
    }
    maven {
        name = "placeholderapi"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    maven {
        name = "viaversion-repo"
        url = uri("https://repo.viaversion.com/")
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")

    implementation("io.github.rothes.rslib:bukkit:0.1.0-SNAPSHOT")

    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.1")
    compileOnly("com.viaversion:viaversion-api:4.5.1")
}

tasks.shadowJar {
    archiveFileName.set("RsAtPlayer-Bukkit-${project.version}-All.jar")
    archiveBaseName.set("RsAtPlayer-Bukkit")

    minimize()

//    relocate("io.github.rothes.rslib", "io.github.rothes.atplayer.lib.io.github.rothes.rslib")
    relocate("io.github.rothes.rslib", "io.github.rothes.atplayer.rslib")
    relocate("org.yaml", "io.github.rothes.atplayer.lib.org.yaml")
    relocate("net.kyori", "io.github.rothes.atplayer.lib.net.kyori")
    relocate("org.bstats", "io.github.rothes.atplayer.lib.org.bstats")
    relocate("org.simpleyaml", "io.github.rothes.atplayer.lib.org.simpleyaml")
    relocate("kotlin", "io.github.rothes.atplayer.lib.kotlin")
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(project.properties)
    }
    filesMatching("build-metadata.yml") {
        expand(project.properties)
    }
}

gradle.buildFinished {
    // Clean Gradle resources cache
    deleteDirectory(File(project.buildDir, "resources/"))
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