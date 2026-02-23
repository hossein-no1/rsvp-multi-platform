package com.util.rsvp.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.StandardCopyOption

abstract class ExportDesktopInstallerTask : DefaultTask() {
    @get:Input
    abstract val requiredOsSubstring: Property<String>

    @get:Input
    abstract val installerExtension: Property<String>

    @get:Input
    abstract val outputFileName: Property<String>

    @get:Input
    abstract val packagingTaskCandidates: ListProperty<String>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val binariesDir: DirectoryProperty

    @get:OutputDirectory
    abstract val distDir: DirectoryProperty

    @get:Internal
    protected val osName: String
        get() = System.getProperty("os.name") ?: ""

    @TaskAction
    fun run() {
        val required = requiredOsSubstring.get()
        if (!osName.contains(required, ignoreCase = true)) {
            throw GradleException("This task must be run on $required (current OS: $osName).")
        }

        val candidates = packagingTaskCandidates.getOrElse(emptyList())
        val packagingTaskExists = candidates.any { project.tasks.findByName(it) != null }
        if (candidates.isNotEmpty() && !packagingTaskExists) {
            throw GradleException(
                "No packaging task found (expected one of: ${candidates.joinToString()}). " +
                    "Run ':composeApp:tasks --all' to see available tasks."
            )
        }

        val ext = installerExtension.get()
        val binariesRoot = binariesDir.get().asFile
        if (!binariesRoot.exists()) {
            throw GradleException("Missing binaries directory: ${binariesRoot.absolutePath}")
        }

        val matches = binariesRoot.walkTopDown()
            .filter { it.isFile && it.extension.equals(ext, ignoreCase = true) }
            .toList()

        val installer = when (matches.size) {
            0 -> throw GradleException("No .$ext found under: ${binariesRoot.absolutePath}")
            1 -> matches.single()
            else -> matches.maxBy { it.lastModified() }
        }

        val outDir = distDir.get().asFile
        outDir.mkdirs()

        val outFile = outDir.resolve(outputFileName.get())
        Files.copy(installer.toPath(), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

        logger.lifecycle("Exported installer to: ${outFile.absolutePath}")
    }
}

