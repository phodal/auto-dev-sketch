package cc.unitmesh.devti.sketch.ui.plan

import cc.unitmesh.devti.AutoDevBundle
import cc.unitmesh.devti.gui.AutoDevPlanerToolWindowFactory
import cc.unitmesh.devti.gui.AutoDevToolWindowFactory
import cc.unitmesh.devti.gui.chat.message.ChatActionType
import cc.unitmesh.devti.observer.agent.AgentStateService
import cc.unitmesh.devti.observer.plan.AgentPlanStep
import cc.unitmesh.devti.observer.plan.AgentTaskEntry
import cc.unitmesh.devti.observer.plan.MarkdownPlanParser
import cc.unitmesh.devti.observer.plan.TaskStatus
import cc.unitmesh.devti.sketch.ui.ExtensionLangSketch
import com.intellij.icons.AllIcons
import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.panels.Wrapper
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu

class PlanSketch(
    private val project: Project,
    private var content: String,
    private var agentTaskItems: MutableList<AgentTaskEntry>,
    private val isInToolwindow: Boolean = false
) : JBPanel<PlanSketch>(BorderLayout(JBUI.scale(8), 0)), ExtensionLangSketch {
    private val contentPanel = JPanel(BorderLayout()).apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = JBEmptyBorder(JBUI.insets(4))
    }

    private val actionGroup = DefaultActionGroup(createToolbar(project))
    private fun createToolbar(project: Project): List<AnAction> {
        val popupAction = object : AnAction("Pin", "Show in popup window", AllIcons.Toolbar.Pin) {
            override fun displayTextInToolbar(): Boolean = true

            override fun actionPerformed(e: AnActionEvent) {
                val toolWindow =
                    ToolWindowManager.Companion.getInstance(project).getToolWindow(AutoDevPlanerToolWindowFactory.Companion.PlANNER_ID)
                        ?: return

                toolWindow.activate {
                    // todo
                }
            }
        }

        return listOf(popupAction)
    }

    private val toolbar = ActionManager.getInstance()
        .createActionToolbar("PlanSketch", actionGroup, true).apply {
            targetComponent = contentPanel
        }

    private val titleLabel = JLabel("Thought Plan").apply {
        border = JBUI.Borders.empty(0, 10)
    }

    private val toolbarPanel = JPanel(BorderLayout()).apply {
        if (!isInToolwindow) {
            add(titleLabel, BorderLayout.WEST)
            add(toolbar.component, BorderLayout.EAST)
        }
    }

    private val toolbarWrapper = Wrapper(JBUI.Panels.simplePanel(toolbarPanel)).also {
        it.border = JBUI.Borders.customLine(UIUtil.getBoundsColor(), 1, 1, 1, 1)
    }

    init {
        createPlanUI()

        if (isInToolwindow) {
            add(toolbarWrapper, BorderLayout.NORTH)
            border = JBUI.Borders.empty(8)
        }

        add(contentPanel, BorderLayout.CENTER)
    }

    private fun createPlanUI() {
        agentTaskItems.forEachIndexed { index, planItem ->
            val titlePanel = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.LEFT, 2, 0)).apply {
                border = JBUI.Borders.empty(2)
            }

            // Check if all tasks in the section are completed
            updateSectionCompletionStatus(planItem)

            // Create a formatted title with the appropriate status marker
            val statusIndicator = when (planItem.status) {
                TaskStatus.COMPLETED -> "✓"
                TaskStatus.FAILED -> "!"
                TaskStatus.IN_PROGRESS -> "*"
                TaskStatus.TODO -> ""
            }

            val titleText = if (statusIndicator.isNotEmpty()) {
                "<html><b>${index + 1}. ${planItem.title} [$statusIndicator]</b></html>"
            } else {
                "<html><b>${index + 1}. ${planItem.title}</b></html>"
            }

            val sectionLabel = JLabel(titleText)
            sectionLabel.border = JBUI.Borders.emptyLeft(2)

            if (planItem.status == TaskStatus.TODO && planItem.completed == false) {
                val executeTaskButton = JButton(AllIcons.Actions.Execute).apply {
                    border = BorderFactory.createEmptyBorder()
                    isOpaque = true
                    preferredSize = Dimension(20, 20)
                    toolTipText = "Execute Task"

                    addActionListener {
                        AutoDevToolWindowFactory.Companion.sendToSketchToolWindow(project, ChatActionType.SKETCH) { ui, _ ->
                            val allSteps = planItem.steps.joinToString("\n") { it.step }
                            ui.sendInput(AutoDevBundle.message("sketch.plan.finish.task") + allSteps)
                        }
                    }
                }
                titlePanel.add(executeTaskButton)
            }

            titlePanel.add(sectionLabel)
            contentPanel.add(titlePanel)

            planItem.steps.forEachIndexed { taskIndex, task ->
                val taskPanel = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.LEFT, 2, 0)).apply {
                    border = JBUI.Borders.empty(1, 16, 1, 0)
                }

                val taskLabel = createStyledTaskLabel(task)

                val statusIcon = when (task.status) {
                    TaskStatus.COMPLETED -> JLabel(AllIcons.Actions.Checked)
                    TaskStatus.FAILED -> JLabel(AllIcons.General.Error)
                    TaskStatus.IN_PROGRESS -> JLabel(AllIcons.Toolwindows.ToolWindowBuild)
                    TaskStatus.TODO -> JBCheckBox().apply {
                        isSelected = task.completed
                        addActionListener {
                            task.completed = isSelected
                            if (isSelected) {
                                task.updateStatus(TaskStatus.COMPLETED)
                            } else {
                                task.updateStatus(TaskStatus.TODO)
                            }

                            // Update section status when task status changes
                            val currentSection = agentTaskItems.find { it.steps.contains(task) }
                            currentSection?.let { updateSectionCompletionStatus(it) }

                            updateTaskLabel(taskLabel, task)
                            contentPanel.revalidate()
                            contentPanel.repaint()
                        }
                        isBorderPainted = false
                        isContentAreaFilled = false
                    }
                }

                taskPanel.add(statusIcon)

                // Add execute button for incomplete tasks
                if (task.status == TaskStatus.TODO) {
                    val executeButton = JButton(AllIcons.Actions.Execute).apply {
                        border = BorderFactory.createEmptyBorder()
                        isOpaque = true
                        preferredSize = Dimension(20, 20)
                        toolTipText = "Execute"

                        addActionListener {
                            AutoDevToolWindowFactory.Companion.sendToSketchToolWindow(project, ChatActionType.SKETCH) { ui, _ ->
                                ui.sendInput(AutoDevBundle.message("sketch.plan.finish.task") + task.step)
                            }
                        }
                    }

                    taskPanel.add(executeButton)
                }

                taskPanel.add(taskLabel)

                // Add context menu for changing task status
                val taskPopupMenu = JPopupMenu()
                val markCompletedItem = JMenuItem("Mark as Completed [✓]")
                val markInProgressItem = JMenuItem("Mark as In Progress [*]")
                val markFailedItem = JMenuItem("Mark as Failed [!]")
                val markTodoItem = JMenuItem("Mark as Todo [ ]")

                markCompletedItem.addActionListener {
                    task.updateStatus(TaskStatus.COMPLETED)
                    updateTaskLabel(taskLabel, task)

                    // Update section status after changing task status
                    val currentSection = agentTaskItems.find { it.steps.contains(task) }
                    currentSection?.let { updateSectionCompletionStatus(it) }

                    contentPanel.revalidate()
                    contentPanel.repaint()
                }

                markInProgressItem.addActionListener {
                    task.updateStatus(TaskStatus.IN_PROGRESS)
                    updateTaskLabel(taskLabel, task)

                    val currentSection = agentTaskItems.find { it.steps.contains(task) }
                    currentSection?.let { updateSectionCompletionStatus(it) }

                    contentPanel.revalidate()
                    contentPanel.repaint()
                }

                markFailedItem.addActionListener {
                    task.updateStatus(TaskStatus.FAILED)
                    updateTaskLabel(taskLabel, task)

                    val currentSection = agentTaskItems.find { it.steps.contains(task) }
                    currentSection?.let { updateSectionCompletionStatus(it) }

                    contentPanel.revalidate()
                    contentPanel.repaint()
                }

                markTodoItem.addActionListener {
                    task.updateStatus(TaskStatus.TODO)
                    updateTaskLabel(taskLabel, task)

                    val currentSection = agentTaskItems.find { it.steps.contains(task) }
                    currentSection?.let { updateSectionCompletionStatus(it) }

                    contentPanel.revalidate()
                    contentPanel.repaint()
                }

                taskPopupMenu.add(markCompletedItem)
                taskPopupMenu.add(markInProgressItem)
                taskPopupMenu.add(markFailedItem)
                taskPopupMenu.add(markTodoItem)

                taskLabel.componentPopupMenu = taskPopupMenu

                contentPanel.add(taskPanel)
            }

            if (index < agentTaskItems.size - 1) {
                contentPanel.add(Box.createVerticalStrut(8))
            }
        }
    }

    // Helper method to create a styled task label based on status
    private fun createStyledTaskLabel(task: AgentPlanStep): JLabel {
        val labelText = when (task.status) {
            TaskStatus.COMPLETED -> "<html><strike>${task.step}</strike></html>"
            TaskStatus.FAILED -> "<html><span style='color:red'>${task.step}</span></html>"
            TaskStatus.IN_PROGRESS -> "<html><span style='color:blue;font-style:italic'>${task.step}</span></html>"
            TaskStatus.TODO -> task.step
        }

        return JLabel(labelText).apply {
            border = JBUI.Borders.emptyLeft(5)
        }
    }

    // Helper method to update the task label based on current status
    private fun updateTaskLabel(label: JLabel, task: AgentPlanStep) {
        label.text = when (task.status) {
            TaskStatus.COMPLETED -> "<html><strike>${task.step}</strike></html>"
            TaskStatus.FAILED -> "<html><span style='color:red'>${task.step}</span></html>"
            TaskStatus.IN_PROGRESS -> "<html><span style='color:blue;font-style:italic'>${task.step}</span></html>"
            TaskStatus.TODO -> task.step
        }
    }

    private fun updateSectionCompletionStatus(planItem: AgentTaskEntry) {
        planItem.updateCompletionStatus()

        contentPanel.revalidate()
        contentPanel.repaint()
    }

    override fun getExtensionName(): String = "ThoughtPlan"

    override fun getViewText(): String = content

    override fun updateViewText(text: String, complete: Boolean) {
        this.content = text
        val agentPlans = MarkdownPlanParser.parse(text)
        updatePlan(agentPlans)
    }

    override fun onComplete(context: String) {
        if (!isInToolwindow) {
            val agentPlans = MarkdownPlanParser.parse(content).toMutableList()
            updatePlan(agentPlans)
            project.getService(AgentStateService::class.java).updatePlan(agentPlans)
        }
    }

    fun updatePlan(newPlanItems: List<AgentTaskEntry>) {
        if (newPlanItems.isNotEmpty()) {
            // Save current states of all tasks
            val taskStateMap = mutableMapOf<String, Pair<Boolean, TaskStatus>>()

            agentTaskItems.forEach { planItem ->
                planItem.steps.forEach { task ->
                    taskStateMap[task.step] = Pair(task.completed, task.status)
                }
            }

            contentPanel.removeAll()
            agentTaskItems.clear()

            newPlanItems.forEach { newItem ->
                agentTaskItems.add(newItem)

                newItem.steps.forEach { task ->
                    // Restore saved states if available
                    taskStateMap[task.step]?.let { (completed, status) ->
                        task.completed = completed
                        task.status = status
                    }
                }
            }

            createPlanUI()
        }

        contentPanel.revalidate()
        contentPanel.repaint()
    }

    override fun getComponent(): JComponent = this

    override fun updateLanguage(language: Language?, originLanguage: String?) {}

    override fun dispose() {}
}