package guru.ioio.asm2aop.creator

interface IClassCreator {
    fun create(className: String, data: ByteArray)
}
