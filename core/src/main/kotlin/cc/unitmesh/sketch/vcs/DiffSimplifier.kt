package cc.unitmesh.sketch.vcs

<<<<<<< HEAD:core/src/main/kotlin/cc/unitmesh/sketch/vcs/DiffSimplifier.kt
import cc.unitmesh.sketch.AutoDevNotifications
=======
import cc.unitmesh.sketch.AutoDevNotifications
import cc.unitmesh.sketch.vcs.context.*
>>>>>>> master:core/src/main/kotlin/cc/unitmesh/devti/vcs/DiffSimplifier.kt
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.*
import com.intellij.project.stateStore
import org.jetbrains.annotations.NotNull
import java.io.StringWriter
import java.nio.file.Path
import java.nio.file.PathMatcher

@Service(Service.Level.PROJECT)
class DiffSimplifier(val project: Project) {
    private val logger = logger<DiffSimplifier>()

    /**
     * Simplifies the given list of changes and returns the resulting diff as a string.
     * Uses context engineering to prioritize files and manage token budget.
     *
     * @param changes The list of changes to be simplified.
     * @param ignoreFilePatterns The list of file patterns to be ignored during simplification.
     * @return The simplified diff as a string.
     * @throws RuntimeException if the project base path is null or if there is an error calculating the diff.
     */
    fun simplify(changes: List<Change>, ignoreFilePatterns: List<PathMatcher>): String {
        return simplifyWithContext(changes, ignoreFilePatterns, maxTokens = 8000)
    }

    /**
     * Simplifies changes with explicit token budget control.
     *
     * @param changes The list of changes to be simplified.
     * @param ignoreFilePatterns The list of file patterns to be ignored during simplification.
     * @param maxTokens Maximum tokens to use for the context.
     * @return The simplified diff as a string.
     */
    fun simplifyWithContext(
        changes: List<Change>,
        ignoreFilePatterns: List<PathMatcher>,
        maxTokens: Int = 8000
    ): String {
        var originChanges = ""

        try {
            val basePath = project.basePath ?: throw RuntimeException("Project base path is null.")

            // Calculate priorities for all changes
            val calculator = FilePriorityCalculator(ignoreFilePatterns)
            val prioritizedChanges = calculator.calculateAndSort(changes)

            if (prioritizedChanges.isEmpty()) {
                return ""
            }

            // Generate full diffs for changes that need them
            val diffContents = generateDiffContents(prioritizedChanges, basePath)

            // Allocate changes based on token budget
            val windowManager = ContextWindowManager.custom(maxTokens)
            val allocation = windowManager.allocateChanges(prioritizedChanges, diffContents)

            // Build final output
            val result = buildDiffOutput(allocation, diffContents)
            originChanges = result

            return DiffFormatter.postProcess(result)
        } catch (e: Exception) {
            if (originChanges.isNotEmpty()) {
                logger.warn("Error calculating diff: $originChanges", e)
            }

            AutoDevNotifications.error(project, "Error calculating diff: ${e.message}")
            return originChanges
        }
    }

    /**
     * Generate diff contents for prioritized changes
     */
    private fun generateDiffContents(
        prioritizedChanges: List<PrioritizedChange>,
        basePath: String
    ): Map<PrioritizedChange, String> {
        val diffContents = mutableMapOf<PrioritizedChange, String>()

        // Group changes by priority to batch process
        val highPriorityChanges = prioritizedChanges.filter {
            it.priority.level >= FilePriority.HIGH.level
        }

        if (highPriorityChanges.isEmpty()) {
            return diffContents
        }

        try {
            val writer = StringWriter()
            val changes = highPriorityChanges.map { it.change }

            val patches = IdeaTextPatchBuilder.buildPatch(
                project, changes, Path.of(basePath), false, true
            )

            UnifiedDiffWriter.write(
                project,
                project.stateStore.projectBasePath,
                patches,
                writer,
                "\n",
                null as CommitContext?,
                emptyList()
            )

            val fullDiff = writer.toString()

            // Parse the full diff and associate with changes
            // For now, store the full diff for each change
            // TODO: Parse individual file diffs from the unified diff
            highPriorityChanges.forEach { prioritizedChange ->
                diffContents[prioritizedChange] = fullDiff
            }
        } catch (e: Exception) {
            logger.warn("Error generating diff contents", e)
        }

        return diffContents
    }

    /**
     * Build final diff output from allocation result
     */
    private fun buildDiffOutput(
        allocation: ContextWindowManager.AllocationResult,
        diffContents: Map<PrioritizedChange, String>
    ): String {
        val output = StringBuilder()

        // Add full diff changes
        val fullDiffStrategy = FullDiffStrategy()
        allocation.fullDiffChanges.forEach { change ->
            val diff = fullDiffStrategy.generateDiff(change, diffContents[change])
            output.append(diff).append("\n")
        }

        // Add summary changes
        val summaryStrategy = SummaryDiffStrategy()
        allocation.summaryChanges.forEach { change ->
            val summary = summaryStrategy.generateDiff(change, diffContents[change])
            output.append(summary).append("\n")
        }

        return output.toString()
    }

    companion object {
        @NotNull
        @Deprecated("Use DiffFormatter.postProcess instead", ReplaceWith("DiffFormatter.postProcess(diffString)"))
        fun postProcess(@NotNull diffString: String): String {
            return DiffFormatter.postProcess(diffString)
        }
    }
}