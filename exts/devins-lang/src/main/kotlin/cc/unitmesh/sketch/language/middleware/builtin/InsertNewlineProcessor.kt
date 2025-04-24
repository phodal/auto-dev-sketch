package cc.unitmesh.sketch.language.middleware.builtin

import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import cc.unitmesh.sketch.devins.post.PostProcessorType
import cc.unitmesh.sketch.devins.post.PostProcessorContext
import cc.unitmesh.sketch.devins.post.PostProcessor
import cc.unitmesh.sketch.util.workerThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class InsertNewlineProcessor : PostProcessor {
    override val processorName: String = PostProcessorType.InsertNewline.handleName
    override val description: String = "`insertNewline` will insert a newline at the cursor position"

    override fun isApplicable(context: PostProcessorContext): Boolean = true

    override fun execute(project: Project, context: PostProcessorContext, console: ConsoleView?, args: List<Any>): Any {
        val editor = context.editor ?: return ""

        CoroutineScope(workerThread).launch {
            WriteCommandAction.runWriteCommandAction(project) {
                editor.document.insertString(editor.caretModel.offset, "\n")
            }
        }

        return editor.document.text
    }
}
