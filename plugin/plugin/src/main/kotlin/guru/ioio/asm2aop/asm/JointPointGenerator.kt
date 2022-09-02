package guru.ioio.asm2aop.asm

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*

class JointPointGenerator(
    private val className: String,
    private val targetClass: String,
    private val targetMethod: String,
    private val descriptorBean: DescriptorBean,
) {
    fun generate(): ByteArray {
        val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        val name = className.replace(".", "/")
        val father = "guru/ioio/asm2aop/aoptools/annotation/JointPoint"
        classWriter.visit(
            V1_7,
            ACC_PUBLIC or ACC_SUPER,
            name,
            null,
            father,
            null
        )

        // constructor
//        classWriter.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/Object;Ljava/lang/Object;)V", null, null).apply {
//            visitCode()
//            visitVarInsn(ALOAD, 0)
//            visitMethodInsn(INVOKESPECIAL, father, "<init>", "()V", false)
//            visitVarInsn(ALOAD, 0)
//            visitVarInsn(ALOAD, 1)
//            visitFieldInsn(PUTFIELD, name, "args", "[Ljava/lang/Object;")
//            visitVarInsn(ALOAD, 0)
//            visitVarInsn(ALOAD, 2)
//            visitFieldInsn(PUTFIELD, name, "target", "Ljava/lang/Object;")
//            visitMaxs(2, 3)
//            visitEnd()
//        }

        // execute
        val target = targetClass.replace(".", "/")
        classWriter.visitMethod(ACC_PUBLIC, "execute", "([Ljava/lang/Object;)Ljava/lang/Object;", null, null).apply {
            visitCode()
            // call target
            visitVarInsn(ALOAD, 0)
            visitFieldInsn(GETFIELD, name, "target", "Ljava/lang/Object;")
            visitTypeInsn(CHECKCAST, target)
            visitVarInsn(ALOAD, 1)
            visitMethodInsn(
                INVOKEVIRTUAL,
                target,
                targetMethod,
                "([Ljava/lang/Object;)${descriptorBean.returnType}",
                false
            )
            // check cast to object
            AsmUtils.castType2Object(this, descriptorBean.returnType)
            // return object
            visitInsn(ARETURN)
            visitMaxs(2, 2)
            visitEnd()
        }

        classWriter.visitEnd()

        return classWriter.toByteArray()
    }
}