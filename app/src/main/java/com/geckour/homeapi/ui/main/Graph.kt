package com.geckour.homeapi.ui.main

import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geckour.homeapi.api.model.Co2Log
import com.geckour.homeapi.api.model.EnvironmentalLog
import com.geckour.homeapi.api.model.SoilHumidityLog
import com.geckour.homeapi.ui.Colors
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Graph(
    modifier: Modifier,
    start: Date,
    end: Date,
    environmentalLogData: List<EnvironmentalLog>,
    soilHumidityLogData: List<SoilHumidityLog>,
    co2LogData: List<Co2Log>,
    isTemperatureEnabled: Boolean,
    isHumidityEnabled: Boolean,
    isPressureEnabled: Boolean,
    isSoilHumidityEnabled: Boolean,
    isCo2Enabled: Boolean,
    maxTemperature: Float?,
    minTemperature: Float?,
    maxHumidity: Float?,
    minHumidity: Float?,
    maxPressure: Float?,
    minPressure: Float?,
    maxSoilHumidity: Float?,
    minSoilHumidity: Float?,
    maxCo2: Float?,
    minCo2: Float?,
    onUpdateSelectedEnvironmentalLog: (new: EnvironmentalLog?) -> Unit,
    onUpdateSelectedSoilHumidityLog: (new: SoilHumidityLog?) -> Unit,
    onUpdateSelectedCo2Log: (new: Co2Log?) -> Unit,
) {
    Box(modifier = modifier) {
        val density = LocalDensity.current
        var width by remember { mutableStateOf(0f) }
        var targetDate by remember { mutableStateOf<Date?>(null) }
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter { event ->
                    return@pointerInteropFilter when (event.action) {
                        MotionEvent.ACTION_DOWN,
                        MotionEvent.ACTION_POINTER_DOWN,
                        MotionEvent.ACTION_MOVE -> {
                            val pointedTime = (start.time + (end.time - start.time) * event.x / width).toLong()
                            val environmental = environmentalLogData.minByOrNull { abs(it.date.time - pointedTime) }
                            onUpdateSelectedEnvironmentalLog(environmental)
                            val environmentalDiff = environmental?.let { EnvironmentalLog::class to abs(it.date.time - pointedTime) }
                            val soilHumidity = soilHumidityLogData.minByOrNull { abs(it.date.time - pointedTime) }
                            onUpdateSelectedSoilHumidityLog(soilHumidity)
                            val soilHumidityDiff = soilHumidity?.let { SoilHumidityLog::class to abs(it.date.time - pointedTime) }
                            val co2 = co2LogData.minByOrNull { abs(it.date.time - pointedTime) }
                            onUpdateSelectedCo2Log(co2)
                            val co2Diff = co2?.let { Co2Log::class to abs(it.date.time - pointedTime) }
                            targetDate = when (listOfNotNull(environmentalDiff, soilHumidityDiff, co2Diff).minByOrNull { it.second }?.first) {
                                EnvironmentalLog::class -> environmental!!.date
                                SoilHumidityLog::class -> soilHumidity!!.date
                                Co2Log::class -> co2!!.date
                                null -> null
                                else -> throw IllegalStateException()
                            }
                            true
                        }

                        MotionEvent.ACTION_UP,
                        MotionEvent.ACTION_POINTER_UP -> {
                            onUpdateSelectedEnvironmentalLog(null)
                            onUpdateSelectedSoilHumidityLog(null)
                            onUpdateSelectedCo2Log(null)
                            targetDate = null
                            true
                        }

                        else -> false
                    }
                },
            onDraw = {
                width = size.width

                val graphPadding = with(density) {
                    RectF(8.dp.toPx(), 8.sp.toPx(), 8.dp.toPx(), 8.sp.toPx())
                }

                repeat(3) {
                    val plotS = getPlot(size, graphPadding, it.toFloat(), 3f, -1f, Date(0), Date(0), Date(1))
                    val plotE = getPlot(size, graphPadding, it.toFloat(), 3f, -1f, Date(1), Date(0), Date(1))
                    drawLine(Colors.PALE_WHITE.copy(alpha = 0.5f), Offset(plotS.x, plotS.y), Offset(plotE.x, plotE.y))
                }

                val temperaturePath = Path()
                val humidityPath = Path()
                val pressurePath = Path()
                environmentalLogData.forEachIndexed { index, log ->
                    if (isTemperatureEnabled && maxTemperature != null && minTemperature != null) {
                        val temperaturePlot =
                            getPlot(size, graphPadding, log.temperature, maxTemperature, minTemperature, log.date, start, end)
                        if (index == 0) {
                            temperaturePath.moveTo(temperaturePlot.x, temperaturePlot.y)
                        } else {
                            temperaturePath.lineTo(temperaturePlot.x, temperaturePlot.y)
                        }
                    }

                    if (isHumidityEnabled && maxHumidity != null && minHumidity != null) {
                        val humidityPlot =
                            getPlot(size, graphPadding, log.humidity, maxHumidity, minHumidity, log.date, start, end)
                        if (index == 0) {
                            humidityPath.moveTo(humidityPlot.x, humidityPlot.y)
                        } else {
                            humidityPath.lineTo(humidityPlot.x, humidityPlot.y)
                        }
                    }

                    if (isPressureEnabled && maxPressure != null && minPressure != null) {
                        val pressurePlot =
                            getPlot(size, graphPadding, log.pressure, maxPressure, minPressure, log.date, start, end)
                        if (index == 0) {
                            pressurePath.moveTo(pressurePlot.x, pressurePlot.y)
                        } else {
                            pressurePath.lineTo(pressurePlot.x, pressurePlot.y)
                        }
                    }
                }
                if (isTemperatureEnabled) {
                    drawPath(
                        path = temperaturePath,
                        color = Colors.TEMPERATURE,
                        style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
                if (isHumidityEnabled) {
                    drawPath(
                        path = humidityPath,
                        color = Colors.HUMIDITY,
                        style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
                if (isPressureEnabled) {
                    drawPath(
                        path = pressurePath,
                        color = Colors.PRESSURE,
                        style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                if (isSoilHumidityEnabled && maxSoilHumidity != null && minSoilHumidity != null) {
                    val soilHumidityPath = Path()
                    soilHumidityLogData.forEachIndexed { index, log ->
                        val soilHumidityPlot =
                            getPlot(size, graphPadding, log.value, maxSoilHumidity, minSoilHumidity, log.date, start, end)
                        if (index == 0) {
                            soilHumidityPath.moveTo(soilHumidityPlot.x, soilHumidityPlot.y)
                        } else {
                            soilHumidityPath.lineTo(soilHumidityPlot.x, soilHumidityPlot.y)
                        }
                    }
                    drawPath(
                        path = soilHumidityPath,
                        color = Colors.SOIL_HUMIDITY,
                        style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                if (isCo2Enabled && maxCo2 != null && minCo2 != null) {
                    val co2Path = Path()
                    co2LogData.forEachIndexed { index, log ->
                        val co2Plot =
                            getPlot(size, graphPadding, log.value.toFloat(), maxCo2, minCo2, log.date, start, end)
                        if (index == 0) {
                            co2Path.moveTo(co2Plot.x, co2Plot.y)
                        } else {
                            co2Path.lineTo(co2Plot.x, co2Plot.y)
                        }
                    }
                    drawPath(
                        path = co2Path,
                        color = Colors.CO2,
                        style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                targetDate?.let {
                    val top = getPlot(size, graphPadding, 1f, 0f, 1f, it, start, end)
                    val bottom = getPlot(size, graphPadding, 0f, 0f, 1f, it, start, end)
                    drawLine(Color.White, Offset(top.x, top.y), Offset(bottom.x, bottom.y))
                }
                drawLine(
                    color = Color.White,
                    start = Offset(graphPadding.left, graphPadding.top),
                    end = Offset(size.width - graphPadding.right, graphPadding.top),
                    strokeWidth = 1.5f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(graphPadding.left, size.height - graphPadding.bottom),
                    end = Offset(size.width - graphPadding.right, size.height - graphPadding.bottom),
                    strokeWidth = 1.5f,
                    cap = StrokeCap.Round
                )
            }
        )
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (maxHumidity != null && minHumidity != null) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${maxHumidity.toInt()} %",
                        color = Colors.HUMIDITY,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .background(color = Colors.TEAL900)
                            .padding(horizontal = 4.dp)
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                    )
                    Text(
                        text = "${minHumidity.toInt()} %",
                        color = Colors.HUMIDITY,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .background(color = Colors.TEAL900)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
            if (maxPressure != null && minPressure != null) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${maxPressure.toInt()} hPa",
                        color = Colors.PRESSURE,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .background(color = Colors.TEAL900)
                            .padding(horizontal = 4.dp)
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                    )
                    Text(
                        text = "${minPressure.toInt()} hPa",
                        color = Colors.PRESSURE,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .background(color = Colors.TEAL900)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
            if (maxSoilHumidity != null && minSoilHumidity != null) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${maxSoilHumidity.toInt()} %",
                        color = Colors.SOIL_HUMIDITY,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .background(color = Colors.TEAL900)
                            .padding(horizontal = 4.dp)
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                    )
                    Text(
                        text = "${minSoilHumidity.toInt()} %",
                        color = Colors.SOIL_HUMIDITY,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .background(color = Colors.TEAL900)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

private fun getPlot(canvasSize: Size, padding: RectF, value: Float, maxValue: Float, minValue: Float, time: Date, start: Date, end: Date): PointF {
    val paddingLeft = padding.left
    val paddingBottom = padding.bottom
    val width = canvasSize.width - (paddingLeft + padding.right)
    val height = canvasSize.height - (padding.top + paddingBottom)
    val timeRange = end.time - start.time
    val timeOffset = time.time - start.time
    val valueRange = maxValue - minValue
    val valueOffset = maxValue - value

    return PointF(paddingLeft + width * timeOffset / timeRange, paddingBottom + height * valueOffset / valueRange)
}