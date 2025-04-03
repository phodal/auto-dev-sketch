package cc.unitmesh.sketch.context.builder

import cc.unitmesh.sketch.context.MethodContext
import com.intellij.psi.PsiElement

/**
 * The MethodContextBuilder interface provides a method for retrieving the method context of a given PsiElement.
 * A method context represents the context in which a method is defined or used within a codebase.
 * @see MethodContext
 */
interface MethodContextBuilder {
    fun getMethodContext(psiElement: PsiElement, includeClassContext: Boolean, gatherUsages: Boolean): MethodContext?
}
