plugins {
    kotlin("jvm") version "2.1.21"
}

group = "com.turbo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    // Core JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")      // @Test, @BeforeEach, etc.
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")   // Silnik testowy

    // Parametrized tests - TO JEST KLUCZOWE! 🎯
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")   // @ParameterizedTest

    // Kotlin support
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.20")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}