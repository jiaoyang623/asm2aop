package guru.ioio.asm2aop.asm

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class AopTargetVisitor : ClassVisitor(Opcodes.ASM7) {

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        if (name in listOf("<init>", "<clinit>")) return null

        println("target visit: $name $descriptor $signature")
        return null
    }
}