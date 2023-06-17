plugins {
    java
    id("com.github.johnrengelman.shadow") version("7.1.2")
}

allprojects {
    apply {
        plugin("java")
        plugin("com.github.johnrengelman.shadow")
    }

    group = "net.blueberrymc.viablueberry"
    version = "0.0.1-SNAPSHOT"

    tasks.withType<JavaExec>().configureEach {
        javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    repositories {
        mavenCentral()
        maven { url = uri("https://repo.blueberrymc.net/repository/maven-public/") }
        maven { url = uri("https://repo.viaversion.com/") }
    }

    dependencies {
        implementation("com.viaversion:viaversion:4.7.0") { isTransitive = false }
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }

        jar {
            manifest.attributes(
                "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
                "MixinConfigs" to "mixins.viablueberry.json",
            )
        }

        test {
            useJUnitPlatform()
        }
    }
}

subprojects {
    tasks {
        shadowJar {
            archiveBaseName.set("${project.parent!!.name}-${project.name}")
        }
    }
}
