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

val postgresqlDriver = configurations.create("postgresqlDriver")

dependencies {
    compileOnly("jakarta.platform:jakarta.jakartaee-web-api:10.0.0")
    implementation("org.mindrot:jbcrypt:0.4")
    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")

    add(postgresqlDriver.name, "org.postgresql:postgresql:42.7.10")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testCompileOnly("org.projectlombok:lombok:1.18.48")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.48")
}

tasks.test {
    useJUnitPlatform()
}

tasks.war {
    archiveFileName = "lab1.war"
}

tasks.register<Copy>("copyPostgresqlDriver") {
    from(postgresqlDriver)
    into(layout.buildDirectory.dir("glassfish/lib"))
}
