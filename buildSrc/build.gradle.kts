plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.21")
    implementation("io.github.gradle-nexus:publish-plugin:1.1.0")
    implementation(gradleApi())
}
