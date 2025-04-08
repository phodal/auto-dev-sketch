package cc.unitmesh.sketch.language.compiler.exec

import cc.unitmesh.sketch.command.InsCommand
import cc.unitmesh.sketch.command.dataprovider.BuiltinCommand
import cc.unitmesh.sketch.language.compiler.error.DEVINS_ERROR
import cc.unitmesh.sketch.provider.devins.DevInsSymbolProvider
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project

class SymbolInsCommand(val myProject: Project, val prop: String) : InsCommand {
    override val commandName: BuiltinCommand = BuiltinCommand.SYMBOL

    override suspend fun execute(): String {
        val result = DevInsSymbolProvider.all().mapNotNull {
            val found = runReadAction { it.resolveSymbol(myProject, prop) }
            if (found.isEmpty()) return@mapNotNull null
            "```${it.language}\n${found.joinToString("\n")}\n```\n"
        }

        if (result.isEmpty()) {
            return "$DEVINS_ERROR No symbol found: $prop"
        }

        return result.joinToString("\n")
    }
}