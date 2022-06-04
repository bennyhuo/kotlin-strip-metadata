plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish") version "0.18.0"
}

dependencies {
    api("org.ow2.asm:asm:9.3")

    testImplementation("junit:junit:4.12")
}