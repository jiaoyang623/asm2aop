package guru.ioio.asm2aop.reader

import com.android.build.api.transform.TransformInput
import guru.ioio.asm2aop.Asm2AopConst
import guru.ioio.asm2aop.asm.AopTargetVisitor
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.FileInputStream

class TargetReader {
    private val targetList = mutableListOf<TargetBean>()
    fun read(inputs: Collection<TransformInput>): List<TargetBean> {
        inputs.forEach { input ->
            input.directoryInputs.forEach { dirInput ->
                dirInput.file.walk().filter { it.absolutePath.endsWith(Asm2AopConst.TARGET_FILE) }
                    .forEach { readClass(it) }
            }
        }
        return targetList
    }

    private fun readClass(target: File) {
        println("TargetReader: ${target.absolutePath}")
        val inputStream = FileInputStream(target)
        val srcByteArray = IOUtils.toByteArray(inputStream)
        IOUtils.closeQuietly(inputStream)
        val classReader = ClassReader(srcByteArray)

        val node = ClassNode()
        classReader.accept(node, ClassReader.EXPAND_FRAMES)
        node.methods.forEach { method ->
            method.apply {
                invisibleAnnotations?.firstOrNull()?.apply {
                    val executeType = when (desc.replace(";", "").substringAfterLast("/")) {
                        "Before" -> Asm2AopConst.EXECUTE_TYPE_BEFORE
                        "After" -> Asm2AopConst.EXECUTE_TYPE_AFTER
                        "Around" -> Asm2AopConst.EXECUTE_TYPE_AROUND
                        else -> null
                    }
                    val query = values.filterIsInstance<String>()
                        .firstOrNull { it.startsWith("execution ") or it.startsWith("call ") }
                    val injectType = query?.let {
                        when {
                            it.startsWith("execution") -> Asm2AopConst.INJECT_TYPE_EXECUTION
                            it.startsWith("call") -> Asm2AopConst.INJECT_TYPE_CALL
                            else -> null
                        }
                    }
                    if (executeType != null && injectType != null) {
                        targetList.add(TargetBean(executeType, injectType, query, name))
                    }
                }
            }
        }
    }
}