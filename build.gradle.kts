plugins {
    id("java")
}

group = "itmo.is"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("jakarta.enterprise:jakarta.enterprise.cdi-api:4.1.0")
    implementation("org.jboss.weld.se:weld-se-core:6.0.4.Final")

    implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
    implementation("org.eclipse.persistence:org.eclipse.persistence.jpa:5.0.1")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
