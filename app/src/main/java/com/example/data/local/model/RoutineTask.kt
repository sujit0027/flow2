package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_tasks")
data class RoutineTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val routineId: Long = 1L,
    val title: String,
    val category: TaskCategory,
    val customCategoryName: String = "",
    val subject: String = "",
    val minDurationMinutes: Int,
    val extensionTargetMinutes: Int = 0,
    val isExtensionAllowed: Boolean = true,
    val isCompulsory: Boolean = true,
    val isOptional: Boolean = false,
    val canBeSkipped: Boolean = true,
    val taskOrder: Int = 0,
    val isCompleted: Boolean = false,
    val accumulatedDurationSeconds: Long = 0L,
    val notes: String = "",
    val completionSound: String = "chime",
    val mealType: String = "", // e.g. "breakfast", "lunch", "dinner", "snack"
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    val totalTargetMinutes: Int
        get() = minDurationMinutes + extensionTargetMinutes

    val minDurationSeconds: Long
        get() = minDurationMinutes * 60L

    val isMinDurationReached: Boolean
        get() = accumulatedDurationSeconds >= minDurationSeconds

    val remainingCompulsorySeconds: Long
        get() = (minDurationSeconds - accumulatedDurationSeconds).coerceAtLeast(0L)

    val extensionSeconds: Long
        get() = (accumulatedDurationSeconds - minDurationSeconds).coerceAtLeast(0L)

    val effectiveCategoryDisplayName: String
        get() = if (category == TaskCategory.CUSTOM && customCategoryName.isNotBlank()) {
            customCategoryName
        } else {
            category.displayName
        }
}
