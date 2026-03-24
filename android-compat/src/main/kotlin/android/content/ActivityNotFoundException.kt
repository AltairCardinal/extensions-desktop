package android.content

class ActivityNotFoundException : RuntimeException {
    constructor() : super()
    constructor(message: String?) : super(message)
}
