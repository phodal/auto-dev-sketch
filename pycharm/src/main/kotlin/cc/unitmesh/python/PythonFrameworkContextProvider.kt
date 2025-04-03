package cc.unitmesh.python

import cc.unitmesh.sketch.provider.context.ChatContextItem
import cc.unitmesh.sketch.provider.context.ChatContextProvider
import cc.unitmesh.sketch.provider.context.ChatCreationContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.jetbrains.python.configuration.PyConfigurableInterpreterList
import com.jetbrains.python.sdk.PythonSdkUtil
import com.jetbrains.python.sdk.mostPreferred

class PythonFrameworkContextProvider : ChatContextProvider {
    override fun isApplicable(project: Project, creationContext: ChatCreationContext) = creationContext.element?.language?.displayName == "Python"

    override suspend fun collect(project: Project, creationContext: ChatCreationContext): List<ChatContextItem> {
        var items = mutableListOf<ChatContextItem>()
        val allSdks = PythonSdkUtil.getAllSdks()
        val preferred = mostPreferred(allSdks)

        val myInterpreterList = PyConfigurableInterpreterList.getInstance(project)
        val projectSdk = ProjectRootManager.getInstance(project).projectSdk
            ?: preferred
            ?: myInterpreterList.allPythonSdks.firstOrNull()

        if (projectSdk != null) {
            val context = "This project is using Python SDK ${projectSdk.name}"
            items.add(ChatContextItem(PythonFrameworkContextProvider::class, context))
        }

        return items
    }
}