package cc.unitmesh.sketch.language.lexer

import cc.unitmesh.sketch.language.DevInLanguage
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class DevInTokenType(debugName: @NonNls String) : IElementType(debugName, DevInLanguage) {
    override fun toString(): String = "DevInTokenType." + super.toString()
}