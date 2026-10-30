package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.local.model.TaskCategory

class Converters {
    @TypeConverter
    fun fromCategory(category: TaskCategory?): String {
        return category?.name ?: TaskCategory.STUDY.name
    }

    @TypeConverter
    fun toCategory(value: String?): TaskCategory {
        return if (value != null) {
            try {
                TaskCategory.valueOf(value)
            } catch (e: IllegalArgumentException) {
                TaskCategory.STUDY
            }
        } else {
            TaskCategory.STUDY
        }
    }
}
