package cc.unitmesh.shirecore.variable.frontend

import cc.unitmesh.sketch.language.ast.shireql.variable.frontend.Component

interface ComponentProvider {
    fun getPages(): List<Component>
    fun getComponents(): List<Component>
    fun getRoutes(): Map<String, String>
}
