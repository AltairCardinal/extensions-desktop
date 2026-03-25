package android.icu.text

/** JVM stub for android.icu.text.RuleBasedCollator. */
class RuleBasedCollator(rules: String = "") : Collator() {
    var isCaseLevel: Boolean = false
    override var strength: Int = Collator.TERTIARY
    override var decomposition: Int = Collator.CANONICAL_DECOMPOSITION
    override fun compare(source: String, target: String): Int = source.compareTo(target, ignoreCase = true)
}
