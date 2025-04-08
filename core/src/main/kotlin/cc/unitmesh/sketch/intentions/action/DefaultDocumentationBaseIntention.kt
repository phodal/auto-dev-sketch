package cc.unitmesh.sketch.intentions.action

import cc.unitmesh.sketch.AutoDevBundle
import cc.unitmesh.sketch.custom.document.CustomDocumentationConfig
import cc.unitmesh.sketch.intentions.action.base.BasedDocumentationBaseIntention
import cc.unitmesh.sketch.provider.LivingDocumentation
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase

class DefaultDocumentationBaseIntention: BasedDocumentationBaseIntention() {
    override val config: CustomDocumentationConfig = CustomDocumentationConfig.default()

    override fun getText(): String = AutoDevBundle.message("intentions.living.documentation.name")

    override fun getFamilyName(): String = AutoDevBundle.message("intentions.living.documentation.family.name")

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || !file.isWritable) return false

        return LivingDocumentation.forLanguage(file.language)?.let {
            editor.selectionModel.selectedText != null || PsiUtilBase.getElementAtCaret(editor)?.run {
                it.findNearestDocumentationTarget(this) != null
            } ?: false
        } ?: false
    }
}