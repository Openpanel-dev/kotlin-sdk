plugins {
    kotlin("jvm") version "1.9.20"
    `application`
    `java`
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.json:json:20210307")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.20")
}

application {
    mainClass.set("dev.openpanel.OpenPanelTestKt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.register<JavaExec>("runTest") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("dev.openpanel.OpenPanelTestKt")
    group = "verification"
    description = "Run the OpenPanelTest"
}
