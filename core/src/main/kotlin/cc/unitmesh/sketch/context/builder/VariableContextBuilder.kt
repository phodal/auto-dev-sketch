package cc.unitmesh.sketch.context.builder

import cc.unitmesh.sketch.context.VariableContext
import com.intellij.psi.PsiElement

interface VariableContextBuilder {
    fun getVariableContext(
        psiElement: PsiElement,
        withMethodContext: Boolean,
        withClassContext: Boolean,
        gatherUsages: Boolean
    ): VariableContext?
}
