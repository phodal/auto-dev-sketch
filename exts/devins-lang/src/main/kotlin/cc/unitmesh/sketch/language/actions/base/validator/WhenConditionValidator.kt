package cc.unitmesh.sketch.language.actions.base.validator

import cc.unitmesh.sketch.language.ast.FrontMatterType
import cc.unitmesh.sketch.language.ast.Statement
import cc.unitmesh.sketch.devins.variable.ConditionPsiVariable
import com.intellij.psi.PsiFile


object WhenConditionValidator {
    private fun buildPsiVariable(file: PsiFile): Map<String, String> {
        return ConditionPsiVariable.values().associate {
            when (it) {
                ConditionPsiVariable.FILE_PATH -> it.variableName to file.virtualFile.path
                ConditionPsiVariable.FILE_NAME -> it.variableName to file.name
                ConditionPsiVariable.FILE_EXTENSION -> it.variableName to (file.virtualFile.extension ?: "")
                ConditionPsiVariable.FILE_CONTENT -> it.variableName to file.text
            }
        }
    }

    fun isAvailable(conditions: FrontMatterType.EXPRESSION, file: PsiFile): Boolean {
        return (conditions.value as? Statement)?.evaluate(buildPsiVariable(file)) == true
    }
}
