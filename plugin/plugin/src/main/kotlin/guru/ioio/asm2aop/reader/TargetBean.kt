package guru.ioio.asm2aop.reader

data class TargetBean(
    val executeType: Int,
    val injectType: Int,
    val query: String,
    val method: String,
)
