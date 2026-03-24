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
    implementation(project(":android-compat"))
    implementation(project(":core"))
    implementation(project(":desktop-api"))
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.3.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    compileOnly("io.reactivex:rxjava:1.3.8")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.7.3")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    compileOnly("org.jsoup:jsoup:1.22.1")
    compileOnly("com.squareup.okhttp3:okhttp:5.3.2")
}
