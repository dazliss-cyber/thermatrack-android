
package com.dazliss.thermatrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private data class ActivityOption(
    val name: String,
    val emoji: String,
    val minutes: Int
)

private val activities = listOf(
    ActivityOption("Sauna", "🔥", 15),
    ActivityOption("Steam room", "💨", 10),
    ActivityOption("Hot tub", "♨️", 15),
    ActivityOption("Cold plunge", "❄️", 2)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ThermaTrackScreen()
            }
        }
    }
}

@Composable
private fun ThermaTrackScreen() {
    var selected by remember { mutableStateOf(activities[0]) }
    var remaining by remember { mutableIntStateOf(900) }
    var running by remember { mutableStateOf(false) }

    LaunchedEffect(running, remaining) {
        if (running && remaining > 0) {
            delay(1000)
            remaining--
        } else if (remaining == 0) {
            running = false
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "ThermaTrack",
                style = MaterialTheme.typography.headlineLarge
            )
            Text("Your wellness session timer")

            Spacer(modifier = Modifier.height(24.dp))
            Text("CHOOSE YOUR ACTIVITY")

            activities.forEach { activity ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    onClick = {
                        selected = activity
                        remaining = activity.minutes * 60
                        running = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(activity.emoji)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            activity.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text("${activity.minutes} min")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("TIME REMAINING")

            Text(
                "%02d:%02d".format(
                    remaining / 60,
                    remaining % 60
                ),
                style = MaterialTheme.typography.displayLarge
            )

            Button(
                onClick = { running = !running },
                enabled = remaining > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (running) "Pause" else "Start")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    running = false
                    remaining = selected.minutes * 60
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset")
            }
        }
    }
}

