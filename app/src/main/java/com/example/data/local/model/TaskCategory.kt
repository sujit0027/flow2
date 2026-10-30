package com.example.data.local.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CategoryBreak
import com.example.ui.theme.CategoryBreakContainer
import com.example.ui.theme.CategoryExercise
import com.example.ui.theme.CategoryExerciseContainer
import com.example.ui.theme.CategoryMeal
import com.example.ui.theme.CategoryMealContainer
import com.example.ui.theme.CategoryPersonal
import com.example.ui.theme.CategoryPersonalContainer
import com.example.ui.theme.CategoryStudy
import com.example.ui.theme.CategoryStudyContainer
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldContainer

enum class TaskCategory(
    val displayName: String,
    val defaultIsCompulsory: Boolean,
    val defaultIsOptional: Boolean,
    val defaultDurationMinutes: Int
) {
    STUDY(
        displayName = "Study",
        defaultIsCompulsory = true,
        defaultIsOptional = false,
        defaultDurationMinutes = 45
    ),
    BREAK(
        displayName = "Break",
        defaultIsCompulsory = false,
        defaultIsOptional = true,
        defaultDurationMinutes = 10
    ),
    MEAL(
        displayName = "Meal",
        defaultIsCompulsory = false,
        defaultIsOptional = true,
        defaultDurationMinutes = 30
    ),
    EXERCISE(
        displayName = "Exercise",
        defaultIsCompulsory = false,
        defaultIsOptional = true,
        defaultDurationMinutes = 20
    ),
    PERSONAL(
        displayName = "Personal",
        defaultIsCompulsory = false,
        defaultIsOptional = true,
        defaultDurationMinutes = 15
    ),
    CUSTOM(
        displayName = "Custom",
        defaultIsCompulsory = false,
        defaultIsOptional = true,
        defaultDurationMinutes = 30
    );

    val accentColor: Color
        get() = when (this) {
            STUDY -> CategoryStudy
            BREAK -> CategoryBreak
            MEAL -> CategoryMeal
            EXERCISE -> CategoryExercise
            PERSONAL -> CategoryPersonal
            CUSTOM -> EmeraldLight
        }

    val containerColor: Color
        get() = when (this) {
            STUDY -> CategoryStudyContainer
            BREAK -> CategoryBreakContainer
            MEAL -> CategoryMealContainer
            EXERCISE -> CategoryExerciseContainer
            PERSONAL -> CategoryPersonalContainer
            CUSTOM -> EmeraldContainer
        }
}
