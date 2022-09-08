package guru.ioio.asm2aop.asm

import guru.ioio.asm2aop.Asm2AopConst
import guru.ioio.asm2aop.MD5Utils
import guru.ioio.asm2aop.creator.IClassCreator
import guru.ioio.asm2aop.reader.TargetBean
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*


class MethodAroundGenerator(
    private val cv: ClassVisitor,
    private val targetBean: TargetBean,
    private val className: String,
    private val access: Int,
    private val name: String?,
    private val descriptor: String?,
    private val signature: String?,
    private val exceptions: Array<out String>?,
    private val classCreator: IClassCreator
) {

    private val newName = "f_" + MD5Utils.md5(descriptor ?: "").substring(0, 8)
    private val descriptorBean = DescriptorBean(descriptor)
    private val jointPointClassName = "$className$$name$$newName${'$'}jp"

    fun generate(): MethodVisitor {
        println("MAG: $newName")
        classCreator.create(
            jointPointClassName,
            JointPointGenerator(jointPointClassName, className, newName, descriptorBean).generate()
        )
        changeOld()
        return genNew()
    }

    private fun changeOld() {
        println("MAG: $className.$name() $descriptor -> $descriptorBean")
        cv.visitMethod(access, name, descriptor, signature, exceptions).apply {
            visitCode()
            val localStart = 1 + descriptorBean.paramList.size
            val paramsPos = localStart + 0
            val jpPos = localStart + 1
//            val outPos = localStart + 2
            // call method with descriptor
            AsmUtils.args2Array(this, descriptorBean)
            visitVarInsn(ASTORE, paramsPos)
            callInject(this, paramsPos, jpPos)
            // call AopTarget.fxx()
            visitVarInsn(ALOAD, jpPos)
            visitMethodInsn(
                INVOKESTATIC,
                Asm2AopConst.TARGET_CLASS_ASM,
                targetBean.executeMethod,
                Asm2AopConst.TARGET_AROUND_DESCRIPTOR,
                false
            )
            visitInsn(DUP)
            // call end
            if (descriptorBean.returnType == "V") {
                visitInsn(RETURN)
            } else {
                val nullLabel = Label()
                visitJumpInsn(IFNULL, nullLabel)
                // parse not null
                AsmUtils.castObject2Type(this, descriptorBean.returnType)
                AsmUtils.callMethodReturn(this, descriptorBean)
                // parse null
                visitLabel(nullLabel)
                AsmUtils.returnNull(this, descriptorBean)
            }
            visitMaxs(1, 1)
            visitEnd()
        }
    }

    // stack [this, args]
    private fun callInject(mv: MethodVisitor, paramsPos: Int, jpPos: Int) {
        // call inject
        val jpClass = jointPointClassName.replace(".", "/")
        mv.apply {
            // new joint point
            visitTypeInsn(NEW, jpClass)
            visitInsn(DUP)
            visitMethodInsn(INVOKESPECIAL, jpClass, "<init>", "()V", false)
            visitVarInsn(ASTORE, jpPos)
            //
            visitVarInsn(ALOAD, jpPos)
            visitVarInsn(ALOAD, paramsPos)
            visitFieldInsn(PUTFIELD, jpClass, "args", "[Ljava/lang/Object;")
            visitVarInsn(ALOAD, jpPos)
            visitVarInsn(ALOAD, 0)
            visitFieldInsn(PUTFIELD, jpClass, "target", "Ljava/lang/Object;")
        }
    }

    private fun genNew(): MethodVisitor {
        return cv.visitMethod(ACC_PUBLIC, newName, descriptor, signature, exceptions)
    }
}
