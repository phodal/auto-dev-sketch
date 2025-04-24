package cc.unitmesh.sketch.language.ast.variable

import cc.unitmesh.sketch.devins.variable.ContextVariable
import cc.unitmesh.sketch.devins.variable.PsiContextVariable
import cc.unitmesh.sketch.devins.variable.SystemInfoVariable
import cc.unitmesh.sketch.devins.variable.ToolchainVariable
import cc.unitmesh.sketch.devins.variable.toolchain.DatabaseToolchainVariable
import cc.unitmesh.sketch.devins.variable.toolchain.VcsToolchainVariable

data class VariableDisplay(
    val name: String,
    val description: String,
    val priority: Double = 0.0
)

object CompositeVariableProvider {
    fun all(): List<VariableDisplay> {
        val results = mutableListOf<VariableDisplay>()

        ContextVariable.entries.forEach {
            results.add(VariableDisplay(it.variableName, it.description, 99.0))
        }

        PsiContextVariable.entries.forEach {
            results.add(VariableDisplay(it.variableName, it.description ?: "", 90.0))
        }

        VcsToolchainVariable.entries.forEach {
            results.add(VariableDisplay(it.variableName, it.description, 80.0))
        }

        DatabaseToolchainVariable.entries.forEach {
            results.add(VariableDisplay(it.variableName, it.description, 70.0))
        }

        ToolchainVariable.all().forEach {
            results.add(VariableDisplay(it.variableName, it.description, 70.0))
        }

        SystemInfoVariable.all().forEach {
            results.add(VariableDisplay(it.variableName, it.description, 60.0))
        }

        return results
    }
}