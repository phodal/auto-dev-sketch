package cc.unitmesh.sketch.bridge

import cc.unitmesh.sketch.gui.chat.message.ChatActionType
import cc.unitmesh.sketch.sketch.SketchInputListener
import cc.unitmesh.sketch.sketch.SketchToolWindow
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

class BridgeToolWindow(val myProject: Project, val myEditor: Editor?, private val showInput: Boolean = false) :
    SketchToolWindow(myProject, myEditor, showInput, ChatActionType.BRIDGE) {
    override val inputListener = object : SketchInputListener(project, chatCodingService, this) {
        override val template = templateRender.getTemplate("bridge.vm")
        override var systemPrompt = ""

        override fun collectSystemPrompt(): String = systemPrompt

        override suspend fun setup() {
            val customContext = BridgeRunContext.create(project, null, "")
            systemPrompt = templateRender.renderTemplate(template, customContext)
            toolWindow.addSystemPrompt(systemPrompt)
        }
    }
}