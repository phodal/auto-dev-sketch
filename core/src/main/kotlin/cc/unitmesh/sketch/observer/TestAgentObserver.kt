package cc.unitmesh.sketch.observer

import cc.unitmesh.sketch.observer.test.RunTestUtil
import cc.unitmesh.sketch.provider.observer.AgentObserver
import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsAdapter
import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsListener
import com.intellij.execution.testframework.sm.runner.SMTestProxy
import com.intellij.openapi.Disposable
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.util.messages.MessageBusConnection
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TestAgentObserver : AgentObserver, Disposable {
    private var connection: MessageBusConnection? = null

    @OptIn(DelicateCoroutinesApi::class)
    override fun onRegister(project: Project) {
        connection = project.messageBus.connect()
        connection?.subscribe(SMTRunnerEventsListener.TEST_STATUS, object : SMTRunnerEventsAdapter() {
            override fun onTestFailed(test: SMTestProxy) {
                GlobalScope.launch {
                    delay(3000)
                    sendResult(test, project, ProjectScope.getProjectScope(project))
                }
            }
        })
    }

    fun sendResult(test: SMTestProxy, project: Project, searchScope: GlobalSearchScope) {
        val task = object : Task.Backgroundable(project, "Processing context", false) {
            override fun run(indicator: ProgressIndicator) {
                val prompt = RunTestUtil.buildFailurePrompt(project, test, searchScope)
                sendErrorNotification(project, prompt)
            }
        }

        ProgressManager.getInstance()
            .runProcessWithProgressAsynchronously(task, BackgroundableProcessIndicator(task))
    }

    override fun dispose() {
        connection?.disconnect()
    }
}
