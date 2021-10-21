plugins {
    kotlin("jvm") version "1.5.10"
    java
    `maven-publish`
    `java-library`
}

group = "com.vandenbreemen"
version = "1.0.0.1000"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    val log4jVersion = "1.2.14"
    implementation("log4j:log4j:$log4jVersion")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}


//  Based on https://github.com/gradle/kotlin-dsl-samples/blob/master/samples/maven-publish/build.gradle.kts
val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}