package com.example.data.backup

import com.example.data.local.model.RoutineEntity
import com.example.data.local.model.RoutineTask
import com.example.data.local.model.StudySessionLog
import com.example.data.local.model.TaskCategory
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FlowStudyBackupData(
    val exportTimestamp: Long,
    val appVersion: String = "1.0",
    val routines: List<RoutineEntity>,
    val tasks: List<RoutineTask>,
    val logs: List<StudySessionLog>
)

object DataBackupManager {

    fun exportToJson(
        routines: List<RoutineEntity>,
        tasks: List<RoutineTask>,
        logs: List<StudySessionLog>
    ): String {
        val root = JSONObject()
        root.put("app", "FlowStudy")
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put("version", "1.0")

        // Routines array
        val routinesArray = JSONArray()
        routines.forEach { r ->
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("name", r.name)
            obj.put("description", r.description)
            obj.put("isTemplate", r.isTemplate)
            obj.put("isSampleData", r.isSampleData)
            obj.put("isActive", r.isActive)
            routinesArray.put(obj)
        }
        root.put("routines", routinesArray)

        // Tasks array
        val tasksArray = JSONArray()
        tasks.forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("routineId", t.routineId)
            obj.put("title", t.title)
            obj.put("category", t.category.name)
            obj.put("subject", t.subject)
            obj.put("minDurationMinutes", t.minDurationMinutes)
            obj.put("extensionTargetMinutes", t.extensionTargetMinutes)
            obj.put("isCompulsory", t.isCompulsory)
            obj.put("isOptional", t.isOptional)
            obj.put("canBeSkipped", t.canBeSkipped)
            obj.put("taskOrder", t.taskOrder)
            obj.put("isCompleted", t.isCompleted)
            obj.put("accumulatedDurationSeconds", t.accumulatedDurationSeconds)
            obj.put("notes", t.notes)
            obj.put("mealType", t.mealType)
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        // Logs array
        val logsArray = JSONArray()
        logs.forEach { l ->
            val obj = JSONObject()
            obj.put("sessionId", l.sessionId)
            obj.put("taskId", l.taskId)
            obj.put("taskTitle", l.taskTitle)
            obj.put("category", l.category.name)
            obj.put("subject", l.subject)
            obj.put("durationSeconds", l.durationSeconds)
            obj.put("minRequiredSeconds", l.minRequiredSeconds)
            obj.put("extensionSeconds", l.extensionSeconds)
            obj.put("wasCompulsory", l.wasCompulsory)
            obj.put("wasInterrupted", l.wasInterrupted)
            obj.put("notes", l.notes)
            obj.put("completedTimestamp", l.completedTimestamp)
            logsArray.put(obj)
        }
        root.put("logs", logsArray)

        return root.toString(2)
    }

    fun exportToCsv(logs: List<StudySessionLog>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("Timestamp,TaskTitle,Category,Subject,DurationMinutes,MinRequiredMinutes,ExtensionMinutes,WasCompulsory,WasInterrupted,Notes\n")
        logs.forEach { log ->
            val formattedDate = dateFormat.format(Date(log.completedTimestamp))
            val safeTitle = log.taskTitle.replace(",", " ")
            val safeSubject = log.subject.replace(",", " ")
            val safeNotes = log.notes.replace(",", " ").replace("\n", " ")
            sb.append("$formattedDate,$safeTitle,${log.category.name},$safeSubject,${log.durationSeconds / 60},${log.minRequiredSeconds / 60},${log.extensionSeconds / 60},${log.wasCompulsory},${log.wasInterrupted},\"$safeNotes\"\n")
        }
        return sb.toString()
    }

    fun parseFromJson(jsonString: String): FlowStudyBackupData? {
        return try {
            val root = JSONObject(jsonString)
            val timestamp = root.optLong("exportTimestamp", System.currentTimeMillis())

            val routines = mutableListOf<RoutineEntity>()
            val routinesArray = root.optJSONArray("routines")
            if (routinesArray != null) {
                for (i in 0 until routinesArray.length()) {
                    val obj = routinesArray.getJSONObject(i)
                    routines.add(
                        RoutineEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            description = obj.optString("description", ""),
                            isTemplate = obj.optBoolean("isTemplate", false),
                            isSampleData = obj.optBoolean("isSampleData", false),
                            isActive = obj.optBoolean("isActive", false)
                        )
                    )
                }
            }

            val tasks = mutableListOf<RoutineTask>()
            val tasksArray = root.optJSONArray("tasks")
            if (tasksArray != null) {
                for (i in 0 until tasksArray.length()) {
                    val obj = tasksArray.getJSONObject(i)
                    val catName = obj.optString("category", TaskCategory.STUDY.name)
                    val cat = try { TaskCategory.valueOf(catName) } catch (e: Exception) { TaskCategory.STUDY }

                    tasks.add(
                        RoutineTask(
                            id = obj.optLong("id", 0L),
                            routineId = obj.optLong("routineId", 1L),
                            title = obj.getString("title"),
                            category = cat,
                            subject = obj.optString("subject", ""),
                            minDurationMinutes = obj.optInt("minDurationMinutes", 30),
                            extensionTargetMinutes = obj.optInt("extensionTargetMinutes", 15),
                            isCompulsory = obj.optBoolean("isCompulsory", true),
                            isOptional = obj.optBoolean("isOptional", false),
                            canBeSkipped = obj.optBoolean("canBeSkipped", true),
                            taskOrder = obj.optInt("taskOrder", i + 1),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            accumulatedDurationSeconds = obj.optLong("accumulatedDurationSeconds", 0L),
                            notes = obj.optString("notes", ""),
                            mealType = obj.optString("mealType", "")
                        )
                    )
                }
            }

            val logs = mutableListOf<StudySessionLog>()
            val logsArray = root.optJSONArray("logs")
            if (logsArray != null) {
                for (i in 0 until logsArray.length()) {
                    val obj = logsArray.getJSONObject(i)
                    val catName = obj.optString("category", TaskCategory.STUDY.name)
                    val cat = try { TaskCategory.valueOf(catName) } catch (e: Exception) { TaskCategory.STUDY }

                    logs.add(
                        StudySessionLog(
                            sessionId = obj.optLong("sessionId", 0L),
                            taskId = obj.optLong("taskId", 0L),
                            taskTitle = obj.optString("taskTitle", "Task"),
                            category = cat,
                            subject = obj.optString("subject", ""),
                            durationSeconds = obj.optLong("durationSeconds", 0L),
                            minRequiredSeconds = obj.optLong("minRequiredSeconds", 0L),
                            extensionSeconds = obj.optLong("extensionSeconds", 0L),
                            wasCompulsory = obj.optBoolean("wasCompulsory", true),
                            wasInterrupted = obj.optBoolean("wasInterrupted", false),
                            notes = obj.optString("notes", ""),
                            completedTimestamp = obj.optLong("completedTimestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            FlowStudyBackupData(
                exportTimestamp = timestamp,
                routines = routines,
                tasks = tasks,
                logs = logs
            )
        } catch (e: Exception) {
            null
        }
    }
}
