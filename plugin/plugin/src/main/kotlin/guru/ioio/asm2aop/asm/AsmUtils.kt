package guru.ioio.asm2aop.asm

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*

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

        fun loadMethodParams(mv: MethodVisitor, descriptor: DescriptorBean) {
            println("AU: in: $descriptor")
            descriptor.paramList.forEachIndexed { index, param ->
//                int ILOAD = 21; // visitVarInsn
//                int LLOAD = 22; // -
//                int FLOAD = 23; // -
//                int DLOAD = 24; // -
//                int ALOAD = 25; // -

//                int IALOAD = 46; // visitInsn
//                int LALOAD = 47; // -
//                int FALOAD = 48; // -
//                int DALOAD = 49; // -
//                int AALOAD = 50; // -
//                int BALOAD = 51; // -
//                int CALOAD = 52; // -
//                int SALOAD = 53; // -
                val p = if (param.startsWith("[")) param.substring(0, 2) else param.substring(0, 1)
                when (p) {
                    "Z", "C", "B", "S", "I" -> ILOAD
                    "J" -> LLOAD
                    "F" -> FLOAD
                    "D" -> DLOAD
                    "L" -> ALOAD
                    "[I" -> IALOAD
                    "[J" -> LALOAD
                    "[F" -> FALOAD
                    "[D" -> DALOAD
                    "[L" -> AALOAD
                    "[B" -> ALOAD//BALOAD
                    "[C" -> ALOAD//CALOAD
                    "[S" -> ALOAD//SALOAD
                    else -> null
                }?.let { code ->
                    println("AU: in $p $code $index")
                    mv.visitVarInsn(code, index + 1)
                }
            }
        }

        //            int IRETURN = 172; // visitInsn
//            int LRETURN = 173; // -
//            int FRETURN = 174; // -
//            int DRETURN = 175; // -
//            int ARETURN = 176; // -
//            int RETURN = 177; // -
        fun callMethodReturn(mv: MethodVisitor, descriptor: DescriptorBean) {
            when (descriptor.returnType.firstOrNull()) {
                'I' -> IRETURN
                'J' -> LRETURN
                'F' -> FRETURN
                'D' -> DRETURN
                'V' -> RETURN
                else -> ARETURN
            }.let { code ->
                mv.visitInsn(code)
            }
        }
    }
}
