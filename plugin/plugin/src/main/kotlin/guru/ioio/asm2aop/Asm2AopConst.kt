package guru.ioio.asm2aop

class Asm2AopConst {
    companion object {
        const val EXECUTE_TYPE_BEFORE = 0
        const val EXECUTE_TYPE_AFTER = 1
        const val EXECUTE_TYPE_AROUND = 2
        const val INJECT_TYPE_CALL = 0
        const val INJECT_TYPE_EXECUTION = 1

        const val TARGET_FILE = "guru/ioio/asm2aop/AopTarget.class"
        const val TARGET_CLASS = "guru.ioio.asm2aop.AopTarget"
    }
}