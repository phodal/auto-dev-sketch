package cc.unitmesh.sketch.custom.variable

import cc.unitmesh.sketch.context.ClassContextProvider
import com.intellij.psi.PsiElement

class ClassStructureVariableResolver(val element: PsiElement) : VariableResolver {
    override val type: CustomResolvedVariableType = CustomResolvedVariableType.METHOD_INPUT_OUTPUT

    override fun resolve(): String {
        val classContext = ClassContextProvider(false).from(element)
        if (classContext.name == null) return ""

        return classContext.format()
    }
}
