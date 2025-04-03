package cc.unitmesh.sketch.custom.variable

interface VariableResolver {
    val type: CustomResolvedVariableType
    fun resolve(): String
    fun variableName() = type.name
}