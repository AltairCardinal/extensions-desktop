package android.icu.text

/** JVM stub for android.icu.text.Normalizer2. */
abstract class Normalizer2 {
    abstract fun normalize(src: CharSequence): String
    abstract fun isNormalized(s: CharSequence): Boolean

    companion object {
        @JvmStatic fun getNFCInstance(): Normalizer2 = object : Normalizer2() {
            override fun normalize(src: CharSequence) = java.text.Normalizer.normalize(src, java.text.Normalizer.Form.NFC)
            override fun isNormalized(s: CharSequence) = java.text.Normalizer.isNormalized(s, java.text.Normalizer.Form.NFC)
        }
        @JvmStatic fun getNFDInstance(): Normalizer2 = object : Normalizer2() {
            override fun normalize(src: CharSequence) = java.text.Normalizer.normalize(src, java.text.Normalizer.Form.NFD)
            override fun isNormalized(s: CharSequence) = java.text.Normalizer.isNormalized(s, java.text.Normalizer.Form.NFD)
        }
        @JvmStatic fun getNFKCInstance(): Normalizer2 = object : Normalizer2() {
            override fun normalize(src: CharSequence) = java.text.Normalizer.normalize(src, java.text.Normalizer.Form.NFKC)
            override fun isNormalized(s: CharSequence) = java.text.Normalizer.isNormalized(s, java.text.Normalizer.Form.NFKC)
        }
    }
}
