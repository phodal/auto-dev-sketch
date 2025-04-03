package cc.unitmesh.sketch.language.completion

import cc.unitmesh.sketch.language.completion.provider.CustomCommandCompletion
import cc.unitmesh.sketch.language.completion.provider.ToolchainCommandCompletion
import cc.unitmesh.sketch.language.psi.DevInTypes
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns

class UserCustomCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(DevInTypes.COMMAND_ID), CustomCommandCompletion())
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(DevInTypes.COMMAND_ID), ToolchainCommandCompletion())
    }
}
