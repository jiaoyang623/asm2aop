package guru.ioio.asm2aop.asm

import guru.ioio.asm2aop.reader.TargetBean
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import java.util.*

class MethodAroundGenerator(
    private val cv: ClassVisitor,
    private val targetBean: TargetBean,
    private val className: String,
    private val access: Int,
    private val name: String?,
    private val descriptor: String?,
    private val signature: String?,
    private val exceptions: Array<out String>?
) {
    private val newName = "f_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8)
    private val newAroundName = newName + "_a"
    private val descriptorBean = DescriptorBean(descriptor)
    private val aroundDescriptor = "([Ljava/lang/Object;)${descriptorBean.returnType}"

    fun generate(): MethodVisitor {
        println("MAG: $newName")
        genAround()
        changeOld()
        return genNew()
    }

    private fun changeOld() {
        println("MAG: $className.$name() $descriptor -> $descriptorBean")
        cv.visitMethod(access, name, descriptor, signature, exceptions).apply {
            visitCode()
            // call method with descriptor
            visitVarInsn(ALOAD, 0)
            AsmUtils.args2Array(this, descriptorBean)
            visitMethodInsn(INVOKESPECIAL, className, newAroundName, aroundDescriptor, false);
            AsmUtils.callMethodReturn(this, descriptorBean)
            visitMaxs(1, 1)
            visitEnd()
        }
    }

    private fun genAround() {
        cv.visitMethod(
            ACC_PRIVATE,
            newAroundName,
            aroundDescriptor,
            signature,
            exceptions
        ).apply {
            visitCode()
            visitVarInsn(ALOAD, 0) // for call
            for (i in 0 until descriptorBean.paramList.size) {
                visitVarInsn(ALOAD, 1) // array
                visitIntInsn(BIPUSH, i) // index
                visitInsn(AALOAD) // load item to stack
                AsmUtils.castObject2Type(this, descriptorBean.paramList[i])
            }
            visitMethodInsn(INVOKESPECIAL, className, newName, descriptor, false);
            AsmUtils.callMethodReturn(this, descriptorBean)
            visitMaxs(1, 1)
            visitEnd()
        }
    }


    private fun genNew(): MethodVisitor {
        return cv.visitMethod(ACC_PRIVATE, newName, descriptor, signature, exceptions)
    }
}
