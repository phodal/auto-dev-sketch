package cc.unitmesh.sketch.actions

import cc.unitmesh.sketch.settings.AutoDevSettingsConfigurable
import cc.unitmesh.sketch.settings.locale.LanguageChangedCallback.presentationText
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil

class EditSettingsAction : AnAction() {
    init{
        presentationText("settings.autodev.others.editSettings", templatePresentation)
    }
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        ShowSettingsUtil.getInstance().showSettingsDialog(project, AutoDevSettingsConfigurable::class.java)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
