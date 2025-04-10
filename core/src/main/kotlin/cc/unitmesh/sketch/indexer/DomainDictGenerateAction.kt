package cc.unitmesh.sketch.indexer

import cc.unitmesh.sketch.indexer.provider.LangDictProvider
import cc.unitmesh.sketch.llms.LlmFactory
import cc.unitmesh.sketch.settings.coder.coderSetting
import cc.unitmesh.sketch.settings.locale.LanguageChangedCallback.presentationText
import cc.unitmesh.sketch.statusbar.AutoDevStatus
import cc.unitmesh.sketch.statusbar.AutoDevStatusService
import cc.unitmesh.sketch.template.GENIUS_CODE
import cc.unitmesh.sketch.template.TemplateRender
import cc.unitmesh.sketch.template.context.TemplateContext
import cc.unitmesh.sketch.util.AutoDevCoroutineScope
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.guessProjectDir
import kotlinx.coroutines.launch
import cc.unitmesh.sketch.AutoDevIcons
import cc.unitmesh.sketch.indexer.usage.PromptEnhancer
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.vfs.LocalFileSystem
import javax.swing.Icon

class DomainDictGenerateAction : AnAction() {
    init {
        presentationText("indexer.generate.domain", templatePresentation)
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val presentation = event.presentation
        
        AutoDevCoroutineScope.scope(project).launch {
            val baseDir = project.coderSetting.state.teamPromptsDir
            val prompt = buildPrompt(project)

            try {
                updatePresentation(presentation, AutoDevIcons.InProgress, false)
                AutoDevStatusService.notifyApplication(AutoDevStatus.InProgress)
                val promptDir = project.guessProjectDir()!!.toNioPath().resolve(baseDir)
                if (!promptDir.exists()) {
                    promptDir.createDirectories()
                }

                logger<DomainDictGenerateAction>().debug("Prompt: $prompt")

                val file = promptDir.resolve("domain.csv").toFile()
                if (!file.exists()) {
                    file.createNewFile()
                }

                val fileEditorManager = FileEditorManager.getInstance(project)
                var editors: Array<FileEditor> = emptyArray()
                ApplicationManager.getApplication().invokeAndWait {
                    val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
                    if (virtualFile != null) {
                        editors = fileEditorManager.openFile(virtualFile, true)
                        fileEditorManager.setSelectedEditor(virtualFile, "text-editor")
                    }
                }

                val editor = fileEditorManager.selectedTextEditor
                val stream: Flow<String> = LlmFactory.create(project).stream(prompt, "")
                val result = StringBuilder()

                stream.cancellable().collect { chunk ->
                    result.append(chunk)
                    WriteCommandAction.writeCommandAction(project).compute<Any, RuntimeException> {
                        editor?.document?.setText(result.toString())
                        editor?.caretModel?.moveToOffset(editor?.document?.textLength ?: 0)
                        editor?.scrollingModel?.scrollToCaret(ScrollType.RELATIVE)
                    }
                }

                AutoDevStatusService.notifyApplication(AutoDevStatus.Done)
            } catch (e: Exception) {
                AutoDevStatusService.notifyApplication(AutoDevStatus.Error)
                e.printStackTrace()
            } finally {
                // Restore icon and enable the action
                updatePresentation(presentation, AutoDevIcons.AI_COPILOT, true)
            }
        }
    }

    private suspend fun buildPrompt(project: Project): String {
        val names = LangDictProvider.all(project)
        val templateRender = TemplateRender(GENIUS_CODE)
        val template = templateRender.getTemplate("indexer.vm")
        val readmeMe = PromptEnhancer.readmeFile(project)

        val context = DomainDictGenerateContext(names.joinToString(", "), readmeMe)
        val prompt = templateRender.renderTemplate(template, context)
        return prompt
    }

    private fun updatePresentation(presentation: Presentation, icon: Icon, enabled: Boolean) {
        presentation.icon = icon
        presentation.isEnabled = enabled
    }
}

data class DomainDictGenerateContext(
    val code: String,
    val readme: String
) : TemplateContext
