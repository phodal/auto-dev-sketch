// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package cc.unitmesh.sketch.gui.snippet.error

import cc.unitmesh.sketch.AutoDevSnippetFile.isSnippet
import com.intellij.codeInsight.daemon.impl.IntentionActionFilter
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.psi.PsiFile

class CodeBlockIntentionActionFilter : IntentionActionFilter {
    override fun accept(intentionAction: IntentionAction, file: PsiFile?): Boolean {
        val virtualFile = file?.virtualFile ?: return true
        return !isSnippet(virtualFile)
    }
}
