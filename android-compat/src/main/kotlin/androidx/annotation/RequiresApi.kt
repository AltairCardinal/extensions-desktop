package androidx.annotation

/** JVM stub for @RequiresApi — no-op on desktop. */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.BINARY)
annotation class RequiresApi(val value: Int = 1, val api: Int = 1)
