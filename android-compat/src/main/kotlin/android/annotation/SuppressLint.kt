package android.annotation

@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.CLASS, AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY, AnnotationTarget.FIELD,
    AnnotationTarget.CONSTRUCTOR, AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.EXPRESSION,
)
annotation class SuppressLint(vararg val value: String)
