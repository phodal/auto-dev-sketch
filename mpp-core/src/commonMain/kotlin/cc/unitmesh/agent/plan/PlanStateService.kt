package cc.unitmesh.agent.plan

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Service for managing plan state.
 * 
 * Provides reactive state management using StateFlow and
 * listener-based notifications for plan updates.
 * 
 * This is the central point for all plan-related state management
 * in the agent system.
 */
class PlanStateService {

    private val _currentPlan = MutableStateFlow<AgentPlan?>(null)

    /**
     * Observable state of the current plan.
     * Use this for reactive UI updates.
     */
    val currentPlan: StateFlow<AgentPlan?> = _currentPlan.asStateFlow()

    // Use synchronized list for thread safety
    private val listeners = mutableListOf<PlanUpdateListener>()
    private val listenersLock = Any()
    
    /**
     * Get the current plan (non-reactive).
     */
    fun getPlan(): AgentPlan? = _currentPlan.value
    
    /**
     * Create a new plan from a list of tasks.
     */
    fun createPlan(tasks: List<PlanTask>): AgentPlan {
        val plan = AgentPlan.create(tasks)
        _currentPlan.value = plan
        notifyPlanCreated(plan)
        return plan
    }
    
    /**
     * Create a new plan from markdown content.
     */
    fun createPlanFromMarkdown(markdown: String): AgentPlan {
        val tasks = MarkdownPlanParser.parse(markdown)
        return createPlan(tasks)
    }
    
    /**
     * Set the current plan directly.
     */
    fun setPlan(plan: AgentPlan) {
        _currentPlan.value = plan
        notifyPlanCreated(plan)
    }
    
    /**
     * Update the current plan with new tasks.
     */
    fun updatePlan(tasks: List<PlanTask>) {
        val plan = _currentPlan.value
        if (plan != null) {
            plan.tasks.clear()
            plan.tasks.addAll(tasks)
            // Force StateFlow emission by reassigning the value
            _currentPlan.value = plan
            notifyPlanUpdated(plan)
        } else {
            createPlan(tasks)
        }
    }
    
    /**
     * Update the current plan from markdown content.
     */
    fun updatePlanFromMarkdown(markdown: String) {
        val tasks = MarkdownPlanParser.parse(markdown)
        updatePlan(tasks)
    }
    
    /**
     * Add a task to the current plan.
     */
    fun addTask(task: PlanTask) {
        val plan = _currentPlan.value ?: createPlan(emptyList())
        plan.addTask(task)
        // Force StateFlow emission by reassigning the value
        _currentPlan.value = plan
        notifyPlanUpdated(plan)
    }

    /**
     * Update a task's status.
     */
    fun updateTaskStatus(taskId: String, status: TaskStatus) {
        val plan = _currentPlan.value ?: return
        val task = plan.getTask(taskId) ?: return
        task.updateStatus(status)
        // Force StateFlow emission by reassigning the value
        _currentPlan.value = plan
        notifyTaskUpdated(task)
    }

    /**
     * Complete a step within a task.
     */
    fun completeStep(taskId: String, stepId: String) {
        val plan = _currentPlan.value ?: return
        plan.completeStep(taskId, stepId)
        // Force StateFlow emission by reassigning the value
        _currentPlan.value = plan
        notifyStepCompleted(taskId, stepId)
    }

    /**
     * Update a step's status.
     */
    fun updateStepStatus(taskId: String, stepId: String, status: TaskStatus) {
        val plan = _currentPlan.value ?: return
        val task = plan.getTask(taskId) ?: return
        task.updateStepStatus(stepId, status)
        // Force StateFlow emission by reassigning the value
        _currentPlan.value = plan
        notifyTaskUpdated(task)
    }
    
    /**
     * Clear the current plan.
     */
    fun clearPlan() {
        _currentPlan.value = null
        notifyPlanCleared()
    }
    
    /**
     * Add a listener for plan updates.
     */
    fun addListener(listener: PlanUpdateListener) {
        synchronized(listenersLock) {
            listeners.add(listener)
        }
    }

    /**
     * Remove a listener.
     */
    fun removeListener(listener: PlanUpdateListener) {
        synchronized(listenersLock) {
            listeners.remove(listener)
        }
    }

    // Notification methods - copy list before iterating for thread safety
    private fun notifyPlanCreated(plan: AgentPlan) {
        val listenersCopy = synchronized(listenersLock) { listeners.toList() }
        listenersCopy.forEach { it.onPlanCreated(plan) }
    }

    private fun notifyPlanUpdated(plan: AgentPlan) {
        val listenersCopy = synchronized(listenersLock) { listeners.toList() }
        listenersCopy.forEach { it.onPlanUpdated(plan) }
    }

    private fun notifyTaskUpdated(task: PlanTask) {
        val listenersCopy = synchronized(listenersLock) { listeners.toList() }
        listenersCopy.forEach { it.onTaskUpdated(task) }
    }

    private fun notifyStepCompleted(taskId: String, stepId: String) {
        val listenersCopy = synchronized(listenersLock) { listeners.toList() }
        listenersCopy.forEach { it.onStepCompleted(taskId, stepId) }
    }

    private fun notifyPlanCleared() {
        val listenersCopy = synchronized(listenersLock) { listeners.toList() }
        listenersCopy.forEach { it.onPlanCleared() }
    }
}

