package com.dat.calib3d_orbtracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.opencv.android.OpenCVLoader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Load OpenCV
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(this, "OpenCV Load Failed", Toast.LENGTH_LONG).show()
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Camera Calibration Lab", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(20.dp))

                        var hasPermission by remember {
                            mutableStateOf(ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                        }

                        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission = it }

                        Button(onClick = {
                            if (hasPermission) {
                                startActivity(Intent(this@MainActivity, ModeloCameraActivity::class.java))
                            } else {
                                launcher.launch(Manifest.permission.CAMERA)
                            }
                        }) {
                            Text(if (hasPermission) "Launch Calibration" else "Grant Camera Permission")
                        }
                    }
                }
            }
        }
    }
}