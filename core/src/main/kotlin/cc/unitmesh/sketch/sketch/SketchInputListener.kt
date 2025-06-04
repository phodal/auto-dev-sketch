package cc.unitmesh.sketch.sketch

import cc.unitmesh.sketch.AutoDevBundle
import cc.unitmesh.sketch.gui.chat.ChatCodingService
import cc.unitmesh.sketch.gui.chat.ui.AutoDevInputListener
import cc.unitmesh.sketch.gui.chat.ui.AutoDevInputSection
import cc.unitmesh.sketch.gui.chat.ui.AutoDevInputTrigger
import cc.unitmesh.sketch.llm2.model.LlmConfig
import cc.unitmesh.sketch.llms.cancelHandler
import cc.unitmesh.sketch.mcp.ui.SketchConfigListener
import cc.unitmesh.sketch.observer.agent.AgentStateService
import cc.unitmesh.sketch.prompting.SimpleDevinPrompter
import cc.unitmesh.sketch.provider.devins.LanguageProcessor
import cc.unitmesh.sketch.template.GENIUS_CODE
import cc.unitmesh.sketch.template.TemplateRender
import cc.unitmesh.sketch.util.AutoDevCoroutineScope
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBusConnection
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import io.modelcontextprotocol.kotlin.sdk.Tool

open class SketchInputListener(
    private val project: Project,
    private val chatCodingService: ChatCodingService,
    open val toolWindow: SketchToolWindow
) : AutoDevInputListener, SimpleDevinPrompter(), Disposable {
    private val connection = ApplicationManager.getApplication().messageBus.connect(this)

    override val template = templateRender.getTemplate("sketch.vm")
    override val templateRender: TemplateRender get() = TemplateRender(GENIUS_CODE)
    open var systemPrompt = ""
    open var planPrompt = ""
    val planTemplate = templateRender.getTemplate("plan.vm")
    private var messageBusConnection: MessageBusConnection? = null

    open suspend fun setup() {
        val customContext = SketchRunContext.create(project, null, "")
        systemPrompt = templateRender.renderTemplate(template, customContext)
        planPrompt = templateRender.renderTemplate(planTemplate, customContext)
        toolWindow.addSystemPrompt(systemPrompt)

        // Subscribe to tool configuration changes
        setupToolConfigListener()
    }

    private fun setupToolConfigListener() {
        messageBusConnection = ApplicationManager.getApplication().messageBus.connect()
        messageBusConnection?.subscribe(SketchConfigListener.TOPIC, object : SketchConfigListener {
            override fun onSelectedToolsChanged(tools: Map<String, Set<Tool>>) {
                AutoDevCoroutineScope.scope(project).launch {
                    updateSystemPrompt()
                }
            }
        })
    }

    private suspend fun updateSystemPrompt() {
        val customContext = SketchRunContext.create(project, null, "")
        val newSystemPrompt = templateRender.renderTemplate(template, customContext)
        val newPlanPrompt = templateRender.renderTemplate(planTemplate, customContext)

        systemPrompt = newSystemPrompt
        planPrompt = newPlanPrompt

        toolWindow.updateSystemPrompt(newSystemPrompt)
    }

    override fun onStop(component: AutoDevInputSection) {
        chatCodingService.stop()
        toolWindow.hiddenProgressBar()
        toolWindow.stop()
    }

    override fun onSubmit(component: AutoDevInputSection, trigger: AutoDevInputTrigger) {
        val userInput = component.renderText()
        component.clearText()

        if (userInput.isEmpty() || userInput.isBlank()) {
            component.showTooltip(AutoDevBundle.message("chat.input.tips"))
            return
        }

        ApplicationManager.getApplication().invokeLater {
            manualSend(userInput)
        }
    }

    open fun collectSystemPrompt(): String {
        return when {
            chatCodingService.getAllMessages().size == 3 && LlmConfig.hasPlanModel() -> {
                val intention = project.getService(AgentStateService::class.java).buildOriginIntention() ?: ""
                planPrompt.replace(
                    "<user.question>user.question</user.question>",
                    "<user.question>$intention</user.question>"
                )
            }

            else -> {
                systemPrompt
            }
        }
    }

    override fun manualSend(userInput: String) {
        val input = userInput.trim()
        if (input.isEmpty() || input.isBlank()) return
        if (input == "\n") return
        if (input.length < 10) {
            logger<SketchInputListener>().debug("Input.length < 10: $input")
        }

        logger<SketchInputListener>().debug("Start compiling: $input")
        AutoDevCoroutineScope.workerScope(project).launch {
            val devInProcessor = LanguageProcessor.devin()
            val compiledInput = devInProcessor?.compile(project, input) ?: input

            val input = compiledInput.toString().trim()
            if (input.isEmpty()) {
                return@launch
            }

            toolWindow.beforeRun()
            toolWindow.updateHistoryPanel()
            toolWindow.addRequestPrompt(input)

            val flow = chatCodingService.sketchRequest(collectSystemPrompt(), input, isFromSketch = true)
            val suggestion = StringBuilder()

            AutoDevCoroutineScope.workerScope(project).launch {
                flow.cancelHandler { toolWindow.handleCancel = it }.cancellable().collect { char ->
                    suggestion.append(char)
                    toolWindow.onUpdate(suggestion.toString())
                }

                toolWindow.onFinish(suggestion.toString())
            }
        }
    }

    override fun dispose() {
        connection.disconnect()
        messageBusConnection?.disconnect()
        messageBusConnection = null
    }

    fun stop() {

    }
}