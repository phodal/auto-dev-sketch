package cc.unitmesh.sketch.gui.chat.message

data class ChatContext(
    val postAction: ((response: String) -> Unit)? = null,
    val prefixText: String = "",
    val suffixText: String = ""
)