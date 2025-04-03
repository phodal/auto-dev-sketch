package cc.unitmesh.sketch.custom.variable

import cc.unitmesh.sketch.context.MethodContextProvider
import com.intellij.psi.PsiElement

class MethodInputOutputVariableResolver(val element: PsiElement) : VariableResolver {
    override val type: CustomResolvedVariableType = CustomResolvedVariableType.METHOD_INPUT_OUTPUT

    override fun resolve(): String {
        val methodContext = MethodContextProvider(false, false).from(element)
        if (methodContext.name == null) return ""

        return methodContext.inputOutputString()
    }
}
