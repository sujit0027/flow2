package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.model.RoutineTask
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineTaskDao {

    @Query("SELECT * FROM routine_tasks WHERE routineId = :routineId ORDER BY taskOrder ASC, id ASC")
    fun getTasksForRoutine(routineId: Long): Flow<List<RoutineTask>>

    @Query("SELECT * FROM routine_tasks ORDER BY taskOrder ASC, id ASC")
    fun getAllTasks(): Flow<List<RoutineTask>>

    @Query("SELECT * FROM routine_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): RoutineTask?

    @Query("SELECT * FROM routine_tasks WHERE id = :id LIMIT 1")
    fun observeTaskById(id: Long): Flow<RoutineTask?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: RoutineTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<RoutineTask>)

    @Update
    suspend fun updateTask(task: RoutineTask)

    @Delete
    suspend fun deleteTask(task: RoutineTask)

    @Query("DELETE FROM routine_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("DELETE FROM routine_tasks WHERE routineId = :routineId")
    suspend fun clearTasksForRoutine(routineId: Long)

    @Query("DELETE FROM routine_tasks")
    suspend fun clearAllTasks()

    @Query("UPDATE routine_tasks SET taskOrder = :newOrder WHERE id = :id")
    suspend fun updateTaskOrder(id: Long, newOrder: Int)

    @Query("UPDATE routine_tasks SET isCompleted = 0, accumulatedDurationSeconds = 0, completedAt = NULL WHERE routineId = :routineId")
    suspend fun resetAllTaskCompletions(routineId: Long)

    @Query("SELECT MAX(taskOrder) FROM routine_tasks WHERE routineId = :routineId")
    suspend fun getMaxTaskOrder(routineId: Long): Int?

    // Check if meal of type was completed in routine
    @Query("SELECT COUNT(*) FROM routine_tasks WHERE routineId = :routineId AND mealType = :mealType AND isCompleted = 1")
    suspend fun countCompletedMealsOfType(routineId: Long, mealType: String): Int
}
