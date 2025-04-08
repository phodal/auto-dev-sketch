package cc.unitmesh.sketch.actions.chat

import cc.unitmesh.sketch.actions.chat.base.ChatCheckForUpdateAction
import cc.unitmesh.sketch.gui.chat.message.ChatActionType
import cc.unitmesh.sketch.settings.locale.LanguageChangedCallback.presentationText

class ExplainThisAction() : ChatCheckForUpdateAction() {
    init{
        presentationText("settings.autodev.rightClick.explain", templatePresentation)
    }

    override fun getActionType(): ChatActionType = ChatActionType.EXPLAIN
}
