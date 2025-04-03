package cc.unitmesh.sketch.language.completion.provider

import cc.unitmesh.sketch.command.dataprovider.FileFunc
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext

class FileFunctionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        FileFunc.all().forEach {
            val element = LookupElementBuilder.create(it.funcName)
                .withIcon(it.icon)
                .withTypeText(it.description, true)

            result.addElement(element)
        }
    }
}
