package cc.unitmesh.sketch.custom.action

import cc.unitmesh.sketch.provider.context.ChatContextItem

class CustomIntentionPrompt(
    val displayPrompt: String,
    val requestPrompt: String,
    val contextItems: List<ChatContextItem> = listOf()
)