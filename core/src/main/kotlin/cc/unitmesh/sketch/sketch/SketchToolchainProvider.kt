package cc.unitmesh.sketch.sketch

import cc.unitmesh.sketch.agent.tool.AgentTool
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

interface SketchToolchainProvider {
    fun collect(): List<AgentTool>

    companion object {
        private val EP_NAME: ExtensionPointName<SketchToolchainProvider> =
            ExtensionPointName.create("cc.unitmesh.sketchToolchainProvider")

        fun collect(project: Project): List<AgentTool> {
            return EP_NAME.extensionList.flatMap {
                it.collect()
            }
        }
    }
}