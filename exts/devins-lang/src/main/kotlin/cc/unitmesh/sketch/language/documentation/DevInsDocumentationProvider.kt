package cc.unitmesh.sketch.language.documentation

import cc.unitmesh.sketch.agent.custom.model.CustomAgentConfig
import cc.unitmesh.sketch.custom.compile.CustomVariable
import cc.unitmesh.sketch.language.DevInLanguage
import cc.unitmesh.sketch.command.dataprovider.BuiltinCommand
import cc.unitmesh.sketch.language.psi.DevInTypes
import cc.unitmesh.sketch.util.parser.convertMarkdownToHtml
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType

class DevInsDocumentationProvider : AbstractDocumentationProvider() {
    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        val project = element?.project ?: return null
        val markdownDoc = when (element.elementType) {
            DevInTypes.AGENT_ID -> {
                val agentConfigs = CustomAgentConfig.loadFromProject(project).filter {
                    it.name == element.text
                }

                if (agentConfigs.isEmpty()) return null
                agentConfigs.joinToString("\n") { it.description }
            }

            DevInTypes.COMMAND_ID -> {
                val command = BuiltinCommand.all().find { it.commandName == element.text } ?: return null
                val example = BuiltinCommand.example(command)
                val lang = DevInLanguage.displayName
                "${command.description}\nExample:\n```$lang\n$example\n```\n "
            }

            DevInTypes.VARIABLE_ID -> {
                CustomVariable.all().find { it.variable == element.text }?.description
            }

            else -> {
                element.text
            }
        }

        return convertMarkdownToHtml(markdownDoc ?: "")
    }

    override fun getCustomDocumentationElement(
        editor: Editor,
        file: PsiFile,
        contextElement: PsiElement?,
        targetOffset: Int
    ): PsiElement? = contextElement ?: file.findElementAt(targetOffset)
}
