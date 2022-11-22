
plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.21"
    id("com.adarshr.test-logger") version "3.0.0"
    id("maven-publish")


    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    `maven-publish`
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "no.kommune.oslo.automatiserteprosesser"
            artifactId = "maskinporten-klient"
            version = "1.3"

            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/oslokommune/maskinporten-klient")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {

    implementation(group = "com.nimbusds", name = "nimbus-jose-jwt", version = "9.0")
    implementation(group = "com.squareup.okhttp3", name = "okhttp", version = "4.9.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.1")
    implementation(group = "org.slf4j", name = "slf4j-api", version = "1.7.32")




    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

testlogger {
    this.theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
}
