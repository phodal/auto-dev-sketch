package cc.unitmesh.sketch.language.run.runner

import cc.unitmesh.sketch.devins.PostFunction
import cc.unitmesh.sketch.language.ast.HobbitHole
import cc.unitmesh.sketch.language.run.DevInsConfiguration
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

data class ShireLlmExecutorContext(
    val configuration: DevInsConfiguration,
    val processHandler: ProcessHandler,
    val console: ConsoleView?,
    val myProject: Project,
    val hole: HobbitHole?,
    val prompt: String,
    val editor: Editor?,
)

abstract class ShireLlmExecutor(open val context: ShireLlmExecutorContext) {
    abstract fun execute(postFunction: PostFunction)
}
