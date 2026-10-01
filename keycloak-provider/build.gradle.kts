plugins {
    `java-library`
}

group = "com.khalibre.keycloak"
version = "1.0.0"

val keycloakVersion = "26.1.3"
val jbossLoggingVersion = "3.6.1.Final"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    compileOnly("org.keycloak:keycloak-server-spi:$keycloakVersion")
    compileOnly("org.keycloak:keycloak-server-spi-private:$keycloakVersion")
    compileOnly("org.keycloak:keycloak-core:$keycloakVersion")
    compileOnly("org.keycloak:keycloak-services:$keycloakVersion")
    compileOnly("org.keycloak:keycloak-model-infinispan:$keycloakVersion")
    compileOnly("org.jboss.logging:jboss-logging:$jbossLoggingVersion")
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }
}

val providersDir = rootProject.layout.projectDirectory.dir("../keycloak-providers").asFile

// keycloak-providers is bind-mounted at /opt/keycloak/providers, so every jar the container needs
// must land there. Third-party jars are committed under libs/ rather than left as manual copies.
val copyJarToProviders by tasks.registering(Copy::class) {
    group = "build"
    description = "Copies the built provider jar and the vendored libs/*.jar into ./keycloak-providers."

    from(tasks.named("jar")) {
        rename { "${project.name}-${project.version}.jar" }
    }
    from(layout.projectDirectory.dir("libs")) {
        include("*.jar")
    }
    into(providersDir)
}

tasks.named("assemble") {
    finalizedBy(copyJarToProviders)
}

tasks.named<Delete>("clean") {
    delete(providersDir)
}
