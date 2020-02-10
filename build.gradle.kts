import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.2.4.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.asciidoctor.convert") version "1.5.8"
    id("org.jetbrains.kotlin.plugin.jpa") version "1.3.61"
    kotlin("jvm") version "1.3.61"
    kotlin("plugin.spring") version "1.3.61"
}

group = "refle"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

val developmentOnly by configurations.creating
configurations {
    runtimeClasspath {
        extendsFrom(developmentOnly)
    }
}

repositories {
    mavenCentral()
}

//extra["snippetsDir"] = file("build/generated-snippets")
val snippetsDir by extra { file("build/generated-snippets") }
val springCloudVersion by extra { "Hoxton.SR1" }

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.cloud:spring-cloud-starter:2.2.1.RELEASE")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.+")
    implementation("org.jasypt:jasypt:1.9.3")
    implementation("org.postgresql:postgresql:42.2.5")
    implementation("com.google.cloud:google-cloud-translate:1.94.2")
    implementation("io.sentry:sentry-logback:1.7.27")
//    implementation("io.sentry:sentry-spring:1.7.27")
    implementation("com.graphql-java:graphql-spring-boot-starter:5.0.2")
    implementation("com.graphql-java:graphql-java-tools:5.2.4")
    implementation("com.graphql-java:graphiql-spring-boot-starter:5.0.2")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-verifier")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.testcontainers:postgresql:1.10.6")
    testImplementation("com.playtika.testcontainers:embedded-postgresql:1.38")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")

}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.test {
    outputs.dir(snippetsDir)
}

tasks.asciidoctor {
    inputs.dir(snippetsDir)
    dependsOn(tasks.getByName("test"))
}

