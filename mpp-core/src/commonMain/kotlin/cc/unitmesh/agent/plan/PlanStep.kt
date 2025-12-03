package cc.unitmesh.agent.plan

import cc.unitmesh.agent.plan.TaskStatus.Companion.toMarker
import kotlinx.serialization.Serializable

/**
 * Represents a single step within a plan task.
 * 
 * A step is the smallest unit of work in a plan, with its own status
 * and optional code file references.
 * 
 * Markdown format: `- [status] description [FileName](filepath)`
 */
@Serializable
data class PlanStep(
    /**
     * Unique identifier for this step
     */
    val id: String,
    
    /**
     * Description of what this step accomplishes
     */
    val description: String,
    
    /**
     * Current status of this step
     */
    var status: TaskStatus = TaskStatus.TODO,
    
    /**
     * Code file links referenced in this step
     */
    val codeFileLinks: List<CodeFileLink> = emptyList()
) {
    /**
     * Whether this step is completed
     */
    val isCompleted: Boolean
        get() = status == TaskStatus.COMPLETED
    
    /**
     * Update the status of this step
     */
    fun updateStatus(newStatus: TaskStatus) {
        status = newStatus
    }
    
    /**
     * Mark this step as completed
     */
    fun complete() {
        status = TaskStatus.COMPLETED
    }
    
    /**
     * Mark this step as failed
     */
    fun fail() {
        status = TaskStatus.FAILED
    }
    
    /**
     * Mark this step as in progress
     */
    fun startProgress() {
        status = TaskStatus.IN_PROGRESS
    }
    
    /**
     * Convert to markdown format
     */
    fun toMarkdown(): String {
        val marker = status.toMarker()
        return "- [$marker] $description"
    }
    
    companion object {
        // Use ASCII markers only for WASM compatibility (no UTF-8 checkmark)
        private val STEP_PATTERN = Regex("^\\s*-\\s*\\[\\s*([xXvV!*Bb]?)\\s*]\\s*(.*)")

        /**
         * Parse a step from markdown text
         */
        fun fromMarkdown(text: String, id: String = generateId()): PlanStep? {
            val match = STEP_PATTERN.find(text) ?: return null
            val marker = match.groupValues[1]
            val description = match.groupValues[2].trim()
            val codeFileLinks = CodeFileLink.extractFromText(description)

            return PlanStep(
                id = id,
                description = description,
                status = TaskStatus.fromMarker(marker),
                codeFileLinks = codeFileLinks
            )
        }

        /**
         * Create a step from plain text (without status marker)
         */
        fun fromText(text: String, id: String = generateId()): PlanStep {
            val cleanText = text.trim().removePrefix("-").trim()
            val codeFileLinks = CodeFileLink.extractFromText(cleanText)

            return PlanStep(
                id = id,
                description = cleanText,
                status = TaskStatus.TODO,
                codeFileLinks = codeFileLinks
            )
        }

        // Use atomic-like counter with synchronized access for thread safety
        private var idCounter = 0L
        private val idLock = Any()

        /**
         * Generate a globally unique step ID.
         * Thread-safe implementation using synchronized block.
         */
        fun generateId(): String {
            val counter = synchronized(idLock) { ++idCounter }
            return "step_${counter}_${currentTimeMillis()}"
        }

        // Platform-agnostic time function
        private fun currentTimeMillis(): Long {
            return kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        }
    }
}

