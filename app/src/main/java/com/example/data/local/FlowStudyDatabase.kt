package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.RoutineDao
import com.example.data.local.dao.RoutineTaskDao
import com.example.data.local.dao.SettingsDao
import com.example.data.local.dao.StudyLogDao
import com.example.data.local.model.AppSettings
import com.example.data.local.model.RoutineEntity
import com.example.data.local.model.RoutineTask
import com.example.data.local.model.StudySessionLog

@Database(
    entities = [
        RoutineEntity::class,
        RoutineTask::class,
        StudySessionLog::class,
        AppSettings::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FlowStudyDatabase : RoomDatabase() {

    abstract fun routineDao(): RoutineDao
    abstract fun routineTaskDao(): RoutineTaskDao
    abstract fun studyLogDao(): StudyLogDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: FlowStudyDatabase? = null

        fun getDatabase(context: Context): FlowStudyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlowStudyDatabase::class.java,
                    "flow_study.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
