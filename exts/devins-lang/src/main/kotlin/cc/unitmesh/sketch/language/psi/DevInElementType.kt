package cc.unitmesh.sketch.language.psi

import cc.unitmesh.sketch.language.DevInLanguage
import com.intellij.psi.tree.IElementType

class DevInElementType(debugName: String): IElementType(debugName, DevInLanguage.INSTANCE)