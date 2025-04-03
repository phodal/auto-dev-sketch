package cc.unitmesh.sketch.provider.builtin

import cc.unitmesh.sketch.provider.ContextPrompter
import cc.unitmesh.sketch.provider.context.ChatCreationContext
import cc.unitmesh.sketch.provider.context.ChatOrigin
import kotlinx.coroutines.runBlocking

class DefaultContextPrompter : ContextPrompter() {
    override fun displayPrompt(): String = getPrompt()

    override fun requestPrompt(): String = getPrompt()

    private fun getPrompt(): String {
        var additionContext: String
        runBlocking {
            val creationContext = ChatCreationContext(ChatOrigin.ChatAction, action!!, file, emptyList(), null)
            additionContext = collectionContext(creationContext)
        }

        val prompt = action!!.instruction(lang, project).requestText
        if (file == null) {
            return "$prompt\n$additionContext\n```${lang}\n$selectedText\n```"
        }

        return "$prompt\n```${lang}\n$selectedText\n```"
    }
}