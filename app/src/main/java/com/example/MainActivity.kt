package com.example

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ui.MainScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.FlowStudyTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var isInPip by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            FlowStudyTheme {
                MainScreen(
                    viewModel = viewModel,
                    isInPipMode = isInPip
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        if (intent == null) return
        if (intent.getBooleanExtra("OPEN_ACTIVE_TIMER", false)) {
            viewModel.openActiveTimerScreen()
        }
        if (intent.getBooleanExtra("START_NEXT_PENDING_TASK", false)) {
            val pending = viewModel.tasks.value.firstOrNull { !it.isCompleted }
            if (pending != null) {
                viewModel.startFocusSession(pending)
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPip = isInPictureInPictureMode
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // If focus timer is active and Android >= 8.0, seamlessly transition into Picture-in-Picture
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val snapshot = viewModel.timerSnapshot.value
            if (snapshot.taskId != 0L && snapshot.isRunning) {
                try {
                    val params = PictureInPictureParams.Builder()
                        .setAspectRatio(Rational(16, 9))
                        .build()
                    enterPictureInPictureMode(params)
                } catch (_: Exception) {}
            }
        }
    }
}
