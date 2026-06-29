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
    implementation("com.discord4j:discord4j-core:3.3.2")

    implementation("org.slf4j:slf4j-simple:2.0.18")

    testImplementation(platform("org.junit:junit-bom:6.1.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
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
