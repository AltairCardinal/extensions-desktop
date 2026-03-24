package uy.kohesive.injekt.api

import uy.kohesive.injekt.Injekt

inline fun <reified T : Any> get(): T = Injekt.get(T::class)
