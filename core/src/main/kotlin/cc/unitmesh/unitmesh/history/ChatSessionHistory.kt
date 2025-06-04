package cc.unitmesh.sketch.history

import cc.unitmesh.sketch.llms.custom.Message
import kotlinx.serialization.Serializable

@Serializable
data class ChatSessionHistory(
    val id: String,
    val name: String,
    val messages: List<Message>,
    val createdAt: Long
)