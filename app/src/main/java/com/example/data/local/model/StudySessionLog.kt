package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_session_logs")
data class StudySessionLog(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Long = 0L,
    val taskId: Long,
    val taskTitle: String,
    val category: TaskCategory,
    val subject: String = "",
    val durationSeconds: Long,
    val minRequiredSeconds: Long,
    val extensionSeconds: Long,
    val wasCompulsory: Boolean,
    val wasInterrupted: Boolean = false,
    val notes: String = "",
    val distractionCount: Int = 0,
    val completedTimestamp: Long = System.currentTimeMillis()
)
