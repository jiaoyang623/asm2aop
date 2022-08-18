package guru.ioio.asm2aop

data class TransformConfig(
    val isProjectLibrary: Boolean,
    val enableMultiThread: Boolean = true,
    val disableJar: Boolean = false,
    val shouldModify: ((className: String) -> Boolean)? = null,
) {

}
