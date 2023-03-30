package guru.ioio.asm2aop.asm

import guru.ioio.asm2aop.Asm2AopConst
import guru.ioio.asm2aop.MD5Utils
import guru.ioio.asm2aop.creator.IClassCreator
import guru.ioio.asm2aop.reader.TargetBean
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*

class CallMethodVisitor(
    methodVisitor: MethodVisitor?,
    private val mTargetList: List<TargetBean>,
    private val mClassSet: Set<String>,
    private val mMethodSet: Set<String>,
    private val classVisitor: ClassVisitor,
    private val classCreator: IClassCreator,
) : MethodVisitor(ASM5, methodVisitor) {

    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean
    ) {
        var beforeBean: TargetBean? = null
        var afterBean: TargetBean? = null
        var aroundBean: TargetBean? = null
        if (mClassSet.contains(owner) && mMethodSet.contains(name)) {
            println("CMV: $owner->$name (in)")
            val className = owner?.replace("/", ".")
            mTargetList.filter { it.injectClass == className && it.injectMethod == name && it.injectType == Asm2AopConst.INJECT_TYPE_CALL }
                .forEach {
                    when (it.executeType) {
                        Asm2AopConst.EXECUTE_TYPE_BEFORE -> beforeBean = it
                        Asm2AopConst.EXECUTE_TYPE_AFTER -> afterBean = it
                        Asm2AopConst.EXECUTE_TYPE_AROUND -> aroundBean = it
                    }
                }
        }
        beforeBean?.executeMethod?.let { method ->
            super.visitMethodInsn(INVOKESTATIC, Asm2AopConst.TARGET_CLASS_ASM, method, "()V", false)
        }
        if (aroundBean != null && owner != null && name != null) {
            makeAround(opcode, owner, name, descriptor, isInterface, aroundBean!!)
        } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }
        afterBean?.executeMethod?.let { method ->
            super.visitMethodInsn(INVOKESTATIC, Asm2AopConst.TARGET_CLASS_ASM, method, "()V", false)
        }
    }

    private fun makeAround(
        opcode: Int,
        executorClassName: String,
        executorMethodName: String,
        descriptor: String?,
        isInterface: Boolean,
        targetBean: TargetBean,
    ) {
        super.visitMethodInsn(opcode, executorClassName, executorMethodName, descriptor, isInterface)
        // call static or call special
//        val newName = "fc_" + MD5Utils.md5(descriptor ?: "").substring(0, 8)
//        val descriptorBean = DescriptorBean(descriptor)
//        val jointPointClassName = "$executorClassName$$executorMethodName$$newName${'$'}jp"
//
//        val callerClassName = ""
//        // create joint point
//        classCreator.create(
//            jointPointClassName,
//            CallJointPointGenerator(
//                jointPointClassName,
//                callerClassName,
//                newName,
//                executorClassName,
//                descriptorBean
//            ).generate()
//        )
//        // create target call method
//        classVisitor.visitMethod(ACC_PUBLIC, newName, descriptor, null, null).apply {
//            visitCode()
//            visitVarInsn(ALOAD, 0)//
//            AsmUtils.loadMethodParams(this, descriptorBean)
//
//            visitMaxs(1, 1)
//            visitEnd()
//        }
//        // visit method
    }

}
