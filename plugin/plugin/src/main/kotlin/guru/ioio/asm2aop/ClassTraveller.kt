package guru.ioio.asm2aop

import com.android.build.api.transform.TransformInput
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import java.io.File
import java.io.FileInputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile

class ClassTraveller {
    val classMap = mutableMapOf<String, ClassBean>()
    fun travel(inputs: Collection<TransformInput>, isIncremental: Boolean) {
        inputs.forEach { input ->
            input.jarInputs.forEach { jarInput ->
                travelJar(jarInput?.file)
            }
            input.directoryInputs.forEach { dirInput ->
                travelDir(dirInput?.file)
            }
        }
    }

    private fun travelJar(jarFile: File?) {
        jarFile ?: return
        jarFile.length() == 0L && return

        val file = JarFile(jarFile, false)
        val entry = file.entries()
        while (entry.hasMoreElements()) {
            val jarEntry = entry.nextElement() as JarEntry
            val entryName = jarEntry.name
            if (entryName.endsWith(".DSA") || entryName.endsWith(".SF")) continue // ignore signature file

            try {
                IOUtils.toByteArray(file.getInputStream(jarEntry))
            } catch (t: Throwable) {
                t.printStackTrace()
                null
            }?.let { travelClass(it) }
        }
    }

    private fun travelDir(dirFile: File?) {
        dirFile?.walk()?.filter { it.name.endsWith(".class") }?.forEach { classFile ->
            val inputStream = FileInputStream(classFile)
            val srcByteArray = IOUtils.toByteArray(inputStream)
            IOUtils.closeQuietly(inputStream)
            travelClass(srcByteArray)
        }
    }

    private fun travelClass(classData: ByteArray) {
        try {
            val reader = ClassReader(classData)
            val bean = ClassBean(
                reader.className.replace("/", "."),
                reader.superName.replace("/", "."),
                reader.interfaces.toList().map { it.replace("/", ".") }
            )
            classMap[bean.name] = bean
        } catch (t: Throwable) {

        }
    }

    fun instanceOf(childName: String, parent: String): Boolean {
        val bean = classMap[childName] ?: return false
        mutableListOf<String>().apply {
            add(bean.name)
            add(bean.ext)
            addAll(bean.impl)
        }.forEach { p ->
            return if (p == parent) true else instanceOf(p, parent)
        }
        return false
    }
}
