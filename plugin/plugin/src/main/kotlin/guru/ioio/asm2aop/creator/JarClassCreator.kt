package guru.ioio.asm2aop.creator

import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class JarClassCreator(private val jarOutputStream: JarOutputStream) : IClassCreator {
    override fun create(className: String, data: ByteArray) {
        // entry name: guru/ioio/asm2aop/demo/R$drawable.class
        println("JCC $className")
        val dstEntry = JarEntry(className.substringBeforeLast(".").replace(".", "/") + ".class")
        jarOutputStream.apply {
            putNextEntry(dstEntry)
            write(data)
            closeEntry()
        }
    }
}
