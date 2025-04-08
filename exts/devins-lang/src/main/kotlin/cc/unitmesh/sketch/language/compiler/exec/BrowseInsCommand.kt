package cc.unitmesh.sketch.language.compiler.exec

import cc.unitmesh.sketch.agent.tool.browse.Browse
import cc.unitmesh.sketch.command.InsCommand
import cc.unitmesh.sketch.command.dataprovider.BuiltinCommand
import com.intellij.openapi.project.Project

class BrowseInsCommand(val myProject: Project, private val prop: String) : InsCommand {
    override val commandName: BuiltinCommand = BuiltinCommand.BROWSE

    override suspend fun execute(): String? {
        val parse = Browse.parse(prop)
        return parse.body
    }
}

