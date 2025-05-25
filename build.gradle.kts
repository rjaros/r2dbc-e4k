plugins {
    kotlin("jvm")
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin")
    id("org.jetbrains.dokka") version "2.0.0"
}

repositories()

val javaVersion: String by project
val coroutinesVersion: String by project
val springDataR2dbcVersion: String by project
val r2dbcH2Version: String by project
val springBootVersion: String by project

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    implementation("org.springframework.data:spring-data-r2dbc:$springDataR2dbcVersion")
    implementation("io.r2dbc:r2dbc-h2:$r2dbcH2Version")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-data-r2dbc:$springBootVersion")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(kotlin.sourceSets.main.get().kotlin)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn("dokkaGenerate")
    archiveClassifier.set("javadoc")
    from(layout.buildDirectory.dir("dokka/html"))
}

publishing {
    publications {
        create<MavenPublication>("kotlin") {
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }
}

setupSigning()
setupPublication()

tasks.withType<Test> {
    testLogging {
    }
}
