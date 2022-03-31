plugins {
    java
    `java-library`
    id("me.champeau.jmh") version "0.6.1"
    `maven-publish`
}

group = "net.kjp12"
version = "0.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    modularity.inferModulePath.set(true)
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    api("it.unimi.dsi", "fastutil", "8.5.6")
    compileOnly("org.jetbrains", "annotations", "19.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

jmh {
    profilers.add("jfr")
    warmupIterations.set(5)
    iterations.set(10)
    fork.set(2)
}

tasks {
    "jmh" { outputs.upToDateWhen { false } }
    jmhJar { outputs.upToDateWhen { false } }
    jmhCompileGeneratedClasses { outputs.upToDateWhen { false } }
    jmhRunBytecodeGenerator { outputs.upToDateWhen { false } }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.isDeprecation = true
        options.isWarnings = true
    }
    test {
        useJUnitPlatform()
    }
}
