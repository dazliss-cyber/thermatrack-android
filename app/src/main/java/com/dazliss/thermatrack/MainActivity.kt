
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

private data class TemperatureReading(
    val temperature: Float,
    val time: String
)

private data class SessionRecord(
    val activity: String,
    val duration: Int,
    val temperatures: List<TemperatureReading>
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
    var sessionDurationMinutes by remember { mutableIntStateOf(15) }
var durationInput by remember { mutableStateOf("15") }
    var durationText by remember { mutableStateOf("15") }
    var durationSeconds by remember { mutableIntStateOf(900) }
    var remaining by remember { mutableIntStateOf(900) }
    var running by remember { mutableStateOf(false) }
    var sessionStarted by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    var temperatureText by remember { mutableStateOf("") }
    var temperatures by remember {
        mutableStateOf(listOf<TemperatureReading>())
    }

    var sessionHistory by remember {
        mutableStateOf(listOf<SessionRecord>())
    }

    LaunchedEffect(running) {
        while (running && remaining > 0) {
            delay(1000)
            remaining--
        }

if (remaining == 0 && running) {
    running = false
    sessionStarted = false
    finished = true

    val elapsed = durationSeconds - remaining
    sessionHistory = sessionHistory + SessionRecord(
        activity = selected.name,
        duration = elapsed,
        temperatures = temperatures
    )
}

    fun resetSession() {
        running = false
        sessionStarted = false
        finished = false
        remaining = durationSeconds
        temperatures = emptyList()
        temperatureText = ""
    }

    fun finishSession() {
        running = false
        sessionStarted = false
        finished = true
        
val elapsed = durationSeconds - remaining
        sessionHistory = sessionHistory + SessionRecord(
            activity = selected.name,
            duration = elapsed,
            temperatures = temperatures
        )
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "ThermaTrack",
                style = MaterialTheme.typography.headlineLarge
            )

            Text("Your wellness session tracker")

            Spacer(modifier = Modifier.height(24.dp))

            Text("CHOOSE YOUR ACTIVITY")

            activities.forEach { activity ->
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    onClick = {
                        if (!running && !sessionStarted) {
                            selected = activity
                            sessionDurationMinutes = activity.minutes
                            durationText = activity.minutes.toString()
                            durationSeconds = activity.minutes * 60
                            resetSession()
                        }
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

            Spacer(modifier = Modifier.height(20.dp))

            Text("SESSION LENGTH")

            OutlinedTextField(
                value = durationText,
                onValueChange = { value ->
                    if (value.all { it.isDigit() } && value.length <= 3) {
                        durationText = value

                        val minutes = value.toIntOrNull()
                        if (minutes != null && minutes in 1..180) {
                            durationSeconds = minutes * 60
                            if (!sessionStarted) {
                                remaining = durationSeconds
                            }
                        }
                    }
                },
                label = { Text("Minutes") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                enabled = !running,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("TEST - NEW TIMER SCREEN")

            Text(
                "%02d:%02d".format(
                    remaining / 60,
                    remaining % 60
                ),
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(12.dp))
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
) {
    OutlinedButton(
        onClick = {
            durationSeconds = (durationSeconds - 60).coerceAtLeast(60)
            remaining = durationSeconds
        },
        modifier = Modifier.weight(1f)
    ) {
        Text("- 1 min")
    }

    OutlinedButton(
        onClick = {
            durationSeconds += 60
            remaining = durationSeconds
        },
        modifier = Modifier.weight(1f)
    ) {
        Text("+ 1 min")
    }
}
            Button(
                onClick = {
                    if (!running && remaining > 0) {
                        running = true
                        sessionStarted = true
                        finished = false
                    } else {
                        running = false
                    }
                },
                enabled = remaining > 0 && !finished,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        running -> "Pause"
                        sessionStarted -> "Resume"
                        else -> "Start Timer"
                    }
                )
            }

            
OutlinedButton(
    onClick = { resetSession() },
    modifier = Modifier.fillMaxWidth()
) {
    Text("Reset")
}

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 24.dp)
            )

            Text(
                "TEMPERATURE RECORDING",
                style = MaterialTheme.typography.titleLarge
            )

            Text("Record the temperature in °C throughout your session.")

            OutlinedTextField(
                value = temperatureText,
                onValueChange = { temperatureText = it },
                label = { Text("Temperature (°C)") },
                placeholder = { Text("e.g. 85.5") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true
            )

            Button(
                onClick = {
                    val value = temperatureText.toFloatOrNull()

                    if (value != null && value in -20f..150f) {
                        val time = SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                        ).format(Date())

                        temperatures = temperatures + TemperatureReading(
                            value,
                            time
                        )

                        temperatureText = ""
                    }
                },
                enabled = temperatureText.toFloatOrNull() != null &&
                    !finished,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Temperature Reading")
            }

            temperatures.forEach { reading ->
                Text(
                    "${reading.temperature}°C  -  ${reading.time}",
                    modifier = Modifier.padding(6.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 24.dp)
            )

            Text(
                "HEART RATE",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                "-- BPM",
                style = MaterialTheme.typography.headlineMedium
            )

            Text("Pixel Watch connection not yet enabled")

            if (finished) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 24.dp)
                )

                Text(
                    "SESSION COMPLETE",
                    style = MaterialTheme.typography.titleLarge
                )

                Text("Activity: ${selected.name}")
                Text(
                    "Duration: ${
                        (durationSeconds - remaining) / 60
                    } min ${
                        (durationSeconds - remaining) % 60
                    } sec"
                )

                Text("Temperature readings: ${temperatures.size}")
            }

            if (sessionHistory.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 24.dp)
                )

                Text(
                    "SESSION HISTORY",
                    style = MaterialTheme.typography.titleLarge
                )

                sessionHistory.forEach { record ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                record.activity,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                "Duration: ${record.duration / 60} min " +
                                    "${record.duration % 60} sec"
                            )

                            Text(
                                "Temperature readings: " +
                                    "${record.temperatures.size}"
                            )

                            record.temperatures.forEach { reading ->
                                Text(
                                    "${reading.temperature}°C at ${reading.time}"
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
