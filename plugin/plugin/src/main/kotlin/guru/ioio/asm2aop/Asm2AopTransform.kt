package guru.ioio.asm2aop

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import guru.ioio.asm2aop.asm.MainClassVisitor
import guru.ioio.asm2aop.creator.DirClassCreator
import guru.ioio.asm2aop.creator.IClassCreator
import guru.ioio.asm2aop.creator.JarClassCreator
import guru.ioio.asm2aop.reader.TargetBean
import guru.ioio.asm2aop.reader.TargetReader
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

class Asm2AopTransform(
    private val config: TransformConfig,
) : Transform() {
    private val awaitableExecutor = if (config.enableMultiThread) WaitableExecutor.useGlobalSharedThreadPool() else null
    private var mTargetList = emptyList<TargetBean>()

    override fun getName() = "Asm2AopTransform"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return if (config.isProjectLibrary) TransformManager.PROJECT_ONLY else TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental() = true

    override fun transform(transformInvocation: TransformInvocation?) {
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
        // read target list
        mTargetList = TargetReader().read(inputs)
        println(mTargetList)
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
        config.disableJar && return
        modifyJar(input.file, context.temporaryDir)?.let {
            FileUtils.moveFile(it, output)
        } ?: let { FileUtils.copyFile(input.file, output) }
    }

    private fun forEachDirectory(
        context: Context,
        input: DirectoryInput,
        output: TransformOutputProvider,
        isIncremental: Boolean
    ) {
        val srcDir = input.file
        val dstDir = output.getContentLocation(input.name, input.contentTypes, input.scopes, Format.DIRECTORY)
        FileUtils.forceMkdir(dstDir)
        val srcPath = srcDir.absolutePath
        val dstPath = dstDir.absolutePath

        println("AAT: dirPath: $dstPath")
        val creator = DirClassCreator(dstPath)
        if (isIncremental) {
            input.changedFiles.forEach { (src, status) ->
                val dst = File(src.absolutePath.replace(srcPath, dstPath))
                when (status) {
                    Status.ADDED, Status.CHANGED -> {
                        forEachClass(context, srcDir, src, srcPath, dstPath, creator, true)
                    }
                    Status.REMOVED -> {
                        if (dst.exists()) {
                            dst.delete()
                        }
                    }
                }
            }
        } else {
            FileUtils.copyDirectory(srcDir, dstDir) // not only class files
            srcDir.walk().filter { it.name.endsWith(".class") }.forEach { classFile ->
                forEachClass(context, srcDir, classFile, srcPath, dstPath, creator)
            }
        }
    }

    private fun forEachClass(
        context: Context,
        srcDir: File,
        srcFile: File,
        srcDirPath: String,
        dstDirPath: String,
        classCreator: IClassCreator,
        copyOnFailed: Boolean = false,
    ) {
        val targetFile = File(srcFile.absolutePath.replace(srcDirPath, dstDirPath)).apply {
            exists() && delete()
        }

        modifyClass(srcDir, srcFile, context.temporaryDir, classCreator)?.let {
            FileUtils.moveFile(it, targetFile)
        } ?: let {
            if (copyOnFailed) {
                FileUtils.copyFile(srcFile, targetFile)
            }
        }

    }

    private fun modifyClass(dir: File, classFile: File, tempDir: File, classCreator: IClassCreator): File? {
        val className = classFile.absolutePath.replace(dir.absolutePath + File.separator, "")
            .replace(File.separator, ".").replace(".class", "")
        return if (config.shouldModify?.invoke(className) == false) {
            null
        } else {
            //read
            val inputStream = FileInputStream(classFile)
            val srcByteArray = IOUtils.toByteArray(inputStream)
            IOUtils.closeQuietly(inputStream)
            // modify
            modifyClass(className, srcByteArray, classCreator)?.let { dstByteArray ->
                // write
                File(tempDir, UUID.randomUUID().toString() + ".class").apply {
                    exists() && delete()
                    createNewFile()
                    IOUtils.closeQuietly(FileOutputStream(this).apply { write(dstByteArray) })
                }
            }
        }
    }

    private fun modifyJar(srcJar: File, tempDir: File): File? {
        srcJar.length() == 0L && return null
        val file = JarFile(srcJar, false)
        val dstJar = File(tempDir, DigestUtils.md5Hex(srcJar.absolutePath).substring(0, 8) + srcJar.name)
        val jos = JarOutputStream(FileOutputStream(dstJar))
        val entry = file.entries()
        val creator = JarClassCreator(jos)
        while (entry.hasMoreElements()) {
            val jarEntry = entry.nextElement() as JarEntry
            val entryName = jarEntry.name
            if (entryName.endsWith(".DSA") || entryName.endsWith(".SF")) continue // ignore signature file

//            println("AAT: jarEntry: ${jarEntry.name}")
            // read src
            val srcByteArray = try {
                IOUtils.toByteArray(file.getInputStream(jarEntry))
            } catch (t: Throwable) {
                t.printStackTrace()
                return null
            }

            // modify
            val dstByteArray = if (!jarEntry.isDirectory && entryName.endsWith(".class")) {
                val className = entryName.replace("/", ".").replace(".class", "")
                if (config.shouldModify?.invoke(className) == false) {
                    srcByteArray
                } else {
                    modifyClass(className, srcByteArray, creator) ?: srcByteArray
                }
            } else {
                srcByteArray
            }

            // write dst
            val dstEntry = JarEntry(entryName)
            jos.putNextEntry(dstEntry)
            jos.write(dstByteArray)
            jos.closeEntry()
        }
        jos.close()
        file.close()

        return dstJar
    }

    private fun modifyClass(className: String, srcClass: ByteArray, classCreator: IClassCreator): ByteArray? {
        return try {
            val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
            val classVisitor = MainClassVisitor(classWriter, mTargetList, classCreator)
            var classReader = ClassReader(srcClass)
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
            classWriter.toByteArray()
        } catch (t: Throwable) {
            t.printStackTrace()
            srcClass
        }
    }

}
