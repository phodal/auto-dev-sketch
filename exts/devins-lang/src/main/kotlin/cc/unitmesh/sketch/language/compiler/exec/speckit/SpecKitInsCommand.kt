package cc.unitmesh.sketch.language.compiler.exec.speckit

import cc.unitmesh.sketch.command.InsCommand
import cc.unitmesh.sketch.command.dataprovider.BuiltinCommand
import cc.unitmesh.sketch.command.dataprovider.SpecKitCommand
import cc.unitmesh.sketch.language.compiler.error.DEVINS_ERROR
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager

/**
 * SpecKit command implementation for GitHub Spec-Kit Spec-Driven Development.
 *
 * Supports subcommands like:
 * - /speckit.clarify <arguments>
 * - /speckit.specify <arguments>
 * - /speckit.plan <arguments>
 * - /speckit.tasks <arguments>
 * - /speckit.implement <arguments>
 * - /speckit.analyze <arguments>
 * - /speckit.checklist <arguments>
 * - /speckit.constitution <arguments>
 *
 * Example:
 * <devin>
 * /speckit.clarify What are the edge cases for user authentication?
 * </devin>
 *
 * <devin>
 * /speckit.specify Build an application that can help me organize my photos
 * </devin>
 */
class SpecKitInsCommand(
    private val project: Project,
    private val prop: String,
    private val arguments: String
) : InsCommand {
    override val commandName: BuiltinCommand = BuiltinCommand.SPECKIT

    private val logger = logger<SpecKitInsCommand>()

    override fun isApplicable(): Boolean {
        return SpecKitCommand.isAvailable(project)
    }

    override suspend fun execute(): String? {
        // Parse subcommand from prop (e.g., "speckit.clarify" -> "clarify")
        val subcommand = parseSubcommand(prop)
        if (subcommand.isEmpty()) {
            return "$DEVINS_ERROR Invalid speckit command format. Use /speckit.<subcommand> <arguments>"
        }

        // Load the SpecKit command
        val specKitCommand = SpecKitCommand.fromSubcommand(project, subcommand)
        if (specKitCommand == null) {
            val availableCommands = SpecKitCommand.all(project)
                .joinToString(", ") { it.subcommand }
            return "$DEVINS_ERROR Prompt file not found: speckit.$subcommand.prompt.md\n" +
                    "Available commands: $availableCommands"
        }

        try {
            // Execute the command with the new compiler for proper variable resolution
            val result = specKitCommand.executeWithCompiler(project, arguments)

            // Refresh VFS to ensure file changes are visible
            VirtualFileManager.getInstance().refreshWithoutFileWatcher(false)

            return result
        } catch (e: Exception) {
            logger.error("Error executing speckit command: $subcommand", e)
            return "$DEVINS_ERROR Error executing speckit.$subcommand: ${e.message}"
        }
    }

    /**
     * Parse subcommand from prop string.
     * Examples:
     * - "speckit.clarify" -> "clarify"
     * - "clarify" -> "clarify"
     * - ".clarify" -> "clarify"
     */
    private fun parseSubcommand(prop: String): String {
        val trimmed = prop.trim()
        
        // Handle "speckit.clarify" format
        if (trimmed.startsWith("speckit.")) {
            return trimmed.removePrefix("speckit.")
        }
        
        // Handle ".clarify" format
        if (trimmed.startsWith(".")) {
            return trimmed.removePrefix(".")
        }
        
        // Handle "clarify" format directly
        return trimmed
    }
}

