package cc.unitmesh.sketch.language.compiler

import cc.unitmesh.sketch.command.dataprovider.BuiltinCommand
import cc.unitmesh.sketch.sketch.SketchToolchainProvider
import cc.unitmesh.sketch.agent.tool.AgentTool

class DevInsSketchToolchainProvider : SketchToolchainProvider {
    override fun collect(): List<AgentTool> {
        /// we need to ignore some bad case for llm
        return BuiltinCommand.all()
            .filter {
                it.enableInSketch
            }
            .map {
            val example = BuiltinCommand.example(it)
            AgentTool(it.commandName, it.description, example)
        }
    }
}
