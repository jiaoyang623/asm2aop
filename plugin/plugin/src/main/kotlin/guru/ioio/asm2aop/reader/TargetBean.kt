package guru.ioio.asm2aop.reader

data class TargetBean(
    val executeType: Int, // before after around
    val injectType: Int, // call execution
    val query: String,
    val executeMethod: String,
) {
    val resultType: String
    val injectClass: String
    val injectMethod: String

    /**
     * @param
     *  null: any params
     *  emptyList: no params
     *  list: specified params
     * */
    val params: List<String>?

    init {
        val segments = query.split(" ")
        resultType = segments[1]
        val nameParams = segments[2].split("(")
        val classMethod = nameParams[0]
        val methodString = nameParams[1].replace(")", "")
        injectMethod = classMethod.substringAfterLast(".")
        injectClass = classMethod.substringBeforeLast(".")
        params = when (methodString) {
            "" -> emptyList()
            ".." -> null
            else -> methodString.split(",").toList()
        }
    }

    override fun toString(): String {
        return "TargetBean(executeType=$executeType, injectType=$injectType, query='$query', method='$executeMethod', resultType='$resultType', className='$injectClass', injectMethod='$injectMethod', params=$params)"
    }

}
