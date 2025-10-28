package cc.unitmesh.sketch.language.compiler.exec.agents

import cc.unitmesh.sketch.a2a.A2AService
import cc.unitmesh.sketch.a2a.A2ASketchToolchainProvider
import cc.unitmesh.sketch.a2a.AgentRequest
import cc.unitmesh.sketch.command.InsCommand
import cc.unitmesh.sketch.command.dataprovider.BuiltinCommand
import cc.unitmesh.sketch.language.compiler.error.DEVINS_ERROR
import cc.unitmesh.sketch.provider.DevInsAgentToolCollector
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import kotlinx.serialization.json.Json

/**
 * Agents command implementation for listing and invoking AI agents.
 *
 * Example:
 * List all available agents:
 * <devin>
 * /agents
 * </devin>
 *
 * Invoke an agent with JSON:
 * <devin>
 * /agents
 * ```json
 * {
 *   "agent": "code-reviewer",
 *   "message": "Please review this code"
 * }
 * ```
 * </devin>
 */
class AgentsInsCommand(
    private val project: Project,
    private val prop: String,
    private val codeContent: String
) : InsCommand {
    override val commandName: BuiltinCommand = BuiltinCommand.AGENTS

    override fun isApplicable(): Boolean = true

    override suspend fun execute(): String? {
        if (prop.isBlank() && codeContent.isBlank()) {
            return listAllAgents()
        }

        val request = parseRequest(prop, codeContent)
            ?: return "$DEVINS_ERROR Invalid request format. Use JSON: {\"agent\": \"agent-name\", \"message\": \"your message\"}"

        if (request.agent.isEmpty()) {
            return "$DEVINS_ERROR Agent name is required."
        }

        if (request.message.isEmpty()) {
            return "$DEVINS_ERROR Message is required."
        }

        return invokeAgent(request.agent, request.message)
    }

    private fun listAllAgents(): String {
        val result = StringBuilder()
        result.append("Available AI Agents:\n\n")

        val a2aAgents = A2ASketchToolchainProvider.collectA2ATools(project)
        val devInsAgents = DevInsAgentToolCollector.all(project)

        if (a2aAgents.isEmpty() && devInsAgents.isEmpty()) {
            result.append("No agents available. Please configure A2A agents or create DevIns agents.\n")
            return result.toString()
        }

        appendUsageExamples(result)

        if (a2aAgents.isNotEmpty()) {
            result.append("## A2A Agents\n\n")
            a2aAgents.forEachIndexed { index, agent ->
                appendAgentInfo(result, index + 1, agent.name, agent.description)
            }
        }

        if (devInsAgents.isNotEmpty()) {
            result.append("## DevIns Agents\n\n")
            devInsAgents.forEachIndexed { index, agent ->
                val scriptPath = agent.devinScriptPath
                appendAgentInfo(result, index + 1, agent.name, agent.description, scriptPath)
            }
        }

        result.append("---\n")
        result.append("Total: ${a2aAgents.size + devInsAgents.size} agent(s) available\n")

        return result.toString()
    }

    private fun appendUsageExamples(result: StringBuilder) {
        result.append("## Usage Examples\n\n")
        result.append("JSON format:\n")
        result.append(formatAgentExample("agent-name", "your message here"))
    }

    private fun appendAgentInfo(
        result: StringBuilder,
        index: Int,
        name: String,
        description: String,
        scriptPath: String? = null
    ) {
        result.append("### $index. $name\n")

        if (description.isNotEmpty()) {
            result.append("**Description**: $description\n\n")
        }

        if (!scriptPath.isNullOrEmpty()) {
            result.append("**Script**: $scriptPath\n\n")
        }

        result.append("**Example**:\n")
        result.append(formatAgentExample(name, "Please help with this task"))
    }

    private fun formatAgentExample(agentName: String, message: String): String {
        return buildString {
            append("<devin>\n")
            append("/agents\n")
            append("```json\n")
            append("{\n")
            append("  \"agent\": \"$agentName\",\n")
            append("  \"message\": \"$message\"\n")
            append("}\n")
            append("```\n")
            append("</devin>\n\n")
        }
    }

    private fun parseRequest(prop: String, codeContent: String): AgentRequest? {
        // Try JSON format first
        if (codeContent.isNotBlank()) {
            try {
                return Json.Default.decodeFromString(codeContent)
            } catch (e: Exception) {
                logger<AgentsInsCommand>().warn("Failed to parse JSON request: $e")
            }
        }

        val (agentName, message) = parseCommand(prop)
        return if (agentName.isNotEmpty()) {
            AgentRequest(agentName, message)
        } else {
            null
        }
    }

    /**
     * Parse the command string to extract agent name and message.
     * Expected format: "<agent_name> \"<message>\"" or "<agent_name> <message>"
     */
    private fun parseCommand(input: String): Pair<String, String> {
        val trimmed = input.trim()

        if (trimmed.isEmpty()) {
            return Pair("", "")
        }

        // Try to parse quoted message first
        val quotedMessageRegex = """^(\S+)\s+"(.+)"$""".toRegex()
        val quotedMatch = quotedMessageRegex.find(trimmed)
        if (quotedMatch != null) {
            val agentName = quotedMatch.groupValues[1]
            val message = quotedMatch.groupValues[2]
            return Pair(agentName, message)
        }

        // Try to parse single quoted message
        val singleQuotedRegex = """^(\S+)\s+'(.+)'$""".toRegex()
        val singleQuotedMatch = singleQuotedRegex.find(trimmed)
        if (singleQuotedMatch != null) {
            val agentName = singleQuotedMatch.groupValues[1]
            val message = singleQuotedMatch.groupValues[2]
            return Pair(agentName, message)
        }

        // Fallback: split by first space
        val parts = trimmed.split(" ", limit = 2)
        if (parts.size >= 2) {
            return Pair(parts[0], parts[1])
        } else if (parts.size == 1) {
            return Pair(parts[0], "")
        }

        return Pair("", "")
    }

    /**
     * Invoke a specific agent by name
     */
    private suspend fun invokeAgent(agentName: String, input: String): String? {
        val a2aService = project.getService(A2AService::class.java)
        a2aService.initialize()

        if (a2aService.isAvailable()) {
            val a2aAgents = A2ASketchToolchainProvider.collectA2ATools(project)
            val selectedAgent = a2aAgents.find { it.name == agentName }
            if (selectedAgent != null) {
                try {
                    val response = a2aService.sendMessage(agentName, input)
                    if (response != null) {
                        return "A2A Agent '$agentName' response:\n$response"
                    }
                } catch (e: Exception) {
                    return "$DEVINS_ERROR Error invoking A2A agent '$agentName': ${e.message}"
                }
            }
        }

        val devInsAgents = DevInsAgentToolCollector.all(project)
        val devInsAgent = devInsAgents.find { it.name == agentName }

        if (devInsAgent == null) {
            return "$DEVINS_ERROR Agent '$agentName' not found. Use /agents to list all available agents."
        }

        return try {
            val collectors = com.intellij.openapi.extensions.ExtensionPointName
                .create<DevInsAgentToolCollector>("cc.unitmesh.devInsAgentTool")
                .extensionList

            for (collector in collectors) {
                val result = collector.execute(project, agentName, input)
                if (result != null) {
                    return "DevIns Agent '$agentName' response:\n$result"
                }
            }

            "$DEVINS_ERROR Failed to execute DevIns agent '$agentName'"
        } catch (e: Exception) {
            "$DEVINS_ERROR Error executing DevIns agent '$agentName': ${e.message}"
        }
    }
}
