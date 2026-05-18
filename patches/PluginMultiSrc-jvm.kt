import keiyoushi.gradle.extensions.alias
import keiyoushi.gradle.extensions.implementation
import keiyoushi.gradle.extensions.libs
import keiyoushi.gradle.extensions.plugins
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

@Suppress("UNUSED")
class PluginMultiSrc : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        plugins {
            kotlin("jvm")
            kotlin("plugin.serialization")
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
                resources.setSrcDirs(listOf("res", "assets"))
            }
        }

        dependencies {
            compileOnly(libs.bundles.common)
            implementation(project(":core"))
            implementation(project(":android-compat"))
            implementation(project(":desktop-api"))
        }
    }
}
