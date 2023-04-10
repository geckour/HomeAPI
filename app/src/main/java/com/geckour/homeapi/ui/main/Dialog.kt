package com.geckour.homeapi.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geckour.homeapi.api.model.EnvironmentalData
import com.geckour.homeapi.api.model.EnvironmentalLog
import com.geckour.homeapi.api.model.SoilHumidityLog
import com.geckour.homeapi.ui.Colors
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor

object Dialog {

    @Composable
    fun Environmental(environmentalData: EnvironmentalData, onClearData: () -> Unit, onHaptic: () -> Unit) {
        AlertDialog(
            onDismissRequest = { onClearData() },
            confirmButton = {
                Button(
                    onClick = {
                        onClearData()
                        onHaptic()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent, contentColor = MaterialTheme.colors.onBackground),
                    elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp)
                ) {
                    Text(text = "OK")
                }
            },
            title = {
                Text(
                    text = "📡 環境値",
                    fontSize = 24.sp,
                    color = MaterialTheme.colors.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            },
            text = {
                Text(
                    text = String.format(
                        "🌡 %.2f [℃]\n💧 %.2f [%%]\n🌪 %.2f [hPa]\n💡 %.2f [lux]",
                        environmentalData.temperature,
                        environmentalData.humidity,
                        environmentalData.pressure,
                        environmentalData.illuminance
                    ),
                    color = MaterialTheme.colors.onBackground,
                    fontSize = 18.sp,
                    lineHeight = 30.sp
                )
            },
            backgroundColor = Colors.TEAL900
        )
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun EnvironmentalLog(
        environmentalLogData: List<EnvironmentalLog>,
        soilHumidityLogData: List<SoilHumidityLog>,
        simpleDateFormat: SimpleDateFormat,
        onClearData: () -> Unit,
        onNewRange: (range: Range) -> Unit,
        onHaptic: () -> Unit,
        currentRange: Range
    ) {
        val dates = (environmentalLogData.map { it.date } + soilHumidityLogData.map { it.date })
        AlertDialog(
            onDismissRequest = { onClearData() },
            confirmButton = {
                Button(
                    onClick = {
                        onClearData()
                        onHaptic()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent, contentColor = MaterialTheme.colors.onBackground),
                    elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp)
                ) {
                    Text(text = "OK")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val start = dates.minOrNull() ?: return@AlertDialog
                    val end = dates.maxOrNull() ?: return@AlertDialog

                    var isTemperatureEnabled by remember { mutableStateOf(true) }
                    var isHumidityEnabled by remember { mutableStateOf(true) }
                    var isPressureEnabled by remember { mutableStateOf(true) }
                    var isSoilHumidityEnabled by remember { mutableStateOf(true) }

                    Text(
                        text = "📈 グラフ",
                        fontSize = 24.sp,
                        color = MaterialTheme.colors.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            modifier = Modifier.padding(end = 8.dp),
                            onClick = {
                                onNewRange(Range.MONTH)
                                onHaptic()
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (currentRange == Range.MONTH) Colors.TEAL200 else Colors.TAR_BLACK
                            )
                        ) {
                            Text(text = "1M")
                        }
                        Button(
                            modifier = Modifier.padding(end = 8.dp),
                            onClick = {
                                onNewRange(Range.WEEK)
                                onHaptic()
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (currentRange == Range.WEEK) Colors.TEAL200 else Colors.TAR_BLACK
                            )
                        ) {
                            Text(text = "1w")
                        }
                        Button(
                            onClick = {
                                onNewRange(Range.DAY)
                                onHaptic()
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (currentRange == Range.DAY) Colors.TEAL200 else Colors.TAR_BLACK
                            )
                        ) {
                            Text(text = "1d")
                        }
                    }

                    Row(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            Modifier
                                .padding(end = 8.dp)
                                .background(
                                    shape = CircleShape,
                                    color = if (isTemperatureEnabled) Color.Yellow else Color.Yellow.copy(alpha = 0.4f)
                                )
                                .size(24.dp)
                                .clickable(interactionSource = MutableInteractionSource(), indication = rememberRipple()) {
                                    isTemperatureEnabled = isTemperatureEnabled.not()
                                }
                        )
                        Box(
                            Modifier
                                .padding(end = 8.dp)
                                .background(
                                    shape = CircleShape,
                                    color = if (isHumidityEnabled) Color.Cyan else Color.Cyan.copy(alpha = 0.4f)
                                )
                                .size(24.dp)
                                .clickable(interactionSource = MutableInteractionSource(), indication = rememberRipple()) {
                                    isHumidityEnabled = isHumidityEnabled.not()
                                }
                        )
                        Box(
                            Modifier
                                .padding(end = 8.dp)
                                .background(
                                    shape = CircleShape,
                                    color = if (isPressureEnabled) Color.Green else Color.Green.copy(alpha = 0.4f)
                                )
                                .size(24.dp)
                                .clickable(interactionSource = MutableInteractionSource(), indication = rememberRipple()) {
                                    isPressureEnabled = isPressureEnabled.not()
                                }
                        )
                        Box(
                            Modifier
                                .background(
                                    shape = CircleShape,
                                    color = if (isSoilHumidityEnabled) Color(0xffff8000) else Color(0x66ff8000)
                                )
                                .size(24.dp)
                                .clickable(interactionSource = MutableInteractionSource(), indication = rememberRipple()) {
                                    isSoilHumidityEnabled = isSoilHumidityEnabled.not()
                                }
                        )
                    }

                    var selectedEnvironmentalLog by remember { mutableStateOf<EnvironmentalLog?>(null) }
                    var selectedSoilHumidityLog by remember { mutableStateOf<SoilHumidityLog?>(null) }

                    Row {
                        Card(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .alpha(if (selectedEnvironmentalLog == null) 0f else 1f)
                        ) {
                            Column(modifier = Modifier.padding(4.dp)) {
                                val dateString = selectedEnvironmentalLog?.date?.let { simpleDateFormat.format(it) }.orEmpty()
                                Text(
                                    text = dateString,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(text = "${selectedEnvironmentalLog?.temperature} ℃", fontSize = 10.sp, color = Color.Yellow)
                                Text(
                                    text = "${selectedEnvironmentalLog?.humidity} %",
                                    fontSize = 10.sp,
                                    color = Color.Cyan
                                )
                                Text(text = "${selectedEnvironmentalLog?.pressure} hPa", fontSize = 10.sp, color = Color.Green)
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Card(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .alpha(if (selectedSoilHumidityLog == null) 0f else 1f)
                        ) {
                            Column(modifier = Modifier.padding(4.dp)) {
                                val dateString = selectedSoilHumidityLog?.date?.let { simpleDateFormat.format(it) }.orEmpty()
                                Text(
                                    text = dateString,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "${selectedSoilHumidityLog?.value} %",
                                    fontSize = 10.sp,
                                    color = Color(0xffff8000)
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.height(160.dp)) {
                        val maxTemperature = ceil(environmentalLogData.maxBy { it.temperature }.temperature / 10) * 10
                        val minTemperature = floor(environmentalLogData.minBy { it.temperature }.temperature / 10) * 10
                        val maxHumidity = ceil(environmentalLogData.maxBy { it.humidity }.humidity / 10) * 10
                        val minHumidity = floor(environmentalLogData.minBy { it.humidity }.humidity / 10) * 10
                        val maxPressure = ceil(environmentalLogData.maxBy { it.pressure }.pressure / 10) * 10
                        val minPressure = floor(environmentalLogData.minBy { it.pressure }.pressure / 10) * 10
                        val maxSoilHumidity = ceil(soilHumidityLogData.maxBy { it.value }.value / 10) * 10
                        val minSoilHumidity = floor(soilHumidityLogData.minBy { it.value }.value / 10) * 10
                        Column(modifier = Modifier.fillMaxHeight()) {
                            Text(text = "${maxTemperature.toInt()} ℃", color = Color.Yellow, fontSize = 10.sp)
                            Spacer(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                            )
                            Text(text = "${minTemperature.toInt()} ℃", color = Color.Yellow, fontSize = 10.sp)
                        }
                        Graph(
                            modifier = Modifier.weight(1f),
                            start = start,
                            end = end,
                            environmentalLogData = environmentalLogData,
                            soilHumidityLogData = soilHumidityLogData,
                            isTemperatureEnabled = isTemperatureEnabled,
                            isHumidityEnabled = isHumidityEnabled,
                            isPressureEnabled = isPressureEnabled,
                            isSoilHumidityEnabled = isSoilHumidityEnabled,
                            maxTemperature = maxTemperature,
                            minTemperature = minTemperature,
                            maxHumidity = maxHumidity,
                            minHumidity = minHumidity,
                            maxPressure = maxPressure,
                            minPressure = minPressure,
                            maxSoilHumidity = maxSoilHumidity,
                            minSoilHumidity = minSoilHumidity,
                            onUpdateSelectedEnvironmentalLog = { selectedEnvironmentalLog = it },
                            onUpdateSelectedSoilHumidityLog = { selectedSoilHumidityLog = it }
                        )
                        Column(modifier = Modifier.fillMaxHeight()) {
                            Text(text = "${maxHumidity.toInt()} %", color = Color.Cyan, fontSize = 10.sp)
                            Spacer(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                            )
                            Text(text = "${minHumidity.toInt()} %", color = Color.Cyan, fontSize = 10.sp)
                        }
                    }
                    Row(modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp)) {
                        val startText = simpleDateFormat.format(start).split(' ')
                        val endText = simpleDateFormat.format(end).split(' ')
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = startText[0], color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = startText[1], color = Color.White, fontSize = 10.sp)
                        }
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = endText[0], color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = endText[1], color = Color.White, fontSize = 10.sp)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(modifier = Modifier.padding(end = 4.dp)) {
                            Text(text = "🔼", fontSize = 10.sp)
                            Text(text = "🔽", fontSize = 10.sp)
                        }
                        Column(modifier = Modifier.padding(end = 8.dp)) {
                            Text(text = "${environmentalLogData.maxBy { it.temperature }.temperature} ℃", fontSize = 10.sp, color = Color.Yellow)
                            Text(text = "${environmentalLogData.minBy { it.temperature }.temperature} ℃", fontSize = 10.sp, color = Color.Yellow)
                        }
                        Column(modifier = Modifier.padding(end = 8.dp)) {
                            Text(text = "${environmentalLogData.maxBy { it.humidity }.humidity} %", fontSize = 10.sp, color = Color.Cyan)
                            Text(text = "${environmentalLogData.minBy { it.humidity }.humidity} %", fontSize = 10.sp, color = Color.Cyan)
                        }
                        Column(modifier = Modifier.padding(end = 8.dp)) {
                            Text(text = "${environmentalLogData.maxBy { it.pressure }.pressure} hPa", fontSize = 10.sp, color = Color.Green)
                            Text(text = "${environmentalLogData.minBy { it.pressure }.pressure} hPa", fontSize = 10.sp, color = Color.Green)
                        }
                        Column {
                            Text(text = "${soilHumidityLogData.maxBy { it.value }.value} %", fontSize = 10.sp, color = Color(0xffff8000))
                            Text(text = "${soilHumidityLogData.minBy { it.value }.value} %", fontSize = 10.sp, color = Color(0xffff8000))
                        }
                    }
                }
            },
            backgroundColor = Colors.TEAL900
        )
    }

    enum class Range(val duration: Long) {
        DAY(86400),
        WEEK(604800),
        MONTH(18144000)
    }
}