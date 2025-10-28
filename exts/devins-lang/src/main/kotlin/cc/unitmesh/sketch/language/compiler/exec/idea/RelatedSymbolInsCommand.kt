<<<<<<<< HEAD:exts/devins-lang/src/main/kotlin/cc/unitmesh/sketch/language/compiler/exec/RelatedSymbolInsCommand.kt
package cc.unitmesh.sketch.language.compiler.exec
========
package cc.unitmesh.sketch.language.compiler.exec.idea
>>>>>>>> master:exts/devins-lang/src/main/kotlin/cc/unitmesh/devti/language/compiler/exec/idea/RelatedSymbolInsCommand.kt

import cc.unitmesh.sketch.command.InsCommand
import cc.unitmesh.sketch.command.dataprovider.BuiltinCommand
import cc.unitmesh.sketch.provider.RelatedClassesProvider
import cc.unitmesh.sketch.provider.devins.DevInsSymbolProvider
import com.intellij.openapi.project.Project

class RelatedSymbolInsCommand(val myProject: Project, private val symbol: String) : InsCommand {
    override val commandName: BuiltinCommand = BuiltinCommand.RELATED

    override suspend fun execute(): String? {
        val elements = DevInsSymbolProvider.all().map {
            it.resolveElement(myProject, symbol)
        }.flatten()

        if (elements.isEmpty()) return null

        val psiElements = elements.mapNotNull {
            RelatedClassesProvider.provide(it.language)?.lookupIO(it)
        }.flatten()

        if (psiElements.isEmpty()) return null

        return psiElements.joinToString("\n") { it.text }
    }
}
