package guru.ioio.asm2aop.creator

import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream

class DirClassCreator(private val dirPath: String) : IClassCreator {
    override fun create(className: String, data: ByteArray) {
        // dirPath: /opt/git/github/public/asm2aop/android/app/build/intermediates/transforms/Asm2AopTransform/debug/52
        val absClassPath = dirPath + File.separator + className.substringBeforeLast(".").replace(".", "/") + ".class"
        val classFile = File(absClassPath)
        if (classFile.exists()) classFile.delete()
        val dirFile = File(absClassPath.substringBeforeLast("/"))
        if (!dirFile.exists()) {
            dirFile.mkdirs()
        }
        val out = FileOutputStream(classFile)
        IOUtils.write(data, out)
        IOUtils.closeQuietly(out)
    }
}
