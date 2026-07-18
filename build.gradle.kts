import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "1.9.21"
    id("com.gradleup.shadow") version "9.2.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "io.github.black_Kittys22"
version = "1.6"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://maven.enginehub.org/repo/")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
    implementation("dev.jorel:commandapi-paper-shade:11.2.0")  // Paper-specific shade
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.4.0")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }

    shadowJar {
        archiveBaseName.set("Mortality")
        archiveVersion.set(version.toString())
        archiveClassifier.set("")
        mergeServiceFiles()
        relocate("dev.jorel.commandapi", "io.github.black_Kittys22.libs.commandapi")
        relocate("net.dv8tion", "io.github.black_Kittys22.libs.jda")
        relocate("com.mysql",   "io.github.black_Kittys22.libs.mysql")
        exclude("*.kotlin_module")
        exclude("META-INF/maven/**")
    }

    runServer {
        minecraftVersion("1.21.11")
        downloadPlugins {
            url("https://github.com/dmulloy2/ProtocolLib/releases/download/5.4.0/ProtocolLib.jar")
        }
    }
}