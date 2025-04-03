package cc.unitmesh.sketch.provider.observer

import cc.unitmesh.sketch.gui.AutoDevToolWindowFactory
import cc.unitmesh.sketch.gui.chat.message.ChatActionType
import cc.unitmesh.sketch.settings.coder.coderSetting
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

interface AgentObserver {
    fun onRegister(project: Project)

    fun sendErrorNotification(project: Project, prompt: String) {
        if (prompt.isBlank()) return
        if (project.coderSetting.state.enableObserver == false) return

        runInEdt {
            // or sendToChatWindow ?
            AutoDevToolWindowFactory.sendToSketchToolWindow(project, ChatActionType.CHAT) { ui, _ ->
                ui.setInput(prompt)
            }
        }
    }

    companion object {
        private val EP_NAME: ExtensionPointName<AgentObserver> =
            ExtensionPointName("cc.unitmesh.agentObserver")

        fun register(project: Project) {
            EP_NAME.extensions.forEach { observer ->
                observer.onRegister(project)
            }
        }
    }
}