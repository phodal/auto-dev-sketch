package cc.unitmesh.sketch.prompting

import cc.unitmesh.sketch.custom.compile.VariableTemplateCompiler
import cc.unitmesh.sketch.gui.chat.message.ChatActionType
import cc.unitmesh.sketch.provider.context.ChatContextItem
import cc.unitmesh.sketch.provider.context.ChatContextProvider
import cc.unitmesh.sketch.provider.context.ChatCreationContext
import cc.unitmesh.sketch.provider.context.ChatOrigin
import cc.unitmesh.sketch.provider.devins.LanguageProcessor
import cc.unitmesh.sketch.template.TemplateRender
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import cc.unitmesh.sketch.intentions.action.getElementToAction
import kotlinx.coroutines.runBlocking
import com.intellij.openapi.application.runReadAction

abstract class SimpleDevinPrompter {
    abstract val templateRender: TemplateRender
    abstract val template: String

    suspend fun prompting(project: Project, userInput: String, editor: Editor?): String {
        val variableCompile = runReadAction {
            VariableTemplateCompiler.create(project, editor)
        }
        if (variableCompile == null) {
            val frameworkContext = collectFrameworkContext(editor, project)
            templateRender.addVariable("input", userInput)
            templateRender.addVariable("frameworkContext", frameworkContext)
            return templateRender.renderTemplate(template)
        }

        val postProcessors = LanguageProcessor.devin()
        val compiledTemplate = postProcessors?.compile(project, template) ?: template

        variableCompile.set("input", userInput)
        return variableCompile.compile(compiledTemplate)
    }

    fun collectFrameworkContext(myEditor: Editor?, project: Project): String {
        val editor = myEditor ?: FileEditorManager.getInstance(project).selectedTextEditor ?: return ""
        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)

        val element = getElementToAction(project, editor)
        val creationContext =
            ChatCreationContext(ChatOrigin.Intention, ChatActionType.SKETCH, psiFile, listOf(), element = element)
        val contextItems: List<ChatContextItem> = runBlocking {
            return@runBlocking ChatContextProvider.collectChatContextList(project, creationContext)
        }

        return contextItems.joinToString("\n") { it.text }
    }
}