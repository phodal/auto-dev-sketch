package cc.unitmesh.sketch.intentions.action.task

import cc.unitmesh.sketch.AutoDevBundle
import cc.unitmesh.sketch.AutoDevNotifications
import cc.unitmesh.sketch.util.InsertUtil
import cc.unitmesh.sketch.util.AutoDevCoroutineScope
import cc.unitmesh.sketch.intentions.action.CodeCompletionBaseIntention
import cc.unitmesh.sketch.llms.LlmFactory
import cc.unitmesh.sketch.settings.AutoDevSettingsState
import cc.unitmesh.sketch.statusbar.AutoDevStatus
import cc.unitmesh.sketch.statusbar.AutoDevStatusService
import cc.unitmesh.sketch.util.parser.CodeFence
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAwareAction
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

abstract class BaseCompletionTask(private val request: CodeCompletionRequest) :
    Task.Backgroundable(request.project, AutoDevBundle.message("intentions.chat.code.complete.name")) {

    private var isCanceled: Boolean = false
    abstract fun promptText(): String

    open fun keepHistory(): Boolean = true

    override fun run(indicator: ProgressIndicator) {
        indicator.isIndeterminate = false
        indicator.fraction = 0.1
        indicator.text = AutoDevBundle.message("intentions.chat.code.test.step.prepare-context")

        AutoDevStatusService.notifyApplication(AutoDevStatus.InProgress)

        val prompt = promptText()

        val keepHistory = keepHistory() && prompt.length < AutoDevSettingsState.maxTokenLength
        val flow: Flow<String> = LlmFactory.create(request.project).stream(prompt, "", keepHistory)
        logger.info("Prompt: $prompt")

        DumbAwareAction.create {
            isCanceled = true
        }.registerCustomShortcutSet(
            CustomShortcutSet(
                KeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), null),
            ),
            request.editor.component
        )

        val editor = request.editor
        val project = request.project
        var currentOffset = request.offset

        indicator.isIndeterminate = false
        indicator.fraction = 0.5
        indicator.text = AutoDevBundle.message("intentions.request.background.process.title")

        AutoDevCoroutineScope.scope(request.project).launch {
            val suggestion = StringBuilder()

            flow.cancellable().collect { char ->
                if (isCanceled) {
                    cancel()
                    return@collect
                }

                suggestion.append(char)
                invokeLater {
                    if (!isCanceled && !request.isReplacement) {
                        InsertUtil.insertStreamingToDoc(project, char, editor, currentOffset)
                        currentOffset += char.length
                    }
                }
            }

            if (request.isReplacement) {
                val parsedContent = CodeFence.parse(suggestion.toString()).text
                InsertUtil.replaceText(project, editor, request.element, parsedContent)
            }

            indicator.fraction = 0.8
            AutoDevStatusService.notifyApplication(AutoDevStatus.Done)
            logger.info("Suggestion: $suggestion")
        }
    }

    override fun onThrowable(error: Throwable) {
        super.onThrowable(error)
        AutoDevNotifications.error(project, "Failed to completion: ${error.message}")
        AutoDevStatusService.notifyApplication(AutoDevStatus.Error)
    }

    override fun onCancel() {
        this.isCanceled = true
        super.onCancel()
    }

    companion object {
        private val logger = logger<CodeCompletionBaseIntention>()
    }
}
