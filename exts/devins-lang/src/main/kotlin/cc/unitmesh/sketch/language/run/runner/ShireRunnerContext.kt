package cc.unitmesh.sketch.language.run.runner

import cc.unitmesh.sketch.language.ast.HobbitHole
import cc.unitmesh.sketch.language.compiler.DevInsCompiledResult
import com.intellij.openapi.editor.Editor

class ShireRunnerContext(
    val hole: HobbitHole?,
    val editor: Editor?,
    val compileResult: DevInsCompiledResult,
    val finalPrompt: String = "",
    val hasError: Boolean,
    val compiledVariables: Map<String, Any>,
)
