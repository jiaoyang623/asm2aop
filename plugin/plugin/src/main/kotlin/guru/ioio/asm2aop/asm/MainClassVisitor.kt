package guru.ioio.asm2aop.asm

import guru.ioio.asm2aop.asm.AsmUtils.Companion.checkMethodDescription
import guru.ioio.asm2aop.reader.TargetBean
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class MainClassVisitor(
    classVisitor: ClassVisitor?,
    private val targetList: List<TargetBean>
) :
    ClassVisitor(Opcodes.ASM7, classVisitor) {
    private var mClassTargetList: List<TargetBean>? = null
    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        if (name == null || superName == null) {
            super.visit(version, access, name, signature, superName, interfaces)
            return
        }
        val className = name.replace("/", ".")
        val superClassName = superName.replace("/", ".")
        mClassTargetList = targetList.filter { it.injectClass == className || it.injectClass == superClassName }
        if (className == "guru.ioio.asm2aop.demo.MainActivity") {
            println("visit $name, $superName, [${interfaces?.joinToString(",")}], $mClassTargetList")
        }
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        println("MCV: $name, $descriptor")
        val methodTargetList =
            mClassTargetList?.filter {
                it.injectMethod == name && checkMethodDescription(it.resultType, it.params, descriptor)
            }
        return if (name != null && !methodTargetList.isNullOrEmpty()) {// execution
            val methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
            MethodAdapter(Opcodes.ASM5, methodVisitor, access, name, descriptor).apply {
                targetList = methodTargetList
            }
        } else {
            super.visitMethod(access, name, descriptor, signature, exceptions)
        }
    }

}
