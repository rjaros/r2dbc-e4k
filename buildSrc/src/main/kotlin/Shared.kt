import io.github.gradlenexus.publishplugin.NexusPublishExtension
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension

fun Project.repositories() {
    repositories {
        mavenCentral()
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

fun Project.setupSigning() {
    extensions.getByType<SigningExtension>().run {
        sign(extensions.getByType<PublishingExtension>().publications)
    }
}

fun Project.setupPublication() {
    extensions.getByType<PublishingExtension>().run {
        publications.withType<MavenPublication>().all {
            pom {
                defaultPom()
            }
        }
    }
    extensions.getByType<NexusPublishExtension>().run {
        repositories {
            sonatype {
                username.set(findProperty("ossrhUsername")?.toString())
                password.set(findProperty("ossrhPassword")?.toString())
            }
        }
    }
}
