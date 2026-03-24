package uy.kohesive.injekt

import android.app.Application
import eu.kanade.tachiyomi.network.NetworkHelper
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

object Injekt {
    private val factories = mutableMapOf<KClass<*>, () -> Any>(
        Json::class to {
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            }
        },
        NetworkHelper::class to { NetworkHelper() },
        Application::class to { Application() },
    )

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(clazz: KClass<T>): T {
        val factory = factories[clazz]
            ?: throw IllegalStateException("No desktop-api injection binding for ${clazz.qualifiedName}")
        return factory() as T
    }

    inline fun <reified T : Any> get(): T = get(T::class)

    fun <T : Any> register(clazz: KClass<T>, factory: () -> T) {
        factories[clazz] = factory
    }
}

inline fun <reified T : Any> injectLazy(): Lazy<T> = lazy { Injekt.get(T::class) }
