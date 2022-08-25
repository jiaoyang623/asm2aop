package guru.ioio.asm2aop.asm

import guru.ioio.asm2aop.reader.TargetBean
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class MainClassVisitor(
    classVisitor: ClassVisitor?,
    private val targetList: List<TargetBean>
) :
    ClassVisitor(Opcodes.ASM7, classVisitor) {
    private var mTargetBean: TargetBean? = null
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
        val targets = targetList.filter { it.injectClass == className || it.injectClass == superClassName }
        mTargetBean = targets.firstOrNull()
        if (className == "guru.ioio.asm2aop.demo.MainActivity") {
            println("visit $name, $superName, [${interfaces?.joinToString(",")}], $targets")
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
        println("MCV: $mTargetBean, $name")
        return if (name != null && mTargetBean?.injectMethod == name) {
            val methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
            MethodAdapter(Opcodes.ASM5, methodVisitor, access, name, descriptor).apply {
                target = mTargetBean
            }
        } else {
            super.visitMethod(access, name, descriptor, signature, exceptions)
        }
    }
}
