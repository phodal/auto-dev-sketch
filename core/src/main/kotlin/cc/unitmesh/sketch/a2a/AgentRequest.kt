package cc.unitmesh.sketch.a2a

import kotlinx.serialization.Serializable

/**
 * Request for A2A agent communication
 */
@Serializable
data class AgentRequest(
    val agent: String,
    val message: String
)
