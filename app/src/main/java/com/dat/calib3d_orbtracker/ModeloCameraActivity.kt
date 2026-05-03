package com.dat.calib3d_orbtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

class ModeloCameraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // 1. STATE MANAGEMENT
                var focal by remember { mutableFloatStateOf(1201f) }
                var cx by remember { mutableFloatStateOf(960f) }
                var cy by remember { mutableFloatStateOf(462f) }
                var isDistorted by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text("Camera Model: Interactive Lab", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(16.dp))

                        // GROUPED FOCAL LENGTH
                        Text("Focal Length (fx/fy): ${focal.toInt()}")
                        Slider(value = focal, onValueChange = { focal = it }, valueRange = 500f..2500f)

                        // GROUPED PRINCIPAL POINT
                        Text("Principal Point (cx/cy): ${cx.toInt()}, ${cy.toInt()}")
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Split into two sliders or use one for each if preferred,
                            // but grouped together visually
                            Column(modifier = Modifier.weight(1f)) {
                                Slider(value = cx, onValueChange = { cx = it }, valueRange = 0f..1920f)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Slider(value = cy, onValueChange = { cy = it }, valueRange = 0f..1080f)
                            }
                        }

                        // K-MATRIX DISPLAY (Matches LookLikeThis.jpg layout)
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF).copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("K-Matrix:", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                                Text("[ $focal, 0, $cx ]", style = MaterialTheme.typography.bodyMedium)
                                Text("[ 0, $focal, $cy ]", style = MaterialTheme.typography.bodyMedium)
                                Text("[ 0, 0, 1 ]", style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        // THE VISUALIZATION BOX
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color(0xFFF9F9F9))
                        ) {
                            InteractiveGridView(focal, cx, cy, isDistorted)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // DYNAMIC BUTTON TEXT
                        Button(
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            onClick = { isDistorted = !isDistorted },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                        ) {
                            Text(if (isDistorted) "VIEW UNDISTORTED" else "VIEW DISTORTED (BARREL)")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun InteractiveGridView(f: Float, cx: Float, cy: Float, distort: Boolean) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            // Points chosen to represent typical feature locations across the frame
            val points = listOf(
                Offset(canvasW * 0.15f, cy - (f * 0.2f)),
                Offset(canvasW * 0.85f, cy - (f * 0.1f)),
                Offset(canvasW * 0.5f, cy),
                Offset(canvasW * 0.25f, cy + (f * 0.15f)),
                Offset(canvasW * 0.75f, cy + (f * 0.25f))
            )

            points.forEach { pt ->
                val path = Path()
                val steps = 40

                val start = if (distort) applyWarp(0f, pt.y, f, cx, cy) else Offset(0f, pt.y)
                path.moveTo(start.x, start.y)

                for (i in 1..steps) {
                    val xPos = (canvasW / steps) * i
                    val next = if (distort) applyWarp(xPos, pt.y, f, cx, cy) else Offset(xPos, pt.y)
                    path.lineTo(next.x, next.y)
                }

                drawPath(path = path, color = Color.Green, style = Stroke(width = 3f))

                val circlePos = if (distort) applyWarp(pt.x, pt.y, f, cx, cy) else pt
                drawCircle(color = Color.Red, radius = 10f, center = circlePos)
            }
        }
    }

    private fun applyWarp(x: Float, y: Float, f: Float, cx: Float, cy: Float): Offset {
        // k1 is negative for Barrel distortion (outward curve)
        val k1 = -0.0000007f
        val dx = x - cx
        val dy = y - cy
        val r2 = dx * dx + dy * dy
        val factor = 1 + k1 * r2

        return Offset(dx * factor + cx, dy * factor + cy)
    }
}