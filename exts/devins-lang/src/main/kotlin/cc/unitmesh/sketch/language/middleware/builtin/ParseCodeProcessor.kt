package cc.unitmesh.sketch.language.middleware.builtin

import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.project.Project
import cc.unitmesh.sketch.devins.post.PostProcessorType
import cc.unitmesh.sketch.devins.post.PostProcessorContext
import cc.unitmesh.sketch.devins.post.PostProcessor
import cc.unitmesh.sketch.util.parser.CodeFence

class ParseCodeProcessor : PostProcessor {
    override val processorName: String = PostProcessorType.ParseCode.handleName
    override val description: String = "`parseCode` will parse the markdown from llm response."

    override fun isApplicable(context: PostProcessorContext): Boolean = true

    override fun execute(project: Project, context: PostProcessorContext, console: ConsoleView?, args: List<Any>): String {
        val code = CodeFence.parse(context.genText ?: "")
        val codeText = code.text

        context.genTargetLanguage = code.language
        context.genTargetExtension = code.extension

        context.pipeData["output"] = codeText
        context.pipeData["code"] = codeText

        return codeText
    }
}
