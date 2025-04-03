package cc.unitmesh.sketch.observer.plan

import cc.unitmesh.sketch.AutoDevBundle
import cc.unitmesh.sketch.AutoDevIcons
import cc.unitmesh.sketch.AutoDevNotifications
import cc.unitmesh.sketch.llms.LlmFactory
import cc.unitmesh.sketch.observer.agent.AgentStateService
import cc.unitmesh.sketch.statusbar.AutoDevStatus
import cc.unitmesh.sketch.statusbar.AutoDevStatusService
import cc.unitmesh.sketch.template.GENIUS_CODE
import cc.unitmesh.sketch.template.TemplateRender
import cc.unitmesh.sketch.util.AutoDevCoroutineScope
import cc.unitmesh.sketch.util.parser.CodeFence
import cc.unitmesh.sketch.util.parser.MarkdownCodeHelper
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PlanReviewAction : AnAction(AutoDevBundle.message("sketch.plan.review"), null, AutoDevIcons.REVIEWER) {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val project = anActionEvent.project ?: return
        reviewPlan(project)
    }
}

fun reviewPlan(project: Project, isBlockingMode: Boolean = false): String {
    val agentStateService = project.getService(AgentStateService::class.java)

    val currentPlan = agentStateService.getPlan()
    val plan = MarkdownPlanParser.formatPlanToMarkdown(currentPlan)

    val allMessages = agentStateService.getAllMessages()
    val withoutCodeMsgs = allMessages.map {
        it.copy(role = it.role, content = MarkdownCodeHelper.removeAllMarkdownCode(it.content))
    }

    if (plan.isEmpty()) {
        AutoDevNotifications.warn(project, AutoDevBundle.message("sketch.plan.empty"))
        return ""
    }

    val templateRender = TemplateRender(GENIUS_CODE)
    val systemPrompt = templateRender.getTemplate("plan-reviewer.vm")
    val history = withoutCodeMsgs.joinToString {
        "# Role ${it.role}\nMessage:\n${it.content}"
    } + "\nLastPlan: \n$plan\n"

    val stream = LlmFactory.create(project).stream(history, systemPrompt)
    val llmResult = StringBuilder()
    var planText = ""

    if (isBlockingMode) {
        runBlocking {
            stream.collect {
                llmResult.append(it)
            }
        }

        val result = llmResult.toString()
        val plan = CodeFence.parseAll(result).firstOrNull {
            it.originLanguage == "plan"
        }

        return plan?.text ?: result
    }

    AutoDevCoroutineScope.scope(project).launch {
        AutoDevNotifications.notify(project, AutoDevBundle.message("sketch.plan.reviewing"))
        AutoDevStatusService.notifyApplication(AutoDevStatus.InProgress, "review the plan")

        runBlocking {
            stream.collect {
                llmResult.append(it)
            }
        }

        val result = llmResult.toString()
        AutoDevNotifications.notify(project, result)
        AutoDevStatusService.notifyApplication(AutoDevStatus.Done, "review the plan")

        val plan = CodeFence.parseAll(result).firstOrNull {
            it.originLanguage == "plan"
        }

        if (plan !== null) {
            planText = plan.text
            agentStateService.updatePlan(plan.text)
        } else {
            planText = result
        }
    }

    return planText
}
