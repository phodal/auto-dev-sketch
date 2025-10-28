package cc.unitmesh.sketch.language.processor

<<<<<<< HEAD:exts/devins-lang/src/main/kotlin/cc/unitmesh/sketch/language/processor/ExecuteProcessor.kt
import cc.unitmesh.sketch.language.actions.DevInsRunFileAction
import cc.unitmesh.sketch.language.ast.action.PatternActionFuncDef
import cc.unitmesh.sketch.language.ast.action.PatternProcessor
import cc.unitmesh.sketch.language.compiler.error.DEVINS_ERROR
import cc.unitmesh.sketch.language.compiler.exec.RunInsCommand
import cc.unitmesh.sketch.language.utils.lookupFile
import cc.unitmesh.sketch.provider.RunService
import cc.unitmesh.sketch.language.startup.ShireActionStartupActivity
import cc.unitmesh.sketch.util.workerThread
=======
import cc.unitmesh.sketch.language.actions.DevInsRunFileAction
import cc.unitmesh.sketch.language.ast.action.PatternActionFuncDef
import cc.unitmesh.sketch.language.ast.action.PatternProcessor
import cc.unitmesh.sketch.language.compiler.error.DEVINS_ERROR
import cc.unitmesh.sketch.language.compiler.exec.idea.RunInsCommand
import cc.unitmesh.sketch.language.utils.lookupFile
import cc.unitmesh.sketch.provider.RunService
import cc.unitmesh.sketch.language.startup.ShireActionStartupActivity
import cc.unitmesh.sketch.util.workerThread
>>>>>>> master:exts/devins-lang/src/main/kotlin/cc/unitmesh/devti/language/processor/ExecuteProcessor.kt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object ExecuteProcessor : PatternProcessor {
    override val type: PatternActionFuncDef = PatternActionFuncDef.EXECUTE

    private val logger = logger<ExecuteProcessor>()

    fun execute(
        myProject: Project,
        filename: Any,
        variableNames: Array<String>,
        variableTable: MutableMap<String, Any?>,
    ): Any {
        val file = filename.toString()
        if (file.endsWith(".devin")) {
            return executeDevinFile(myProject, filename, variableNames, variableTable)
        }

        if (file.startsWith(":")) {
            CoroutineScope(workerThread).launch {
                RunInsCommand(myProject, file).execute()
            }
        }

        val virtualFile = myProject.lookupFile(file) ?: return "$DEVINS_ERROR: File not found: $filename"

        val runService = RunService.provider(myProject, virtualFile)
        return runService?.runFileAsync(myProject, virtualFile, null)
            ?: "$DEVINS_ERROR: [ExecuteProcessor] No run service found for file: $filename"
    }

    private fun executeDevinFile(
        myProject: Project,
        filename: Any,
        variableNames: Array<String>,
        variableTable: MutableMap<String, Any?>,
    ): String {
        try {
            val file = runReadAction {
                ShireActionStartupActivity.findShireFile(myProject, filename.toString())
            }

            if (file == null) {
                logger.warn("execute shire error: file not found")
                return ""
            }

            return DevInsRunFileAction.suspendExecuteFile(myProject, file, variableNames, variableTable) ?: ""
        } catch (e: Exception) {
            logger.warn("execute shire error: $e")
            return ""
        }
    }

}
