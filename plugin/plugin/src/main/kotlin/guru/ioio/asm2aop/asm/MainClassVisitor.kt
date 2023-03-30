package guru.ioio.asm2aop.asm

import guru.ioio.asm2aop.Asm2AopConst
import guru.ioio.asm2aop.ClassTraveller
import guru.ioio.asm2aop.asm.AsmUtils.Companion.checkMethodDescription
import guru.ioio.asm2aop.creator.IClassCreator
import guru.ioio.asm2aop.reader.TargetBean
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class MainClassVisitor(
    classVisitor: ClassVisitor?,
    private val targetList: List<TargetBean>,
    private val classCreator: IClassCreator,
    private val classTraveller: ClassTraveller,
) :
    ClassVisitor(Opcodes.ASM7, classVisitor) {
    private var mClassTargetList: List<TargetBean>? = null
    private val mTargetClassSet = targetList.map { it.injectClass.replace(".", "/") }.toHashSet()
    private val mTargetMethodSet = targetList.map { it.injectMethod }.toHashSet()
    private var mClass: String? = null
    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        if (name == null || superName == null) {
            super.visit(version, access, name, signature, superName, interfaces)
            return
        }
        mClass = name
        val className = name.replace("/", ".")
        mClassTargetList = targetList.filter { classTraveller.instanceOf(className, it.injectClass) }
        if (className == "guru.ioio.asm2aop.demo.MainActivity") {
            println("visit $name, $superName, [${interfaces?.joinToString(",")}], $mClassTargetList")
        }
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val className = mClass
        val methodTargetList =
            mClassTargetList?.filter {
                it.injectMethod == name && checkMethodDescription(it.resultType, it.params, descriptor)
            }
        return if (name != null && !methodTargetList.isNullOrEmpty() && className != null) {// execution
            val aroundBean = methodTargetList.firstOrNull { it.executeType == Asm2AopConst.EXECUTE_TYPE_AROUND }
            if (aroundBean == null) { // before or after
                val methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
                MethodEdgeAdapter(Opcodes.ASM5, methodVisitor, access, name, descriptor).apply {
                    targetList = methodTargetList
                }
            } else { // around
                println("around: $name")
                // 生成新方法
                MethodAroundGenerator(
                    cv,
                    aroundBean,
                    className,
                    access,
                    name,
                    descriptor,
                    signature,
                    exceptions,
                    classCreator
                ).generate()
            }
        } else {
            super.visitMethod(access, name, descriptor, signature, exceptions)
        }.let { mv ->
            CallMethodVisitor(
                mv,
                targetList.filter { it.injectType == Asm2AopConst.INJECT_TYPE_CALL },
                mTargetClassSet,
                mTargetMethodSet,
                this@MainClassVisitor,
                classCreator,
            )
        }
    }

}
