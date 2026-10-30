package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val autoAdvanceToNextTask: Boolean = false,
    val vibrateOnComplete: Boolean = true,
    val keepScreenOn: Boolean = true,
    val breaksOptionalDefault: Boolean = true,
    val defaultStudyMinDuration: Int = 30,
    val enforceCompulsoryWarning: Boolean = true
)
