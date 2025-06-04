package cc.unitmesh.sketch.settings.ui

import cc.unitmesh.sketch.settings.LLMParam
import cc.unitmesh.sketch.settings.ReactiveComboBox
import cc.unitmesh.sketch.settings.ReactiveTextField
import cc.unitmesh.sketch.settings.locale.LanguageChangedCallback.i18nLabel
import com.intellij.util.ui.FormBuilder

/**
 * Helper extension function for FormBuilder to add LLMParam
 */
fun FormBuilder.addLLMParam(llmParam: LLMParam): FormBuilder = apply {
    when (llmParam.type) {
        LLMParam.ParamType.Text -> {
            addLabeledComponent(i18nLabel(llmParam.label), ReactiveTextField(llmParam) {
                this.isEnabled = it.isEditable
            }, 1, false)
        }
        LLMParam.ParamType.ComboBox -> {
            addLabeledComponent(i18nLabel(llmParam.label), ReactiveComboBox(llmParam), 1, false)
        }
        else -> {
            addSeparator()
        }
    }
}
