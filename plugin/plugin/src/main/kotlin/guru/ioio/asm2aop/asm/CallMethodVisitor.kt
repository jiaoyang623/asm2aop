package guru.ioio.asm2aop.asm

import guru.ioio.asm2aop.Asm2AopConst
import guru.ioio.asm2aop.reader.TargetBean
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ASM5

class CallMethodVisitor(
    methodVisitor: MethodVisitor?,
    private val mTargetList: List<TargetBean>,
    private val mClassSet: Set<String>,
    private val mMethodSet: Set<String>,
) :
    MethodVisitor(ASM5, methodVisitor) {
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
            super.visitMethodInsn(Opcodes.INVOKESTATIC, Asm2AopConst.TARGET_CLASS_ASM, method, "()V", false)
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        afterBean?.executeMethod?.let { method ->
            super.visitMethodInsn(Opcodes.INVOKESTATIC, Asm2AopConst.TARGET_CLASS_ASM, method, "()V", false)
        }
    }

}
