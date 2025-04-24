package cc.unitmesh.sketch.language.startup

import cc.unitmesh.sketch.language.ast.HobbitHole
import cc.unitmesh.sketch.language.compiler.HobbitHoleParser
import cc.unitmesh.sketch.language.psi.DevInFile
import com.intellij.openapi.editor.Editor

data class DynamicDevInsActionConfig(
    val name: String,
    val hole: HobbitHole? = null,
    val devinFile: DevInFile,
    val editor: Editor? = null,
) {
    companion object {
        fun from(file: DevInFile): DynamicDevInsActionConfig {
            val hole = HobbitHoleParser.parse(file)
            return DynamicDevInsActionConfig(file.name, hole, file)
        }
    }
}