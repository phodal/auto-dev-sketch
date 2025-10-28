package cc.unitmesh.sketch.shadow

import cc.unitmesh.sketch.llms.LlmFactory
import cc.unitmesh.sketch.llms.cancelHandler
import cc.unitmesh.sketch.provider.devins.LanguageProcessor
import cc.unitmesh.sketch.sketch.SketchRunContext
import cc.unitmesh.sketch.template.GENIUS_CODE
import cc.unitmesh.sketch.template.TemplateRender
import cc.unitmesh.sketch.util.AutoDevCoroutineScope
import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch

object ShadowWorker {
    /**
     * Call llm to make prompt better
     */
    fun enhancePrompt(prompt: String): String {
        return prompt
    }

    suspend fun generatePlan(project: Project, input: String, handleCancel: ((String) -> Unit)?) {
        var llmProvider = LlmFactory.create(project)

        AutoDevCoroutineScope.workerScope(project).launch {
            val devInProcessor = LanguageProcessor.devin()
            val compiledInput = devInProcessor?.compile(project, input) ?: input

            val input = compiledInput.toString().trim()
            if (input.isEmpty()) {
                return@launch
            }

            val systemPrompt = collectSystemPrompt(project)
            val flow = llmProvider.stream(input, systemPrompt, keepHistory = true, true)
            val suggestion = StringBuilder()

            flow.cancelHandler { handleCancel }.cancellable().collect { char ->
                suggestion.append(char)
            }
        }
    }

    /**
     * call sketch and can also can plan model to directly generate code
     */
    suspend fun collectSystemPrompt(project: Project): String {
        val templateRender = TemplateRender(GENIUS_CODE)
        val template = templateRender.getTemplate("sketch.vm")
        val customContext = SketchRunContext.create(project, null, "")
        return templateRender.renderTemplate(template, customContext)
    }
}