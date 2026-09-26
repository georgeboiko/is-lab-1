plugins {
    id("war")
}

group = "itmo.is"
version = "1.0-SNAPSHOT"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("jakarta.platform:jakarta.jakartaee-web-api:11.0.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.war {
    archiveFileName = "lab1.war"
}
