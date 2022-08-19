package guru.ioio.asm2aop.asm

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

class MainClassVisitor(classVisitor: ClassVisitor?) : ClassVisitor(Opcodes.ASM7, classVisitor) {
    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        println("visit $name, $superName, [${interfaces?.joinToString(",")}]")
        super.visit(version, access, name, signature, superName, interfaces)
    }
}
