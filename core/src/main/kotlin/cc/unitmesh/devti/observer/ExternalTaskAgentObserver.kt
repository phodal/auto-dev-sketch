package cc.unitmesh.devti.observer

import cc.unitmesh.devti.provider.observer.AgentObserver
import com.intellij.execution.ExecutionListener
import com.intellij.execution.ExecutionManager
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.Disposable
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemProcessHandler
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.util.messages.MessageBusConnection


class ExternalTaskAgentObserver : AgentObserver, Disposable {
    private var connection: MessageBusConnection? = null

    override fun onRegister(project: Project) {
        connection = project.messageBus.connect()
        connection?.subscribe(ExecutionManager.EXECUTION_TOPIC, object : ExecutionListener {
            private var globalBuffer = StringBuilder()

            override fun processStarted(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler) {
                if (handler is ExternalSystemProcessHandler) {
                    handler.addProcessListener(object : ProcessListener {
                        private val outputBuffer = StringBuilder()
                        override fun onTextAvailable(
                            event: ProcessEvent,
                            outputType: Key<*>
                        ) {
                            outputBuffer.append(event.text)
                        }

                        override fun processTerminated(event: ProcessEvent) {
                            globalBuffer = outputBuffer
                        }
                    })
                }
            }

            override fun processTerminated(
                executorId: String,
                env: ExecutionEnvironment,
                handler: ProcessHandler,
                exitCode: Int
            ) {
                if (handler is ExternalSystemProcessHandler && exitCode != 0) {
                    val prompt = "Help Me fix follow build issue:\n$globalBuffer"
                    sendErrorNotification(project, prompt)
                }
            }
        })
    }

    private fun getThreadTrace(thread: Thread, depth: Int): String { // debugging
        val buf = StringBuilder()
        val trace = thread.getStackTrace()
        var i = 0
        while (i < depth && i < trace.size) {
            val element = trace[i]
            buf.append("\tat ").append(element).append("\n")
            i++
        }
        return buf.toString()
    }

    override fun dispose() {
        connection?.disconnect()
    }
}
