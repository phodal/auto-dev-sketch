package cc.unitmesh.sketch.observer

import cc.unitmesh.sketch.observer.agent.AgentProcessor
import cc.unitmesh.sketch.provider.observer.AgentObserver
import com.intellij.openapi.project.Project

/**
 * Remote Hook observer will receive the remote hook event and process it.
 * like:
 * - [ ] Jira issue
 * - [ ] GitHub/Gitlab issue
 * and Trigger after processor, and send the notification to the chat window.
 */
class RemoteHookObserver : AgentObserver {
    override fun onRegister(project: Project) {
//        TODO("Not yet implemented")
    }
}

class IssueWorker : AgentProcessor {
    override fun process() {
//        TODO("Not yet implemented")
    }
}