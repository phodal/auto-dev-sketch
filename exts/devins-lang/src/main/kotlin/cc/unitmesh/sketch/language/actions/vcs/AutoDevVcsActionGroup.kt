package cc.unitmesh.sketch.language.actions.vcs

import cc.unitmesh.sketch.AutoDevIcons
import cc.unitmesh.sketch.language.actions.DevInsRunFileAction
import cc.unitmesh.sketch.devins.ShireActionLocation
import cc.unitmesh.sketch.devins.VariableActionEventDataHolder
import cc.unitmesh.sketch.language.startup.DynamicDevInsActionConfig
import cc.unitmesh.sketch.language.startup.DynamicShireActionService
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project

class AutoDevVcsActionGroup : ActionGroup() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val isMultipleActions = shireActionConfigs(project).size > 1
        e.presentation.isVisible = isMultipleActions
        e.presentation.isEnabled = shireActionConfigs(project).any { it.hole?.enabled == true }
        e.presentation.isPopupGroup = true
    }

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val project = e?.project ?: return emptyArray()
        return shireActionConfigs(project).map(::ShireVcsAction).toTypedArray()
    }

    private fun shireActionConfigs(project: Project) =
        DynamicShireActionService.getInstance(project).getActions(ShireActionLocation.COMMIT_MENU)
}

class ShireVcsAction(val config: DynamicDevInsActionConfig) :
    DumbAwareAction(config.name, config.hole?.description, AutoDevIcons.AI_COPILOT) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        VariableActionEventDataHolder.putData(VariableActionEventDataHolder(e.dataContext))
        DevInsRunFileAction.executeFile(project, config, null)
    }
}
