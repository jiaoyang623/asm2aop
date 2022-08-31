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
                getLoadCode(param)?.let { code ->
                    println("AU: in $param $code $index")
                    mv.visitVarInsn(code, index + 1)
                }
            }
        }

        fun getLoadCode(param: String): Int? {
            return when (if (param.startsWith("[")) param.substring(0, 2) else param.substring(0, 1)) {
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

        fun args2Array(mv: MethodVisitor, descriptor: DescriptorBean) {
            mv.apply {
                // make array
                visitIntInsn(BIPUSH, descriptor.paramList.size)
                visitTypeInsn(ANEWARRAY, "java/lang/Object")

                for (i in 0 until descriptor.paramList.size) {
                    val itemDesc = descriptor.paramList[i]
                    val code = getLoadCode(itemDesc)
                    code ?: continue
                    visitInsn(DUP)
                    visitIntInsn(BIPUSH, i)
                    visitVarInsn(code, i + 1)
                    castType2Object(mv, itemDesc)
                    visitInsn(AASTORE)
                }
            }
        }

        //        "boolean" -> "Z"
//        "char" -> "C"
//        "byte" -> "B"
//        "short" -> "S"
//        "int" -> "I"
//        "long" -> "J"
//        "float" -> "F"
//        "double" -> "D"
        private fun castType2Object(mv: MethodVisitor, desc: String) {
            println("AU: castType: $desc")
            when (desc) {
                "Z" -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false)
                "C" -> mv.visitMethodInsn(
                    INVOKESTATIC,
                    "java/lang/Character",
                    "valueOf",
                    "(C)Ljava/lang/Character;",
                    false
                )
                "B" -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false)
                "S" -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false)
                "I" -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
                "J" -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false)
                "F" -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false)
                "D" -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false)
            }
        }

        fun castObject2Type(methodVisitor: MethodVisitor, desc: String) {
            when (desc) {
                "Z" -> { // boolean
                    methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                }
                "C" -> { // char
                    methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Character");
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
                }
                "B" -> { // byte
                    methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Byte");
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false);
                }
                "S" -> { // short
                    methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Short");
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false);
                }
                "I" -> { // int
                    methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Integer")
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false)
                }
                "J" -> {// long
                    methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Long");
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
                }
                "F" -> { // float
                    methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Float");
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
                }
                "D" -> { // double
                    methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Double");
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
                }
            }
        }

    }
}
