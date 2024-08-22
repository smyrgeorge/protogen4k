import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    // https://github.com/vanniktech/gradle-maven-publish-plugin
    id("com.vanniktech.maven.publish") version "0.29.0"
}

group = rootProject.group
version = rootProject.version

// "KotlinCompile" task must use the same version.
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    // IMPORTANT: must be last.
    mavenLocal()
}

dependencies {
    api(kotlin("reflect"))
    implementation("com.google.guava:guava:32.0.1-jre")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.16.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
}

mavenPublishing {
    coordinates(
        groupId = group as String,
        artifactId = name,
        version = version as String
    )

    pom {
        name = "actor4k"
        description = "A small actor system written in kotlin using Coroutines (kotlinx.coroutines)."
        url = "https://github.com/smyrgeorge/protogen4k"

        licenses {
            license {
                name = "MIT License"
                url = "https://github.com/smyrgeorge/protogen4k/blob/main/LICENSE"
            }
        }

        developers {
            developer {
                id = "smyrgeorge"
                name = "Yorgos S."
                email = "smyrgoerge@gmail.com"
                url = "https://smyrgeorge.github.io/"
            }
        }

        scm {
            url = "https://github.com/smyrgeorge/protogen4k"
            connection = "scm:git:https://github.com/smyrgeorge/protogen4k.git"
            developerConnection = "scm:git:git@github.com:smyrgeorge/protogen4k.git"
        }
    }

    // Configure publishing to Maven Central
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // Enable GPG signing for all publications
    signAllPublications()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    // Log each test.
    testLogging { events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED) }

    // Print a summary after test suite.
    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun beforeTest(testDescriptor: TestDescriptor) {}
        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}
        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            // Wll match the outermost suite.
            if (suite.parent == null) {
                println("\nTest result: ${result.resultType}")
                val summary = "Test summary: ${result.testCount} tests, " +
                        "${result.successfulTestCount} succeeded, " +
                        "${result.failedTestCount} failed, " +
                        "${result.skippedTestCount} skipped"
                println(summary)
            }
        }
    })
}
