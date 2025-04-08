package cc.unitmesh.sketch.observer.plan

import cc.unitmesh.sketch.AutoDevBundle
import cc.unitmesh.sketch.gui.planner.AutoDevPlannerToolWindow
import cc.unitmesh.sketch.gui.AutoDevToolWindowFactory
import cc.unitmesh.sketch.gui.chat.message.ChatActionType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import cc.unitmesh.sketch.observer.agent.AgentStateService
import com.intellij.openapi.actionSystem.ActionUpdateThread

class EditPlanAction : AnAction(AutoDevBundle.message("sketch.plan.edit")) {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        val agentStateService = project.getService(AgentStateService::class.java)
        val currentPlan = agentStateService.getPlan()
        val planString = MarkdownPlanParser.formatPlanToMarkdown(currentPlan)

        AutoDevPlannerToolWindow.showPlanEditor(project, planString) { newPlan ->
            if (newPlan.isNotEmpty()) {
                /// todo: modify old messages
                AutoDevToolWindowFactory.sendToSketchToolWindow(project, ChatActionType.CHAT) { ui, _ ->
                    ui.setInput("Please follow the new plan to complete the task:\n# Plan\n```plan\n$newPlan\n```")
                }
            }
        }
    }
}
