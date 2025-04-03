package cc.unitmesh.sketch.gui.toolbar

import cc.unitmesh.sketch.gui.AutoDevToolWindowFactory
import cc.unitmesh.sketch.gui.AutoDevToolWindowFactory.AutoDevToolUtil
import cc.unitmesh.sketch.settings.locale.LanguageChangedCallback.componentStateChanged
import cc.unitmesh.sketch.sketch.SketchToolWindow
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.components.panels.Wrapper
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.JBUI
import javax.swing.JButton
import javax.swing.JComponent

class NewSketchAction : AnAction("New Sketch", "Create new Sketch", AllIcons.General.Add), CustomComponentAction {
    private val logger = logger<NewChatAction>()

    override fun update(e: AnActionEvent) {
        e.presentation.text = "New Sketch"
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
    override fun actionPerformed(e: AnActionEvent) {
        newSketch(e.dataContext)
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        val button: JButton = object : JButton() {
            init {
                putClientProperty("ActionToolbar.smallVariant", true)
                putClientProperty("customButtonInsets", JBInsets(1, 1, 1, 1).asUIResource())

                setOpaque(false)
                addActionListener {
                    val dataContext: DataContext = ActionToolbar.getDataContextFor(this)
                    newSketch(dataContext)
                }
            }
        }.apply {
            componentStateChanged("chat.panel.newSketch", this) { b, d -> b.text = d }
        }

        return Wrapper(button).also {
            it.setBorder(JBUI.Borders.empty(0, 10))
        }
    }

    private fun newSketch(dataContext: DataContext) {
        val project = dataContext.getData(CommonDataKeys.PROJECT)
        if (project == null) {
            logger.error("project is null")
            return
        }

        val toolWindowManager = ToolWindowManager.getInstance(project).getToolWindow(AutoDevToolUtil.ID)
        val contentManager = toolWindowManager?.contentManager

        val sketchPanel =
            contentManager?.component?.components?.filterIsInstance<SketchToolWindow>()?.firstOrNull()

        if (sketchPanel == null) {
            AutoDevToolWindowFactory.createSketchToolWindow(project, toolWindowManager!!)
        }

        sketchPanel?.resetSketchSession()
    }
}
