package cc.unitmesh.sketch.mcp

import cc.unitmesh.sketch.settings.coder.coderSetting
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfo
import java.io.File

internal class MCPServerStartupValidator : ProjectActivity {
    private val GROUP_ID = "UnitMesh.MCPServer"

    val logger by lazy { logger<MCPServerStartupValidator>() }

    fun isNpxInstalled(): Boolean {
        return try {
            logger.info("Starting npx installation check")
            if (SystemInfo.isWindows) {
                logger.info("Detected Windows OS, using 'where' command")
                checkNpxWindows()
            } else {
                logger.info("Detected non-Windows OS, checking known locations")
                checkNpxUnix()
            }
        } catch (e: Exception) {
            logger.error("Failed to check npx installation", e)
            logger.error("Exception details - Class: ${e.javaClass.name}, Message: ${e.message}")
            false
        }
    }

    private fun checkNpxWindows(): Boolean {
        val commandLine = GeneralCommandLine("where", "npx")
        logger.info("Windows - Environment PATH: ${commandLine.environment["PATH"]}")

        val handler = OSProcessHandler(commandLine)
        val output = StringBuilder()
        val error = StringBuilder()

        handler.addProcessListener(object : ProcessAdapter() {
            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                when (outputType) {
                    ProcessOutputTypes.STDOUT -> output.append(event.text)
                    ProcessOutputTypes.STDERR -> error.append(event.text)
                }
            }
        })

        handler.startNotify()
        val completed = handler.waitFor(5000)

        logger.info("Windows - where npx completed with success: $completed")
        if (output.isNotBlank()) logger.info("Windows - Output: $output")
        if (error.isNotBlank()) logger.warn("Windows - Error: $error")

        return completed && handler.exitCode == 0
    }

    private fun checkNpxUnix(): Boolean {
        // First try checking known locations including user-specific installations
        val homeDir = System.getProperty("user.home")
        val knownPaths = listOf(
            "/opt/homebrew/bin/npx",
            "/usr/local/bin/npx",
            "/usr/bin/npx",
            "$homeDir/.volta/bin/npx",  // Volta installation
            "$homeDir/.nvm/current/bin/npx",  // NVM installation
            "$homeDir/.npm-global/bin/npx"    // NPM global installation
        )

        logger.info("Unix - Checking known npx locations: ${knownPaths.joinToString(", ")}")

        val existingPath = knownPaths.find { path ->
            File(path).also {
                logger.info("Unix - Checking path: $path exists: ${it.exists()}")
            }.exists()
        }

        if (existingPath != null) {
            logger.info("Unix - Found npx at: $existingPath")
            return true
        }

        // Fallback to which command with extended PATH
        logger.info("Unix - No npx found in known locations, trying which command")
        val commandLine = GeneralCommandLine("which", "npx")

        // Add all potential paths to PATH
        val currentPath = System.getenv("PATH") ?: ""
        val additionalPaths = listOf(
            "/opt/homebrew/bin",
            "/opt/homebrew/sbin",
            "/usr/local/bin",
            "$homeDir/.volta/bin",
            "$homeDir/.nvm/current/bin",
            "$homeDir/.npm-global/bin"
        ).joinToString(":")
        commandLine.environment["PATH"] = "$additionalPaths:$currentPath"
        logger.info("Unix - Modified PATH for which command: ${commandLine.environment["PATH"]}")

        val handler = OSProcessHandler(commandLine)
        val output = StringBuilder()
        val error = StringBuilder()

        handler.addProcessListener(object : ProcessAdapter() {
            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                when (outputType) {
                    ProcessOutputTypes.STDOUT -> output.append(event.text)
                    ProcessOutputTypes.STDERR -> error.append(event.text)
                }
            }
        })

        handler.startNotify()
        val completed = handler.waitFor(5000)

        logger.info("Unix - which npx completed with success: $completed")
        logger.info("Unix - which npx completed with code: ${handler.exitCode}")
        if (output.isNotBlank()) logger.info("Unix - Output: $output")
        if (error.isNotBlank()) logger.warn("Unix - Error: $error")

        return completed && handler.exitCode == 0
    }

    override suspend fun execute(project: Project) {
        val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup(GROUP_ID)
        if (!project.coderSetting.state.enableExportAsMcpServer) {
            logger.info("MCP Server is disabled, skipping validation")
            return
        }

        val npxInstalled = isNpxInstalled()
        if (!npxInstalled) {
            val notification = notificationGroup.createNotification(
                "Node is not installed",
                "MCP Server Proxy requires Node.js to be installed",
                NotificationType.INFORMATION
            )
            notification.addAction(NotificationAction.createSimpleExpiring("Open Installation Instruction") {
                BrowserUtil.open("https://nodejs.org/en/download/package-manager")
            })

            notification.notify(project)
        }
    }
}