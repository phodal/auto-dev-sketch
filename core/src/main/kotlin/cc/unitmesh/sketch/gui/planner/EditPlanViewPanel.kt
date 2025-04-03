package cc.unitmesh.sketch.gui.planner

import cc.unitmesh.sketch.AutoDevSnippetFile
import cc.unitmesh.sketch.sketch.ui.code.EditorUtil
import cc.unitmesh.sketch.sketch.ui.code.findDocument
import cc.unitmesh.sketch.util.parser.CodeFence
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.Box
import javax.swing.JButton
import javax.swing.JPanel

class EditPlanViewPanel(
    private val project: Project,
    private val content: String,
    private val onSave: (String) -> Unit,
    private val onCancel: () -> Unit
) : JPanel(BorderLayout()), Disposable {
    private var markdownEditor: EditorEx

    init {
        val language = CodeFence.findLanguage("Markdown")
        val file = LightVirtualFile(AutoDevSnippetFile.naming("md"), language, content)
        markdownEditor = try {
            val document: Document = file.findDocument() ?: throw IllegalStateException("Document not found")
            EditorFactory.getInstance().createEditor(document, project, EditorKind.MAIN_EDITOR) as? EditorEx
        } catch (e: Throwable) {
            throw e
        } ?: throw IllegalStateException("Editor not found")

        EditorUtil.configEditor(markdownEditor, project, file, false)

        val buttonPanel = JPanel(BorderLayout()).apply {
            background = markdownEditor.backgroundColor
        }

        val buttonsBox = Box.createHorizontalBox().apply {
            add(JButton("Save").apply {
                addActionListener {
                    val newContent = markdownEditor.document.text
                    onSave(newContent)
                }
            })
            add(Box.createHorizontalStrut(8))
            add(JButton("Cancel").apply {
                addActionListener {
                    onCancel()
                }
            })
        }
        buttonPanel.add(buttonsBox, BorderLayout.EAST)
        buttonPanel.border = JBUI.Borders.empty(4)

        add(JBScrollPane(markdownEditor.component), BorderLayout.CENTER)
        add(buttonPanel, BorderLayout.SOUTH)
    }

    fun getEditor(): EditorEx = markdownEditor

    override fun dispose() {

    }
} 