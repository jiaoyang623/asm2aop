package guru.ioio.asm2aop.asm

import guru.ioio.asm2aop.Asm2AopConst
import guru.ioio.asm2aop.reader.TargetBean
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class MethodAdapter(api: Int, methodVisitor: MethodVisitor?, access: Int, name: String?, descriptor: String?) :
    AdviceAdapter(api, methodVisitor, access, name, descriptor) {
    private val mTargetClassName = "guru/ioio/asm2aop/AopTarget"
    var targetList: List<TargetBean>? = null
    override fun onMethodEnter() {
        val target = targetList?.firstOrNull { it.executeType == Asm2AopConst.EXECUTE_TYPE_BEFORE }
        target ?: return
        val method = target.executeMethod
        println("MA: before $method")
        if (method.isNotEmpty()) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, mTargetClassName, method, "()V", false)
        }
    }

    override fun onMethodExit(opcode: Int) {
        val target = targetList?.firstOrNull { it.executeType == Asm2AopConst.EXECUTE_TYPE_AFTER }
        target ?: return
        val method = target.executeMethod
        println("MA: after $method")
        if (method.isNotEmpty()) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, mTargetClassName, method, "()V", false)
        }
    }

}
