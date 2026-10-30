package com.example.data.repository

import com.example.data.backup.FlowStudyBackupData
import com.example.data.local.dao.RoutineDao
import com.example.data.local.dao.RoutineTaskDao
import com.example.data.local.dao.SettingsDao
import com.example.data.local.dao.StudyLogDao
import com.example.data.local.model.AppSettings
import com.example.data.local.model.RoutineEntity
import com.example.data.local.model.RoutineTask
import com.example.data.local.model.StudySessionLog
import com.example.data.local.model.TaskCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Calendar

class FlowStudyRepository(
    private val routineDao: RoutineDao,
    private val taskDao: RoutineTaskDao,
    private val logDao: StudyLogDao,
    private val settingsDao: SettingsDao
) {
    val allRoutines: Flow<List<RoutineEntity>> = routineDao.getAllRoutines()

    val activeRoutine: Flow<RoutineEntity?> = routineDao.getActiveRoutine()

    val allTasks: Flow<List<RoutineTask>> = routineDao.getActiveRoutine().flatMapLatest { routine ->
        if (routine != null) {
            taskDao.getTasksForRoutine(routine.id)
        } else {
            flowOf(emptyList())
        }
    }

    val allLogs: Flow<List<StudySessionLog>> = logDao.getAllLogs()

    val settings: Flow<AppSettings> = settingsDao.getSettings().map {
        it ?: AppSettings()
    }

    fun getTodayLogs(): Flow<List<StudySessionLog>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return logDao.getTodayLogs(calendar.timeInMillis)
    }

    suspend fun getTaskById(id: Long): RoutineTask? {
        return taskDao.getTaskById(id)
    }

    suspend fun insertTask(task: RoutineTask): Long {
        val active = routineDao.getActiveRoutineSync()
        val routineId = if (task.routineId > 0) task.routineId else (active?.id ?: 1L)
        val nextOrder = if (task.taskOrder == 0) {
            (taskDao.getMaxTaskOrder(routineId) ?: 0) + 1
        } else {
            task.taskOrder
        }
        return taskDao.insertTask(task.copy(routineId = routineId, taskOrder = nextOrder))
    }

    suspend fun updateTask(task: RoutineTask) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: RoutineTask) {
        taskDao.deleteTask(task)
        normalizeTaskOrders(task.routineId)
    }

    suspend fun duplicateTask(task: RoutineTask) {
        val nextOrder = (taskDao.getMaxTaskOrder(task.routineId) ?: 0) + 1
        val duplicated = task.copy(
            id = 0L,
            title = "${task.title} (Copy)",
            taskOrder = nextOrder,
            isCompleted = false,
            accumulatedDurationSeconds = 0L,
            completedAt = null,
            createdAt = System.currentTimeMillis()
        )
        taskDao.insertTask(duplicated)
    }

    suspend fun moveTaskUp(task: RoutineTask) {
        val tasks = taskDao.getTasksForRoutine(task.routineId).first()
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index > 0) {
            val previous = tasks[index - 1]
            taskDao.updateTaskOrder(task.id, previous.taskOrder)
            taskDao.updateTaskOrder(previous.id, task.taskOrder)
        }
    }

    suspend fun moveTaskDown(task: RoutineTask) {
        val tasks = taskDao.getTasksForRoutine(task.routineId).first()
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1 && index < tasks.size - 1) {
            val next = tasks[index + 1]
            taskDao.updateTaskOrder(task.id, next.taskOrder)
            taskDao.updateTaskOrder(next.id, task.taskOrder)
        }
    }

    suspend fun toggleTaskCompleted(task: RoutineTask) {
        val newStatus = !task.isCompleted
        val now = if (newStatus) System.currentTimeMillis() else null
        val updated = task.copy(
            isCompleted = newStatus,
            completedAt = now
        )
        taskDao.updateTask(updated)

        if (newStatus && task.accumulatedDurationSeconds > 0) {
            val minSeconds = task.minDurationSeconds
            val extSeconds = (task.accumulatedDurationSeconds - minSeconds).coerceAtLeast(0L)
            logDao.insertLog(
                StudySessionLog(
                    taskId = task.id,
                    taskTitle = task.title,
                    category = task.category,
                    subject = task.subject,
                    durationSeconds = task.accumulatedDurationSeconds,
                    minRequiredSeconds = minSeconds,
                    extensionSeconds = extSeconds,
                    wasCompulsory = task.isCompulsory,
                    wasInterrupted = false,
                    notes = task.notes,
                    completedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun logCompletedSession(
        task: RoutineTask,
        actualDurationSeconds: Long,
        wasInterrupted: Boolean,
        distractionCount: Int = 0,
        notes: String = ""
    ) {
        val minSeconds = task.minDurationSeconds
        val extSeconds = (actualDurationSeconds - minSeconds).coerceAtLeast(0L)
        val isMinCompleted = actualDurationSeconds >= minSeconds

        val shouldMarkTaskCompleted = !wasInterrupted && (isMinCompleted || !task.isCompulsory)

        val updatedTask = task.copy(
            accumulatedDurationSeconds = actualDurationSeconds,
            isCompleted = if (shouldMarkTaskCompleted) true else task.isCompleted,
            completedAt = if (shouldMarkTaskCompleted) System.currentTimeMillis() else task.completedAt
        )
        taskDao.updateTask(updatedTask)

        if (actualDurationSeconds > 0) {
            logDao.insertLog(
                StudySessionLog(
                    taskId = task.id,
                    taskTitle = task.title,
                    category = task.category,
                    subject = task.subject,
                    durationSeconds = actualDurationSeconds,
                    minRequiredSeconds = minSeconds,
                    extensionSeconds = extSeconds,
                    wasCompulsory = task.isCompulsory,
                    wasInterrupted = wasInterrupted,
                    notes = notes.ifBlank { task.notes },
                    distractionCount = distractionCount,
                    completedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun carryForwardUnfinishedTasks() {
        val active = routineDao.getActiveRoutineSync() ?: return
        val tasks = taskDao.getTasksForRoutine(active.id).first()
        // Incomplete tasks retain accumulated time and are moved to the top or flagged
        tasks.filter { !it.isCompleted }.forEachIndexed { idx, task ->
            taskDao.updateTask(task.copy(taskOrder = idx + 1))
        }
    }

    suspend fun hasMealBeenCompletedToday(mealType: String): Boolean {
        if (mealType.isBlank()) return false
        val active = routineDao.getActiveRoutineSync() ?: return false
        return taskDao.countCompletedMealsOfType(active.id, mealType.lowercase()) > 0
    }

    suspend fun resetAllCompletions() {
        val active = routineDao.getActiveRoutineSync() ?: return
        taskDao.resetAllTaskCompletions(active.id)
    }

    suspend fun switchActiveRoutine(routineId: Long) {
        routineDao.setActiveRoutine(routineId)
    }

    suspend fun createNewRoutine(name: String, description: String = ""): Long {
        return routineDao.insertRoutine(
            RoutineEntity(
                name = name,
                description = description,
                isTemplate = false,
                isSampleData = false,
                isActive = false
            )
        )
    }

    suspend fun updateSettings(settings: AppSettings) {
        settingsDao.upsertSettings(settings)
    }

    suspend fun clearHistoryLogs() {
        logDao.clearAllLogs()
    }

    suspend fun restoreBackup(data: FlowStudyBackupData) {
        routineDao.clearAll()
        taskDao.clearAllTasks()
        logDao.clearAllLogs()

        data.routines.forEach { routineDao.insertRoutine(it) }
        data.tasks.forEach { taskDao.insertTask(it) }
        data.logs.forEach { logDao.insertLog(it) }

        if (data.routines.isNotEmpty()) {
            val firstId = data.routines.first().id
            routineDao.setActiveRoutine(firstId)
        }
    }

    suspend fun seedDefaultDataIfEmpty() {
        val existingRoutines = routineDao.getAllRoutines().first()
        if (existingRoutines.isEmpty()) {
            loadPrepopulatedSampleRoutine()
        }
        val existingSettings = settingsDao.getSettingsSync()
        if (existingSettings == null) {
            settingsDao.upsertSettings(AppSettings())
        }
    }

    suspend fun loadPrepopulatedSampleRoutine() {
        routineDao.clearAll()
        taskDao.clearAllTasks()

        val sampleRoutineId = routineDao.insertRoutine(
            RoutineEntity(
                name = "Student Competitive Prep",
                description = "Physics, Chemistry, CET MCQs & Flexible Nutrition (Sample Routine)",
                isTemplate = true,
                isSampleData = true,
                isActive = true
            )
        )

        val sampleTasks = listOf(
            RoutineTask(
                routineId = sampleRoutineId,
                title = "Physics: Wave Optics Theory",
                category = TaskCategory.STUDY,
                subject = "Physics",
                minDurationMinutes = 45,
                extensionTargetMinutes = 15,
                isCompulsory = true,
                isOptional = false,
                taskOrder = 1,
                notes = "Interference formulas, Young's double slit derivation, and sign conventions."
            ),
            RoutineTask(
                routineId = sampleRoutineId,
                title = "Physics: Numerical Problem Set",
                category = TaskCategory.STUDY,
                subject = "Physics",
                minDurationMinutes = 40,
                extensionTargetMinutes = 20,
                isCompulsory = true,
                isOptional = false,
                taskOrder = 2,
                notes = "Solve 20 high-yield problems on optics and diffraction."
            ),
            RoutineTask(
                routineId = sampleRoutineId,
                title = "Mindful Hydration & Eye Rest",
                category = TaskCategory.BREAK,
                minDurationMinutes = 10,
                extensionTargetMinutes = 5,
                isCompulsory = false,
                isOptional = true,
                taskOrder = 3,
                notes = "Look away from screens, drink water, stretch."
            ),
            RoutineTask(
                routineId = sampleRoutineId,
                title = "Chemistry: Organic Reaction Mechanisms",
                category = TaskCategory.STUDY,
                subject = "Chemistry",
                minDurationMinutes = 45,
                extensionTargetMinutes = 15,
                isCompulsory = true,
                isOptional = false,
                taskOrder = 4,
                notes = "Electrophilic aromatic substitution and nucleophilic addition pathways."
            ),
            RoutineTask(
                routineId = sampleRoutineId,
                title = "Balanced Nutritious Lunch",
                category = TaskCategory.MEAL,
                minDurationMinutes = 35,
                extensionTargetMinutes = 15,
                isCompulsory = false,
                isOptional = true,
                mealType = "lunch",
                taskOrder = 5,
                notes = "Healthy meal away from the study desk."
            ),
            RoutineTask(
                routineId = sampleRoutineId,
                title = "Chemistry: Questions & Conversions",
                category = TaskCategory.STUDY,
                subject = "Chemistry",
                minDurationMinutes = 40,
                extensionTargetMinutes = 15,
                isCompulsory = true,
                isOptional = false,
                taskOrder = 6,
                notes = "Reagent identification and step-by-step synthetic conversions."
            ),
            RoutineTask(
                routineId = sampleRoutineId,
                title = "CET / Entrance Mock MCQs",
                category = TaskCategory.STUDY,
                subject = "Test Prep",
                minDurationMinutes = 60,
                extensionTargetMinutes = 30,
                isCompulsory = true,
                isOptional = false,
                taskOrder = 7,
                notes = "Timed exam simulation for accuracy and speed."
            ),
            RoutineTask(
                routineId = sampleRoutineId,
                title = "Weak Topics Targeted Review",
                category = TaskCategory.STUDY,
                subject = "Revision",
                minDurationMinutes = 35,
                extensionTargetMinutes = 15,
                isCompulsory = true,
                isOptional = false,
                taskOrder = 8,
                notes = "Target questions answered incorrectly during morning sets."
            ),
            RoutineTask(
                routineId = sampleRoutineId,
                title = "Evening Walk & Posture Mobility",
                category = TaskCategory.EXERCISE,
                minDurationMinutes = 20,
                extensionTargetMinutes = 10,
                isCompulsory = false,
                isOptional = true,
                taskOrder = 9,
                notes = "Fresh air walk and lumbar spine mobility."
            ),
            RoutineTask(
                routineId = sampleRoutineId,
                title = "Active Recall & Flashcard Synthesis",
                category = TaskCategory.STUDY,
                subject = "Revision",
                minDurationMinutes = 30,
                extensionTargetMinutes = 15,
                isCompulsory = true,
                isOptional = false,
                taskOrder = 10,
                notes = "Anki / flashcard run on today's key definitions and formulas."
            )
        )
        taskDao.insertTasks(sampleTasks)
    }

    private suspend fun normalizeTaskOrders(routineId: Long) {
        val tasks = taskDao.getTasksForRoutine(routineId).first()
        tasks.forEachIndexed { index, routineTask ->
            if (routineTask.taskOrder != index + 1) {
                taskDao.updateTaskOrder(routineTask.id, index + 1)
            }
        }
    }
}
