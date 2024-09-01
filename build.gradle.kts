plugins {
    `maven-publish`
}

allprojects {
    group = "dev.openpanel"
    version = "0.0.1"

    repositories {
        mavenCentral()
        google() // Add Google Maven repository
    }
}

subprojects {
    apply(plugin = "maven-publish")

    // Only configure the Java plugin if it is applied
    afterEvaluate {
        if (plugins.hasPlugin("java")) {
            configure<JavaPluginExtension> {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(17)) // Use JDK 17 for building
                }
            }

            tasks.withType<JavaCompile>().configureEach {
                options.release.set(17)
            }

            publishing {
                publications {
                    create<MavenPublication>("maven") {
                        from(components["java"])
                    }
                }
            }
        }
    }
}
