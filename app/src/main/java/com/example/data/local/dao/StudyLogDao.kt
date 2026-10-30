package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.model.StudySessionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyLogDao {

    @Query("SELECT * FROM study_session_logs ORDER BY completedTimestamp DESC")
    fun getAllLogs(): Flow<List<StudySessionLog>>

    @Query("SELECT * FROM study_session_logs WHERE completedTimestamp >= :startOfDay ORDER BY completedTimestamp DESC")
    fun getTodayLogs(startOfDay: Long): Flow<List<StudySessionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: StudySessionLog): Long

    @Query("DELETE FROM study_session_logs")
    suspend fun clearAllLogs()
}
