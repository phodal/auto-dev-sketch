package cc.unitmesh.sketch.actions.chat

import cc.unitmesh.sketch.actions.chat.base.collectElementProblemAsSting
import cc.unitmesh.sketch.gui.chat.message.ChatActionType
import cc.unitmesh.sketch.settings.locale.LanguageChangedCallback.presentationText
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

class FixThisAction : RefactorThisAction() {
    init {
        presentationText("settings.autodev.rightClick.fixThis", templatePresentation)
    }

    override fun getActionType(): ChatActionType = ChatActionType.FIX_ISSUE

    override fun addAdditionPrompt(project: Project, editor: Editor, element: PsiElement): String {
        return collectElementProblemAsSting(element, project, editor)
    }
}
