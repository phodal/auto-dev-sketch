<<<<<<<< HEAD:exts/devins-lang/src/main/kotlin/cc/unitmesh/sketch/language/compiler/exec/RunInsCommand.kt
package cc.unitmesh.sketch.language.compiler.exec
========
package cc.unitmesh.sketch.language.compiler.exec.idea
>>>>>>>> master:exts/devins-lang/src/main/kotlin/cc/unitmesh/devti/language/compiler/exec/idea/RunInsCommand.kt

import cc.unitmesh.sketch.command.InsCommand
import cc.unitmesh.sketch.command.dataprovider.BuiltinCommand
import cc.unitmesh.sketch.language.compiler.error.DEVINS_ERROR
import cc.unitmesh.sketch.language.utils.lookupFile
import cc.unitmesh.sketch.provider.AutoTestService
import cc.unitmesh.sketch.provider.ProjectRunService
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager

/**
 * The `RunAutoCommand` class is responsible for executing an auto command on a given project.
 *
 * @property myProject The project to execute the auto command on.
 * @property argument The name of the file to find and run tests for.
 *
 */
class RunInsCommand(val myProject: Project, private val argument: String) : InsCommand {
    override val commandName: BuiltinCommand = BuiltinCommand.RUN

    override suspend fun execute(): String? {
        val task = ProjectRunService.Companion.all().mapNotNull { projectRun ->
            val hasTasks = projectRun.tasks(myProject).any { task -> task.contains(argument) }
            if (hasTasks) projectRun else null
        }

        if (task.isNotEmpty()) {
            task.first().run(myProject, argument)
            return "Task run successfully: $argument"
        }

        val virtualFile = myProject.lookupFile(argument.trim()) ?: return "${DEVINS_ERROR}: File not found: $argument"
        try {
            val psiFile: PsiFile = runReadAction { PsiManager.getInstance(myProject).findFile(virtualFile) }
                    ?: return "${DEVINS_ERROR}: File not found: $argument"

            val testService =
                AutoTestService.Companion.context(psiFile) ?: return "${DEVINS_ERROR}: No test service found for file: $argument"

            return testService.runFileAsync(myProject, virtualFile, null)
        } catch (e: Exception) {
            return "${DEVINS_ERROR}: ${e.message}"
        }
    }
}