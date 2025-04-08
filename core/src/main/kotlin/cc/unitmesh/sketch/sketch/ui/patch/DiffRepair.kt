package cc.unitmesh.sketch.sketch.ui.patch

import cc.unitmesh.sketch.llm2.model.ModelType
import cc.unitmesh.sketch.llms.LlmFactory
import cc.unitmesh.sketch.observer.agent.AgentStateService
import cc.unitmesh.sketch.template.GENIUS_CODE
import cc.unitmesh.sketch.template.TemplateRender
import cc.unitmesh.sketch.util.AutoDevCoroutineScope
import cc.unitmesh.sketch.util.parser.CodeFence
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch

object DiffRepair {
    private const val TEMPLATE_NAME = "repair-diff.vm"
    private const val systemPrompt = "You are professional programmer."

    fun applyDiffRepairSuggestion(
        project: Project,
        editor: Editor,
        oldCode: String,
        patchedCode: String,
        callback: ((newContent: String) -> Unit)? = null
    ) {
        val prompt = createDiffRepairPrompt(project, oldCode, patchedCode)
        val flow = LlmFactory.create(project, ModelType.FastApply).stream(prompt, systemPrompt, false)

        processStreamRealtime(project, flow) { code ->
            callback?.invoke(code)
            runWriteAction {
                editor.document.setText(code)
            }
        }
    }

    fun applyDiffRepairSuggestionSync(
        project: Project,
        oldCode: String,
        patchedCode: String,
        onComplete: (newContent: String) -> Unit
    ) {
        val prompt = createDiffRepairPrompt(project, oldCode, patchedCode)
        val flow = LlmFactory.create(project, ModelType.FastApply).stream(prompt, systemPrompt, false)
        processStreamBatch(project, flow) { code ->
            onComplete.invoke(code)
        }
    }

    private fun createDiffRepairPrompt(project: Project, oldCode: String, patchedCode: String): String {
        val templateRender = TemplateRender(GENIUS_CODE)
        val template = templateRender.getTemplate(TEMPLATE_NAME)
        val intention = project.getService(AgentStateService::class.java).buildOriginIntention()

        templateRender.context = DiffRepairContext(intention, patchedCode, oldCode)
        return templateRender.renderTemplate(template)
    }

    private fun processStreamRealtime(project: Project, flow: Flow<String>, onCodeChange: (String) -> Unit) {
        AutoDevCoroutineScope.Companion.scope(project).launch {
            val suggestion = StringBuilder()
            var lastProcessedCode = ""

            flow.cancellable().collect { char ->
                suggestion.append(char)
                val code = CodeFence.Companion.parse(suggestion.toString())
                if (code.text.isNotEmpty() && code.text != lastProcessedCode) {
                    lastProcessedCode = code.text
                    onCodeChange(code.text)
                }
            }
        }
    }

    private fun processStreamBatch(project: Project, flow: Flow<String>, onComplete: (String) -> Unit) {
        AutoDevCoroutineScope.Companion.scope(project).launch {
            val suggestion = StringBuilder()
            var lastProcessedCode = ""

            flow.cancellable().collect { char ->
                suggestion.append(char)
                val code = CodeFence.Companion.parse(suggestion.toString())
                if (code.text.isNotEmpty() && code.text != lastProcessedCode) {
                    lastProcessedCode = code.text
                }
            }

            if (lastProcessedCode.isNotEmpty()) {
                onComplete(lastProcessedCode)
            } else {
                val code = CodeFence.Companion.parse(suggestion.toString())
                if (code.text.isNotEmpty()) {
                    onComplete(code.text)
                }
            }
        }
    }
}
