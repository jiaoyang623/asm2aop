package guru.ioio.asm2aop

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import java.io.File

class Asm2AopTransform(
    private val config: TransformConfig,
) : Transform() {
    private val awaitableExecutor = if (config.enableMultiThread) WaitableExecutor.useGlobalSharedThreadPool() else null

    override fun getName() = "Asm2AopTransform"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return if (config.isProjectLibrary) TransformManager.PROJECT_ONLY else TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental() = true

    override fun transform(transformInvocation: TransformInvocation?) {
        println("AsmAop.transform($transformInvocation)")
        transformInvocation?.let { invocation ->
            transformClass(
                invocation.context,
                invocation.inputs,
                invocation.outputProvider,
                invocation.isIncremental
            )
        }
    }

    private fun transformClass(
        context: Context,
        inputs: Collection<TransformInput>,
        outputProvider: TransformOutputProvider,
        isIncremental: Boolean,
    ) {
        val startTime = System.currentTimeMillis()
        if (!isIncremental) {
            outputProvider.deleteAll()
        }
        //
        inputs.forEach { input ->
            input.jarInputs.forEach { jarInput ->
                awaitableExecutor?.apply {
                    execute { forEachJar(context, jarInput, outputProvider, isIncremental) }
                } ?: forEachJar(context, jarInput, outputProvider, isIncremental)
            }
            input.directoryInputs.forEach { directoryInput ->
                awaitableExecutor?.apply {
                    execute { forEachDirectory(context, directoryInput, outputProvider, isIncremental) }
                } ?: forEachDirectory(context, directoryInput, outputProvider, isIncremental)
            }
        }
        awaitableExecutor?.waitForTasksWithQuickFail<Any>(true)
        println("transform finished ${System.currentTimeMillis() - startTime}")
    }

    private fun forEachJar(
        context: Context,
        input: JarInput,
        output: TransformOutputProvider,
        isIncremental: Boolean
    ) {
        val destName = input.file.name.substringAfterLast(".jar") + '_' +
                DigestUtils.md5Hex(input.file.absolutePath).substring(0, 8)
        val destFile = output.getContentLocation(destName, input.contentTypes, input.scopes, Format.JAR)
//        println("forEachJar $isIncremental ${input.status} ${input.file.absolutePath} -> $destFile")
        if (isIncremental) {
            when (input.status) {
                Status.ADDED, Status.CHANGED -> {
                    transformJar(context, input, destFile)
                }
                Status.REMOVED -> {
                    if (destFile.exists()) {
                        destFile.delete()
                    }
                }
                else -> {
                }
            }
        } else {
            transformJar(context, input, destFile)
        }
    }

    private fun transformJar(context: Context, input: JarInput, output: File) {
//        println("transformJar")
    }

    private fun forEachDirectory(
        context: Context,
        input: DirectoryInput,
        output: TransformOutputProvider,
        isIncremental: Boolean
    ) {
        val src = input.file
        val dst = output.getContentLocation(input.name, input.contentTypes, input.scopes, Format.DIRECTORY)
        FileUtils.forceMkdir(dst)
        val srcPath = src.absolutePath
        val dstPath = dst.absolutePath

        println("forEachDirectory: $srcPath $dstPath")
        if (isIncremental) {
            input.changedFiles.forEach { (src, status) ->
                println("class: $status ${src.absolutePath}")
                val dst = File(src.absolutePath.replace(srcPath, dstPath))
                when (status) {
                    Status.ADDED, Status.CHANGED -> {

                    }
                    Status.REMOVED -> {
                        if (dst.exists()) {
                            dst.delete()
                        }
                    }
                }
            }
        } else {
            FileUtils.copyDirectory(src, dst)
            src.walk().filter { it.name.endsWith(".class") }.forEach { classFile ->
                forEachClass(context, src, classFile, srcPath, dstPath)
            }
        }
    }

    private fun forEachClass(
        context: Context,
        srcDir: File,
        srcFile: File,
        srcDirPath: String,
        dstDirPath: String
    ) {
        println("forEachClass ${srcFile.absolutePath}")
    }

//    private fun modifyClass()
}
