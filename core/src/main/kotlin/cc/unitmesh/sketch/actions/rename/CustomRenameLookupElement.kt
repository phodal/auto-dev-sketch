package cc.unitmesh.sketch.actions.rename

import cc.unitmesh.sketch.AutoDevIcons
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.template.impl.TemplateManagerImpl

class CustomRenameLookupElement(val name: String) : LookupElement() {
    override fun getLookupString(): String = name

    override fun handleInsert(context: InsertionContext) {
        val editor = context.editor
        val templateState = TemplateManagerImpl.getTemplateState(editor)

        if (templateState != null && !templateState.isFinished) {
            templateState.considerNextTabOnLookupItemSelected(this)
        }
    }

    override fun renderElement(presentation: LookupElementPresentation) {
        presentation.icon = AutoDevIcons.IDEA
        super.renderElement(presentation)
    }
}