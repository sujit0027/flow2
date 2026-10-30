package com.example.ui.wallpaper

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.ui.theme.DarkBackground

data class WallpaperOption(
    val id: String,
    val name: String,
    val category: String,
    val previewColor: Color
)

object WallpaperGallery {
    val options = listOf(
        WallpaperOption("AMOLED", "Pure AMOLED", "Solid", Color(0xFF000000)),
        WallpaperOption("FOREST", "Misty Forest", "Nature", Color(0xFF0A1F16)),
        WallpaperOption("MOUNTAIN", "Twilight Ridge", "Mountain", Color(0xFF0D1322)),
        WallpaperOption("SKY", "Midnight Starlight", "Sky", Color(0xFF0B0E1B)),
        WallpaperOption("COUNTRYSIDE", "Dusk Meadow", "Peaceful", Color(0xFF1E1711)),
        WallpaperOption("GRADIENT", "Emerald Flow", "Gradient", Color(0xFF0F261C)),
        WallpaperOption("CUSTOM", "Custom Photo", "Personal", Color(0xFF263238))
    )
}

@Composable
fun WallpaperBackground(
    wallpaperType: String,
    dimLevel: Float = 0.35f,
    customUri: String = "",
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (wallpaperType) {
            "AMOLED" -> {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black))
            }

            "FOREST" -> {
                ForestMistBackground()
            }

            "MOUNTAIN" -> {
                MountainRidgeBackground()
            }

            "SKY" -> {
                MidnightSkyBackground()
            }

            "COUNTRYSIDE" -> {
                DuskMeadowBackground()
            }

            "GRADIENT" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF061A12),
                                    Color(0xFF0B291D),
                                    Color(0xFF07120E)
                                )
                            )
                        )
                )
            }

            "CUSTOM" -> {
                if (customUri.isNotBlank()) {
                    AsyncImage(
                        model = Uri.parse(customUri),
                        contentDescription = "Custom Wallpaper",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(DarkBackground))
                }
            }

            else -> {
                Box(modifier = Modifier.fillMaxSize().background(DarkBackground))
            }
        }

        // Adjustable dim scrim to ensure text readability over any wallpaper
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = dimLevel.coerceIn(0f, 0.9f)))
        )
    }
}

@Composable
private fun ForestMistBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Atmospheric vertical gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF06150F),
                    Color(0xFF0C241B),
                    Color(0xFF07130E)
                )
            )
        )

        // Subtle misty tree silhouette layers
        val treeColorFar = Color(0x2210B981)
        val treeColorNear = Color(0x330B3824)

        // Distant pine peaks
        val pathFar = Path().apply {
            moveTo(0f, h * 0.75f)
            var x = 0f
            while (x < w) {
                val step = w / 12f
                val peakHeight = h * (0.68f + (kotlin.math.sin(x.toDouble()).toFloat() * 0.04f))
                lineTo(x + step * 0.5f, peakHeight)
                lineTo(x + step, h * 0.75f)
                x += step
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(pathFar, color = treeColorFar)

        // Near pine peaks
        val pathNear = Path().apply {
            moveTo(0f, h * 0.85f)
            var x = 0f
            while (x < w) {
                val step = w / 8f
                val peakHeight = h * (0.78f + (kotlin.math.cos(x.toDouble()).toFloat() * 0.03f))
                lineTo(x + step * 0.5f, peakHeight)
                lineTo(x + step, h * 0.85f)
                x += step
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(pathNear, color = treeColorNear)
    }
}

@Composable
private fun MountainRidgeBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0B1020),
                    Color(0xFF141933),
                    Color(0xFF090D1A)
                )
            )
        )

        // Dramatic mountain silhouette
        val mountainPath = Path().apply {
            moveTo(0f, h * 0.7f)
            lineTo(w * 0.25f, h * 0.55f)
            lineTo(w * 0.45f, h * 0.65f)
            lineTo(w * 0.75f, h * 0.52f)
            lineTo(w, h * 0.68f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            mountainPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x442A3B66), Color(0x660B1020)),
                startY = h * 0.52f,
                endY = h
            )
        )
    }
}

@Composable
private fun MidnightSkyBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF060914),
                    Color(0xFF0C1329),
                    Color(0xFF050811)
                )
            )
        )

        // Soft ambient star field
        val stars = listOf(
            Offset(w * 0.15f, h * 0.12f),
            Offset(w * 0.78f, h * 0.18f),
            Offset(w * 0.42f, h * 0.25f),
            Offset(w * 0.88f, h * 0.32f),
            Offset(w * 0.22f, h * 0.45f),
            Offset(w * 0.65f, h * 0.50f)
        )
        stars.forEach { pos ->
            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = 2.5f,
                center = pos
            )
        }
    }
}

@Composable
private fun DuskMeadowBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1A130C),
                    Color(0xFF261D13),
                    Color(0xFF110D08)
                )
            )
        )

        val meadowPath = Path().apply {
            moveTo(0f, h * 0.8f)
            cubicTo(w * 0.3f, h * 0.75f, w * 0.7f, h * 0.85f, w, h * 0.78f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(meadowPath, color = Color(0x33443322))
    }
}
