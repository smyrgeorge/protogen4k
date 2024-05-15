import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    `java-library`
    signing
    kotlin("jvm")
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
    api("com.google.guava:guava:32.0.1-jre")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.16.0")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.0")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }

    publications {
        val archivesBaseName = tasks.jar.get().archiveBaseName.get()
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = archivesBaseName
            pom {
                name = archivesBaseName
                packaging = "jar"
                description = "A small proto file generator from kotlin data classes."
                url = "https://github.com/smyrgeorge/protogen4k"

                scm {
                    url = "https://github.com/smyrgeorge/protogen4k"
                    connection = "scm:git:https://github.com/smyrgeorge/protogen4k.git"
                    developerConnection = "scm:git:git@github.com:smyrgeorge/protogen4k.git"
                }

                licenses {
                    license {
                        name = "MIT License"
                        url = "https://github.com/smyrgeorge/protogen4k/blob/main/LICENSE"
                    }
                }

                developers {
                    developer {
                        name = "Yorgos S."
                        email = "smyrgoerge@gmail.com"
                        url = "https://smyrgeorge.github.io/"
                    }
                }
            }
        }
    }
}

signing {
    val signingKey = System.getenv("MAVEN_SIGNING_KEY")
    val signingPassword = System.getenv("MAVEN_SIGNING_PASSWORD")
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
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
