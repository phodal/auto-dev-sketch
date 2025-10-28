package cc.unitmesh.sketch.language.compiler.exec.idea

import cc.unitmesh.sketch.AutoDevNotifications
import cc.unitmesh.sketch.command.InsCommand
import cc.unitmesh.sketch.command.dataprovider.BuiltinCommand
import com.intellij.openapi.project.Project
import cc.unitmesh.sketch.util.parser.CodeFence
import cc.unitmesh.sketch.provider.toolchain.ToolchainFunctionProvider

class DatabaseInsCommand(val myProject: Project, private val prop: String, private val codeContent: String?) :
    InsCommand {
    override val commandName: BuiltinCommand = BuiltinCommand.DATABASE

    override fun isApplicable(): Boolean {
        return  ToolchainFunctionProvider.lookup("DatabaseFunctionProvider") != null
    }

    override suspend fun execute(): String {
        val args = if (codeContent != null) {
            val code = CodeFence.parse(codeContent).text
            listOf(code)
        } else {
            listOf()
        }

        val result = try {
            ToolchainFunctionProvider.lookup("DatabaseFunctionProvider")
                ?.execute(myProject, prop, args, emptyMap(), "")
        } catch (e: Exception) {
            AutoDevNotifications.notify(myProject, "Error: ${e.message}")
            return "Error: ${e.message}"
        }

        return result?.toString() ?: "No database provider found"
    }
}
