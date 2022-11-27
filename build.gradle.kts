import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin")
    id("org.jetbrains.dokka") version "1.7.20"
}

repositories()

val javaVersion: String by project
val coroutinesVersion: String by project
val springDataR2dbcVersion: String by project
val r2dbcH2Version: String by project
val springBootVersion: String by project

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = javaVersion
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions.jvmTarget = javaVersion

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
    dependsOn("dokkaJavadoc")
    archiveClassifier.set("javadoc")
    from("$buildDir/dokka/javadoc")
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
