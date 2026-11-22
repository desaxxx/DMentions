plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.6.1"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
    implementation("org.bstats:bstats-bukkit:3.2.1")

    implementation("dev.dash:loom:0.2.1")
    implementation("dev.yoshi:xconfig:0.4.1")

    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.116.3")
    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("dev.dash.loom", "dev.dash.dmentions.lib.loom")
    relocate("dev.yoshi.xconfig", "dev.dash.dmentions.lib.xconfig")
    relocate("org.bstats", "dev.dash.dmentions.lib.bstats")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
