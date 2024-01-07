import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
val kotlinVersion = "1.7.21"
plugins {
    kotlin("jvm") version "1.7.21"
    application
}

group = "ru.batonchik"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.1")

    implementation(kotlin("reflect"))

    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.register<Copy>("copyParams") {
    from("src/main/resources/params.properties")
    into(layout.buildDirectory.dir("libs"))
}

tasks.register<Jar>("uberJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveClassifier.set("uber")

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })

    manifest {
        attributes["Main-Class"] = "AceStreamPlaylistGenerator"
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "AceStreamPlaylistGenerator"
    }
}

tasks.named("build") { finalizedBy("copyParams") }
tasks.named("build") { finalizedBy("uberJar") }
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}