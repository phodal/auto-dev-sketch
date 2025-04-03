package cc.unitmesh.sketch.agent.tool

import cc.unitmesh.sketch.agent.Tool
import kotlinx.serialization.Serializable

@Serializable
data class AgentTool(
    override val name: String, override val description: String,
    val example: String,
    val isMcp: Boolean = false,
    val completion: String = "",
    val mcpGroup: String = "",
) : Tool {
    override fun toString(): String {
        val string = if (description.isEmpty()) "" else """desc: $description"""
        val example = if (example.isEmpty()) "" else """example:
<devin>
$example
</devin>"""
        return """<tool>name: ${name}, $string, $example
</tool>"""
    }
}