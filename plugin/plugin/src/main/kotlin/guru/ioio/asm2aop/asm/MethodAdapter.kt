package guru.ioio.asm2aop.asm

import guru.ioio.asm2aop.Asm2AopConst
import guru.ioio.asm2aop.reader.TargetBean
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class MethodAdapter(api: Int, methodVisitor: MethodVisitor?, access: Int, name: String?, descriptor: String?) :
    AdviceAdapter(api, methodVisitor, access, name, descriptor) {
    private val mTargetClassName = "guru/ioio/asm2aop/AopTarget"
    var target: TargetBean? = null
    override fun onMethodEnter() {
        if (target?.executeType != Asm2AopConst.EXECUTE_TYPE_BEFORE) {
            return
        }
        val method = target?.executeMethod
        println("MA: before $method")
        if (!method.isNullOrEmpty()) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, mTargetClassName, method, "()V", false)
        }
    }

    override fun onMethodExit(opcode: Int) {
        if (target?.executeType != Asm2AopConst.EXECUTE_TYPE_AFTER) {
            return
        }
        val method = target?.executeMethod
        println("MA: after $method")
        if (!method.isNullOrEmpty()) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, mTargetClassName, method, "()V", false)
        }
    }
}
