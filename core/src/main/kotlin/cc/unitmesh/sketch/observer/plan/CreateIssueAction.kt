package cc.unitmesh.sketch.observer.plan

import cc.unitmesh.sketch.AutoDevBundle
import cc.unitmesh.sketch.gui.planner.AutoDevPlannerToolWindow
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class CreateIssueAction : AnAction(AutoDevBundle.message("sketch.plan.create")) {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        AutoDevPlannerToolWindow.showIssueInput(project)
    }
}
