package cc.unitmesh.sketch.language.startup

import cc.unitmesh.sketch.agent.tool.AgentTool
import cc.unitmesh.sketch.language.actions.DevInsRunFileAction
import cc.unitmesh.sketch.language.compiler.error.DEVINS_ERROR
import cc.unitmesh.sketch.provider.DevInsAgentToolCollector
import cc.unitmesh.sketch.util.relativePath
import com.intellij.openapi.project.Project

class DevInsAgentToolchainProviderCollector : DevInsAgentToolCollector {
    override fun collect(project: Project): List<AgentTool> {
        val actions = DynamicShireActionService.getInstance(project).getAllActions().filter {
            it.hole?.agentic == true
        }

        return actions.map {
            AgentTool(
                it.hole?.name ?: "<Placeholder>",
                it.hole?.description ?: "<No Description>",
                "",
                devinScriptPath = it.devinFile.virtualFile.relativePath(project),
                isDevIns = true
            )
        }
    }

    override suspend fun execute(project: Project, agentName: String, input: String): String? {
        val config = DynamicShireActionService.getInstance(project).getAllActions().firstOrNull {
            it.hole?.agentic == true && it.hole.name == agentName
        } ?: return "$DEVINS_ERROR No action found for agent name: $agentName"

        return DevInsRunFileAction.suspendExecuteFile(project, config.devinFile)
    }
}