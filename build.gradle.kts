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

val glassfishJdbcLibs = configurations.create("glassfishJdbcLibs")

dependencies {
    compileOnly("jakarta.platform:jakarta.jakartaee-web-api:10.0.0")
    implementation("org.mindrot:jbcrypt:0.4")
    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")

    add(glassfishJdbcLibs.name, "org.postgresql:postgresql:42.7.10")
    add(glassfishJdbcLibs.name, "com.alibaba:druid:1.2.28")

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

tasks.register<Sync>("copyGlassfishJdbcLibs") {
    from(glassfishJdbcLibs)
    include("postgresql-*.jar", "druid-*.jar")
    into(layout.buildDirectory.dir("glassfish/lib"))
}
