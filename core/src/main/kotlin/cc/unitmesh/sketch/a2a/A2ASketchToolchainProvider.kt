package cc.unitmesh.sketch.a2a

import cc.unitmesh.sketch.agent.tool.AgentTool
import cc.unitmesh.sketch.sketch.SketchToolchainProvider
import com.intellij.openapi.project.Project
import io.a2a.spec.AgentCard

/**
 * A2A Sketch Toolchain Provider that converts A2A agents to AgentTool format
 * for use in the Sketch system.
 */
class A2ASketchToolchainProvider : SketchToolchainProvider {
    override fun collect(): List<AgentTool> {
        // Since we can't get project from interface, we need to find it another way
        // For now, return empty list as this will be called from SketchRunContext directly
        return emptyList()
    }

    companion object {
        fun collectA2ATools(project: Project): List<AgentTool> {
            return try {
                val a2aService = project.getService(A2AService::class.java)

                // Initialize A2A service from configuration
                a2aService.initialize()

                val agentCards = a2aService.getAvailableAgents()

                agentCards.map { agentCard ->
                    convertAgentCardToTool(agentCard)
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

        private fun convertAgentCardToTool(agentCard: AgentCard): AgentTool {
            val name = try {
                agentCard.name() ?: "unknown_agent"
            } catch (e: Exception) {
                "unknown_agent"
            }

            val description = try {
                agentCard.description() ?: "A2A Agent"
            } catch (e: Exception) {
                "A2A Agent"
            }

            val skills = try {
                agentCard.skills()?.joinToString(", ") { skill ->
                    try {
                        when {
                            skill.javaClass.simpleName == "AgentSkill" -> {
                                val nameMethod = skill.javaClass.getMethod("name")
                                nameMethod.invoke(skill) as? String ?: "skill"
                            }
                            else -> skill.toString()
                        }
                    } catch (e: Exception) {
                        "skill"
                    }
                } ?: ""
            } catch (e: Exception) {
                ""
            }

            val fullDescription = if (skills.isNotEmpty()) {
                "$description. Available skills: $skills"
            } else {
                description
            }

            val example = generateExampleUsage(name)
            return AgentTool(
                name = name,
                description = fullDescription,
                example = example,
                isMcp = false,
                completion = "",
                mcpGroup = "a2a",
                isDevIns = false,
                devinScriptPath = ""
            )
        }

        private fun generateExampleUsage(agentName: String): String {
            return """
                /a2a $agentName "Please help me with my task"
            """.trimIndent()
        }
    }
}
