// JVM replacement for lib-multisrc/*.gradle.kts files.
plugins {
    kotlin("jvm")
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
    main {
        kotlin { srcDirs("src") }
        resources { srcDirs("res") }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":android-compat"))
    implementation(project(":desktop-api"))
    implementation(project(":lib:i18n"))
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.coroutines.core)
    compileOnly(libs.rxjava)
    compileOnly(libs.kotlin.protobuf)
    compileOnly(libs.kotlin.json)
    compileOnly(libs.jsoup)
    compileOnly(libs.okhttp)
}
