package cc.unitmesh.sketch.observer.agent

import cc.unitmesh.sketch.agent.tool.AgentTool
import cc.unitmesh.sketch.llms.custom.Message
import cc.unitmesh.sketch.observer.plan.AgentTaskEntry
import com.intellij.openapi.vcs.changes.Change
import java.util.UUID

data class AgentState(
    /**
     * First question of user
     */
    var originIntention: String = "",

    var conversationId: String = UUID.randomUUID().toString(),

    var changes: MutableList<Change> = mutableListOf(),

    var messages: List<Message> = emptyList(),

    var usedTools: List<AgentTool> = emptyList(),

    /**
     * Logging environment variables, maybe related to  [cc.unitmesh.sketch.provider.context.ChatContextProvider]
     */
    var environment: Map<String, String> = emptyMap(),

    var plan: MutableList<AgentTaskEntry> = mutableListOf()
)

