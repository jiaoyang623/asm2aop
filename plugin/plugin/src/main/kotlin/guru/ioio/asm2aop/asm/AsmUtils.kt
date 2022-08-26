package guru.ioio.asm2aop.asm

class AsmUtils {
    companion object {
        /**
         * @param returnType * or return types
         * @param params null -> all ok
         * */
        fun checkMethodDescription(returnType: String, params: List<String>?, descriptor: String?): Boolean {
            descriptor ?: return false
            val currentReturnType = descriptor.substringAfterLast(")")
            val currentParams = descriptor.substring(1).substringBeforeLast(")")
            // check return
            val targetReturnType = convertType(returnType)
            val targetParams = params?.joinToString { convertType(it) } ?: ".."
            if (returnType != "*" && targetReturnType != currentReturnType) {
                println("checkMethod: return error: $targetReturnType -> $currentReturnType")
                return false
            }
            if (params != null && targetParams != currentParams) {
                println("checkMethod: params error: $targetParams -> $currentParams")
                return false
            }

            // check params
            return true
        }

        fun convertType(javaType: String): String {
            val arrayIndex = javaType.indexOf("[")
            val arrayResult = if (arrayIndex == -1) "" else javaType.substring(arrayIndex).replace("]", "")
            val typePart = javaType.substringBefore("[")
            val typeResult = when (typePart) {
                "boolean" -> "Z"
                "char" -> "C"
                "byte" -> "B"
                "short" -> "S"
                "int" -> "I"
                "float" -> "F"
                "long" -> "J"
                "double" -> "D"
                "void" -> "V"
                else -> "L${typePart.replace(".", "/")};"
            }
            return arrayResult + typeResult
        }
    }
}
