/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package guru.ioio.asm2aop

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A simple 'hello world' plugin.
 */
class Asm2AopPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        println("Asm2AopPlugin.apply()")
        val extension = if (project.plugins.hasPlugin("com.android.application")) {
            project.extensions.findByType(AppExtension::class.java)
        } else if (project.plugins.hasPlugin("com.android.library")) {
            project.extensions.findByType(LibraryExtension::class.java)
        } else {
            null
        }
        val config = TransformConfig(
            isProjectLibrary = extension is LibraryExtension,
            enableMultiThread = true,
            shouldModify = {
//                it.startsWith("guru.ioio")
                true
            },
        )
        extension?.registerTransform(Asm2AopTransform(config))
    }
}
