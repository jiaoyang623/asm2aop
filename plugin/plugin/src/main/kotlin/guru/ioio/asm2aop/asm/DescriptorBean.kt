package guru.ioio.asm2aop.asm

class DescriptorBean(descriptor: String?) {
    val paramList = mutableListOf<String>()
    var returnType = "V"
        private set

    init {
        if (descriptor != null) {
            returnType = descriptor.substringAfterLast(")")
            val paramStr = descriptor.substring(1).substringBeforeLast(")")
            proceedParam(paramStr)
        }
    }

    private fun proceedParam(param: String) {
        param.isEmpty() && return
        val builder = java.lang.StringBuilder()
        var isClassLoading = false
        var isArrayLoading = false
        param.forEach { c ->
            if (isClassLoading) {
                builder.append(c)
                if (c == ';') {
                    paramList.add(builder.toString())
                    isClassLoading = false
                }
            } else {
                when (c) {
                    'Z', 'C', 'B', 'S', 'I', 'F', 'J', 'D' -> {
                        if (isArrayLoading) {
                            paramList.add(builder.append(c).toString())
                            isArrayLoading = false
                        } else {
                            paramList.add(c.toString())
                        }
                    }
                    'L' -> {
                        isClassLoading = true
                        builder.clear().append(c)
                    }
                    '[' -> {
                        builder.clear().append(c)
                        isArrayLoading = true
                    }
                }
            }
        }
    }

    override fun toString(): String {
        return "DescriptorBean(paramList=$paramList, returnType='$returnType')"
    }
}