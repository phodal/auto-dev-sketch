package cc.unitmesh.endpoints.provider

import cc.unitmesh.sketch.provider.context.ChatContextItem
import cc.unitmesh.sketch.provider.context.ChatContextProvider
import cc.unitmesh.sketch.provider.context.ChatCreationContext
import com.intellij.openapi.project.Project

class EndpointsContextProvider : ChatContextProvider {
    override fun isApplicable(project: Project, creationContext: ChatCreationContext): Boolean {
        return false
    }

    override suspend fun collect(
        project: Project,
        creationContext: ChatCreationContext
    ): List<ChatContextItem> {
        return emptyList()
    }
}