
package com.dazliss.thermatrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val context = LocalContext.current
    val preferences = remember {
        context.getSharedPreferences("thermatrack", 0)
    }

    var selected by remember { mutableStateOf(activities[0]) }
    var remaining by remember { mutableIntStateOf(900) }
    var running by remember { mutableStateOf(false) }
    var temperature by remember { mutableStateOf("") }
    var heartRate by remember { mutableStateOf("") }

    val history = remember {
        mutableStateListOf<String>().apply {
            val saved = preferences.getString("history", "") ?: ""
            if (saved.isNotEmpty()) addAll(saved.split("\n"))
        }
    }

    LaunchedEffect(running, remaining) {
        if (running && remaining > 0) {
            delay(1000)
            remaining--
        } else if (remaining == 0) {
            running = false
        }
    }

    fun saveSession() {
        val elapsed = selected.minutes * 60 - remaining
        val date = SimpleDateFormat(
            "dd MMM yyyy, HH:mm",
            Locale.getDefault()
        ).format(Date())

        val record = listOf(
            selected.name,
            date,
            temperature.ifBlank { "Not recorded" },
            heartRate.ifBlank { "Not recorded" },
            "${elapsed / 60}m ${elapsed % 60}s"
        ).joinToString("|")

        history.add(0, record)
        preferences.edit()
            .putString("history", history.joinToString("\n"))
            .apply()

        running = false
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "ThermaTrack",
                style = MaterialTheme.typography.headlineLarge
            )
            Text("Your personal wellness tracker")

            Spacer(Modifier.height(24.dp))
            Text("CHOOSE YOUR ACTIVITY")

            activities.forEach { activity ->
                Spacer(Modifier.height(8.dp))
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
                        Spacer(Modifier.width(12.dp))
                        Text(
                            activity.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.weight(1f))
                        Text("${activity.minutes} min")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
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

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    running = false
                    remaining = selected.minutes * 60
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset")
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text(
                "SESSION MEASUREMENTS",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = temperature,
                onValueChange = { temperature = it },
                label = { Text("Temperature (°C)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = heartRate,
                onValueChange = { heartRate = it },
                label = { Text("Heart rate (BPM)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { saveSession() },
                enabled = !running,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Finish & Save Session")
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text(
                "SESSION HISTORY",
                style = MaterialTheme.typography.titleLarge
            )

            if (history.isEmpty()) {
                Text("Your saved sessions will appear here.")
            } else {
                history.forEach { record ->
                    val parts = record.split("|", limit = 5)
                    if (parts.size == 5) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    "${parts[0]} - ${parts[4]}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(parts[1])
                                Text("Temperature: ${parts[2]} °C")
                                Text("Heart rate: ${parts[3]} BPM")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Heart rate and temperature are entered manually for now.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
