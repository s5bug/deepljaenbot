plugins {
    id("java")
    id("application")
}

group = "tf.bug"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.discord4j:discord4j-core:3.2.8")

    implementation("org.slf4j:slf4j-simple:2.0.17")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.12.2")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "tf.bug.deepljaenbot.DeepLJaEnBot"
    }
}

application {
    mainClass = "tf.bug.deepljaenbot.DeepLJaEnBot"
}

tasks.test {
    useJUnitPlatform()
}
