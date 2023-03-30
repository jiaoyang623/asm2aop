package guru.ioio.asm2aop.asm

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*

class CallJointPointGenerator(
    private val className: String,
    private val callerClass: String,
    private val callerMethod: String,
    private val executorClass: String,
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
        val caller = callerClass.replace(".", "/")
        val executor = executorClass.replace(".", "/")
        classWriter.visitMethod(ACC_PUBLIC, "execute", "([Ljava/lang/Object;)Ljava/lang/Object;", null, null).apply {
            visitCode()
            // call target
            visitVarInsn(ALOAD, 0)
            visitFieldInsn(GETFIELD, name, "caller", "Ljava/lang/Object;")
            visitTypeInsn(CHECKCAST, caller)
            // load executor
            visitVarInsn(ALOAD, 0)
            visitFieldInsn(GETFIELD, name, "executor", "Ljava/lang/Object;")
            visitTypeInsn(CHECKCAST, executor)

            // array to args
            for (i in 0 until descriptorBean.paramList.size) {
                visitVarInsn(ALOAD, 1) // array
                visitIntInsn(BIPUSH, i) // index
                visitInsn(AALOAD) // load item to stack
                AsmUtils.castObject2Type(this, descriptorBean.paramList[i])
            }
            visitMethodInsn(
                INVOKEVIRTUAL,
                caller,
                callerMethod,
                descriptorBean.descriptor?.replace("(","(Ljava/lang/Object;"),
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
