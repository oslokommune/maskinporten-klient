val nimbusJoseJwtVersion = "10.0.1"
val okhttpVersion = "4.12.0"
val jacksonModuleKotlinVersion = "2.18.2"
val slf4jApiVersion = "2.0.16"
val awsSdkSsmVersion = "2.30.13"
val kotlinBomVersion = "1.9.25"
val junitJupiterVersion = "5.11.4"
val mockWebServerVersion = "4.12.0"

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
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
            groupId = "no.kommune.oslo.origo"
            artifactId = "maskinporten-klient"
            version = "1.5.2-SNAPSHOT"

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
    mavenCentral()
}

dependencies {
    implementation(group = "com.nimbusds", name = "nimbus-jose-jwt", version = nimbusJoseJwtVersion)
    implementation(group = "com.squareup.okhttp3", name = "okhttp", version = okhttpVersion)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonModuleKotlinVersion")
    implementation(group = "org.slf4j", name = "slf4j-api", version = slf4jApiVersion)
    implementation(group = "software.amazon.awssdk", name = "ssm", version = awsSdkSsmVersion)

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinBomVersion"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
    testImplementation("com.squareup.okhttp3:mockwebserver:$mockWebServerVersion")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}