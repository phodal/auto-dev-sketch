package cc.unitmesh.sketch.update

import cc.unitmesh.sketch.inline.AutoDevInlineChatProvider
import cc.unitmesh.sketch.provider.observer.AgentObserver
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread

class AutoDevUpdateStartupActivity : ProjectActivity {
    @RequiresBackgroundThread
    override suspend fun execute(project: Project) {
        if (ApplicationManager.getApplication().isUnitTestMode) return

        AutoDevInlineChatProvider.addListener(project)
        AgentObserver.register(project)
    }
}