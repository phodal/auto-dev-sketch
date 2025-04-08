package cc.unitmesh.ide.javascript.flow

import cc.unitmesh.sketch.bridge.archview.model.UiComponent
import cc.unitmesh.sketch.flow.TaskFlow
import cc.unitmesh.sketch.gui.chat.NormalChatCodingPanel
import cc.unitmesh.sketch.llms.LLMProvider
import cc.unitmesh.sketch.template.GENIUS_PAGE
import cc.unitmesh.sketch.template.TemplateRender
import cc.unitmesh.ide.javascript.flow.model.AutoPageContext
import kotlinx.coroutines.runBlocking

class AutoPageFlow(val context: AutoPageContext, val panel: NormalChatCodingPanel, val llm: LLMProvider) :
    TaskFlow<String> {
    override fun clarify(): String {
        val stepOnePrompt = generateStepOnePrompt(context)

        panel.addMessage(stepOnePrompt, true, stepOnePrompt)

        return runBlocking {
            val prompt = llm.stream(stepOnePrompt, "")
            return@runBlocking panel.updateMessage(prompt)
        }
    }

    private fun generateStepOnePrompt(context: AutoPageContext): String {
        val templateRender = TemplateRender(GENIUS_PAGE)
        val template = templateRender.getTemplate("page-gen-clarify.vm")

        templateRender.context = context

        val prompter = templateRender.renderTemplate(template)
        return prompter
    }


    override fun design(context: Any): List<String> {
        val componentList = context as List<UiComponent>
        val stepTwoPrompt = generateStepTwoPrompt(componentList)

        panel.addMessage(stepTwoPrompt, true, stepTwoPrompt)

        return runBlocking {
            val prompt = llm.stream(stepTwoPrompt, "")
            return@runBlocking panel.updateMessage(prompt)
        }.let { listOf(it) }
    }

    private fun generateStepTwoPrompt(selectedComponents: List<UiComponent>): String {
        val templateRender = TemplateRender(GENIUS_PAGE)
        val template = templateRender.getTemplate("page-gen-design.vm")

        context.pages = selectedComponents.map { it.format() }
        templateRender.context = context

        val prompter = templateRender.renderTemplate(template)
        return prompter
    }
}