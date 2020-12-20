import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.withType

fun Project.repositories() {
    repositories {
        mavenCentral()
        jcenter()
        maven { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
        maven { url = uri("https://kotlin.bintray.com/kotlinx") }
        maven { url = uri("https://dl.bintray.com/kotlin/kotlin-js-wrappers") }
        maven { url = uri("https://dl.bintray.com/kotlin/kotlin-dev") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        maven { url = uri("https://dl.bintray.com/rjaros/kotlin") }
        mavenLocal()
    }
}

fun MavenPom.defaultPom() {
    name.set("r2dbc-e4k")
    description.set("Kotlin extensions for Spring Data R2DBC")
    url.set("https://github.com/rjaros/r2dbc-e4k")
    licenses {
        license {
            name.set("MIT")
            url.set("https://opensource.org/licenses/MIT")
        }
    }
    developers {
        developer {
            id.set("rjaros")
            name.set("Robert Jaros")
            organization.set("Treksoft")
            organizationUrl.set("http://www.treksoft.pl")
        }
    }
    scm {
        url.set("https://github.com/rjaros/r2dbc-e4k.git")
        connection.set("scm:git:git://github.com/rjaros/r2dbc-e4k.git")
        developerConnection.set("scm:git:git://github.com/rjaros/r2dbc-e4k.git")
    }
}

fun Project.setupPublication() {
    plugins.apply("maven-publish")

    extensions.getByType<PublishingExtension>().run {
        publications.withType<MavenPublication>().all {
            pom {
                defaultPom()
            }
        }

        repositories {
            maven {
                url = uri("https://api.bintray.com/maven/rjaros/kotlin/${project.name}/;publish=0;override=1")
                credentials {
                    username = findProperty("buser")?.toString()
                    password = findProperty("bkey")?.toString()
                }
            }
        }
    }
}
