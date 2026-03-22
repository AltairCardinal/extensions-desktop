plugins {
    kotlin("jvm")
    id("kotlinx-serialization")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        freeCompilerArgs.add("-opt-in=kotlinx.serialization.ExperimentalSerializationApi")
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

sourceSets {
    named("main") {
        java.setSrcDirs(listOf("src"))
        resources.setSrcDirs(listOf("assets"))
    }
}

dependencies {
    compileOnly(versionCatalogs.named("libs").findBundle("common").get())
    implementation(project(":core"))
}
