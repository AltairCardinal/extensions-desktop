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

dependencies {
    implementation(project(":android-compat"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.coroutines.core)
    implementation(libs.kotlin.protobuf)
    implementation(libs.kotlin.json)
    implementation(libs.rxjava)
    implementation(libs.jsoup)
    implementation(libs.okhttp)
}
