package com.geckour.homeapi.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.PointF
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Bottom
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.AlertDialog
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.geckour.homeapi.R
import com.geckour.homeapi.model.RequestData
import com.geckour.homeapi.ui.Colors
import com.geckour.homeapi.ui.DarkColors
import com.geckour.homeapi.ui.LightColors
import com.geckour.homeapi.ui.login.LoginActivity
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor

class MainActivity : AppCompatActivity() {

    companion object {

        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(colors = if (isSystemInDarkTheme()) DarkColors else LightColors) {
                Content()
                Loading()
                Error()

                EnvironmentalDialog()
                EnvironmentalLogDialog()
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun Content() {
        Column(verticalArrangement = Bottom, modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()

            Scaffold(
                topBar = {
                    TopAppBar(contentPadding = PaddingValues(horizontal = 16.dp)) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .pointerInteropFilter { event ->
                                    return@pointerInteropFilter when (event.action) {
                                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                                            viewModel.sendSignal(true)
                                            true
                                        }
                                        MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                                            viewModel.sendSignal(false)
                                            true
                                        }
                                        else -> false
                                    }
                                },
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            modifier = Modifier.padding(end = 8.dp),
                            onClick = { viewModel.setRoom(MainViewModel.Room.LIVING) },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (viewModel.data.room == MainViewModel.Room.LIVING) Colors.TEAL700 else Colors.TEAL900
                            )
                        ) {
                            Text(text = "居室")
                        }
                        Button(
                            onClick = { viewModel.setRoom(MainViewModel.Room.KITCHEN) },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (viewModel.data.room == MainViewModel.Room.KITCHEN) Colors.TEAL700 else Colors.TEAL900
                            )
                        ) {
                            Text(text = "台所")
                        }
                    }
                },
                bottomBar = {
                    Column {
                        Row(
                            modifier = Modifier
                                .padding(vertical = 20.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Environmental()
                            Spacer(modifier = Modifier.width(8.dp))
                            EnvironmentalLog()
                        }
                        BottomNavigation {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination

                            MainViewModel.Screen.values().forEach { screen ->
                                val selected = currentDestination?.hierarchy?.any { it.route == screen.title } == true
                                BottomNavigationItem(
                                    modifier = Modifier.background(color = MaterialTheme.colors.background),
                                    icon = {
                                        when (screen) {
                                            MainViewModel.Screen.CEILING_LIGHT -> Text(text = "\uD83D\uDCA1")
                                            MainViewModel.Screen.AIR_COND -> Text(text = "\uD83C\uDF2C")
                                            MainViewModel.Screen.AMP -> Text(text = "\uD83D\uDD0A")
                                        }
                                    },
                                    label = { Text(text = screen.title, fontSize = if (selected) 12.sp else 10.sp) },
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(screen.title) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        haptic()
                                    },
                                    selectedContentColor = MaterialTheme.colors.onBackground,
                                    unselectedContentColor = Color.Gray
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(navController = navController, startDestination = MainViewModel.Screen.CEILING_LIGHT.title, Modifier.padding(innerPadding)) {
                    composable(MainViewModel.Screen.CEILING_LIGHT.title) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = MaterialTheme.colors.background), reverseLayout = true
                        ) {
                            viewModel.items[MainViewModel.Screen.CEILING_LIGHT]?.let {
                                itemsIndexed(it) { i, item -> ListItem(item = item, i == 0) }
                            }
                        }
                    }
                    composable(MainViewModel.Screen.AIR_COND.title) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = MaterialTheme.colors.background), reverseLayout = true
                        ) {
                            viewModel.items[MainViewModel.Screen.AIR_COND]?.let {
                                itemsIndexed(it) { i, item -> ListItem(item = item, i == 0) }
                            }
                            item { ModifyTemperature(viewModel.data.temperature) }
                        }
                    }
                    composable(MainViewModel.Screen.AMP.title) {
                        val spanCount = 4
                        var size by remember { mutableStateOf(IntSize.Zero) }
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = MaterialTheme.colors.background)
                                .onGloballyPositioned { layoutCoordinates -> size = layoutCoordinates.size },
                            reverseLayout = true
                        ) {
                            viewModel.items[MainViewModel.Screen.AMP]?.chunked(spanCount)?.let {
                                items(it) { item ->
                                    GridItem(
                                        items = item.reversed(),
                                        sideLength = with(LocalDensity.current) { (size.width / spanCount).toDp() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ListItem(item: RequestData, first: Boolean) {
        val interactionSource = remember { MutableInteractionSource() }
        if (first.not()) Divider(color = Color(0x20ffffff))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = item.onClick != null,
                    onClick = {
                        item.onClick?.invoke()
                        haptic()
                    },
                    indication = rememberRipple(color = Color(0xc0ffffff)),
                    interactionSource = interactionSource
                )
        ) {
            Text(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterStart),
                text = "${item.emoji} ${item.name}",
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }
    }

    @Composable
    fun GridItem(items: List<RequestData>, sideLength: Dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            items.forEach { item ->
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .size(sideLength)
                        .clickable(
                            enabled = item.onClick != null,
                            onClick = {
                                item.onClick?.invoke()
                                haptic()
                            },
                            indication = rememberRipple(color = Color(0xc0ffffff)),
                            interactionSource = interactionSource
                        ),
                ) {
                    Text(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart),
                        text = item.emoji,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp
                    )
                    Text(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomEnd),
                        text = item.name,
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }

    @Composable
    fun Environmental() {
        Button(
            onClick = {
                viewModel.requestEnvironmentalData()
                haptic()
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Colors.TEAL700),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(text = getString(R.string.request_environmental), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }

    @Composable
    fun EnvironmentalLog() {
        Button(
            onClick = {
                viewModel.requestEnvironmentalLog(id = "D8:BF:C0:D0:09:07")
                haptic()
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Colors.TEAL700),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(text = getString(R.string.request_environmental_log), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }

    @Composable
    fun Loading() {
        if (viewModel.data.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colors.surface)
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Colors.TEAL200)
            }
        }
    }

    @Composable
    fun Error() {
        viewModel.data.error?.let {
            if ((it as? HttpException)?.code() in 400..401) {
                startActivity(LoginActivity.newIntent(this))
            }
            Snackbar(
                action = {
                    Button(onClick = { viewModel.clearError() }) {
                        Text(text = "OK", fontWeight = FontWeight.Bold)
                    }
                }
            ) {
                Text(text = it.message.orEmpty())
            }
        }
    }

    @Composable
    fun EnvironmentalDialog() {
        viewModel.data.environmentalData?.let {
            AlertDialog(
                onDismissRequest = { viewModel.clearEnvironmentalData() },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearEnvironmentalData()
                            haptic()
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
                            it.temperature,
                            it.humidity,
                            it.pressure,
                            it.illuminance
                        ),
                        color = MaterialTheme.colors.onBackground,
                        fontSize = 18.sp,
                        lineHeight = 30.sp
                    )
                },
                backgroundColor = Colors.TEAL900
            )
        }
    }

    @Composable
    fun EnvironmentalLogDialog() {
        viewModel.data.environmentalLogData?.let { environmentalLog ->
            AlertDialog(
                onDismissRequest = { viewModel.clearEnvironmentalLogData() },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearEnvironmentalLogData()
                            haptic()
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent, contentColor = MaterialTheme.colors.onBackground),
                        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp)
                    ) {
                        Text(text = "OK")
                    }
                },
                title = {
                    Text(
                        text = "📈 グラフ",
                        fontSize = 24.sp,
                        color = MaterialTheme.colors.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                },
                text = {
                    val getPlot =
                        { canvasSize: Size, padding: RectF, value: Float, maxValue: Float, minValue: Float, time: Date, start: Date, end: Date ->
                            val paddingLeft = padding.left
                            val paddingBottom = padding.bottom
                            val width = canvasSize.width - (paddingLeft + padding.right)
                            val height = canvasSize.height - (padding.top + paddingBottom)
                            val timeRange = end.time - start.time
                            val timeOffset = time.time - start.time
                            val valueRange = maxValue - minValue
                            val valueOffset = maxValue - value

                            PointF(paddingLeft + width * timeOffset / timeRange, paddingBottom + height * valueOffset / valueRange)
                        }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        val start = environmentalLog.firstOrNull()?.date ?: return@AlertDialog
                        val end = environmentalLog.lastOrNull()?.date ?: return@AlertDialog
                        Row(modifier = Modifier.weight(1f)) {
                            val maxTemperature = ceil(environmentalLog.maxBy { it.temperature }.temperature / 10) * 10
                            val minTemperature = floor(environmentalLog.minBy { it.temperature }.temperature / 10) * 10
                            val maxHumidity = ceil(environmentalLog.maxBy { it.humidity }.humidity / 10) * 10
                            val minHumidity = floor(environmentalLog.minBy { it.humidity }.humidity / 10) * 10
                            val maxPressure = ceil(environmentalLog.maxBy { it.pressure }.pressure / 10) * 10
                            val minPressure = floor(environmentalLog.minBy { it.pressure }.pressure / 10) * 10
                            Column(modifier = Modifier.fillMaxHeight()) {
                                Text(text = "${maxTemperature.toInt()} ℃", color = Color.Yellow, fontSize = 10.sp)
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(1f)
                                )
                                Text(text = "${minTemperature.toInt()} ℃", color = Color.Yellow, fontSize = 10.sp)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                val density = LocalDensity.current
                                Canvas(
                                    modifier = Modifier.fillMaxSize(),
                                    onDraw = {
                                        val graphPadding = with(density) {
                                            RectF(8.dp.toPx(), 8.sp.toPx(), 8.dp.toPx(), 8.sp.toPx())
                                        }
                                        val temperaturePath = Path()
                                        environmentalLog.forEachIndexed { index, log ->
                                            val plot =
                                                getPlot(size, graphPadding, log.temperature, maxTemperature, minTemperature, log.date, start, end)
                                            if (index == 0) {
                                                temperaturePath.moveTo(plot.x, plot.y)
                                            } else {
                                                temperaturePath.lineTo(plot.x, plot.y)
                                            }
                                        }
                                        drawPath(
                                            path = temperaturePath,
                                            color = Color.Yellow,
                                            style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                        )

                                        val humidityPath = Path()
                                        environmentalLog.forEachIndexed { index, log ->
                                            val plot =
                                                getPlot(size, graphPadding, log.humidity, maxHumidity, minHumidity, log.date, start, end)
                                            if (index == 0) {
                                                humidityPath.moveTo(plot.x, plot.y)
                                            } else {
                                                humidityPath.lineTo(plot.x, plot.y)
                                            }
                                        }
                                        drawPath(
                                            path = humidityPath,
                                            color = Color.Cyan,
                                            style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                        )
                                        val pressurePath = Path()
                                        environmentalLog.forEachIndexed { index, log ->
                                            val plot =
                                                getPlot(size, graphPadding, log.pressure, maxPressure, minPressure, log.date, start, end)
                                            if (index == 0) {
                                                pressurePath.moveTo(plot.x, plot.y)
                                            } else {
                                                pressurePath.lineTo(plot.x, plot.y)
                                            }
                                        }
                                        drawPath(
                                            path = pressurePath,
                                            color = Color.Green,
                                            style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                        )
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
                                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${maxPressure.toInt()} hPa",
                                        color = Color.Green,
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
                                        color = Color.Green,
                                        fontSize = 10.sp,
                                        modifier = Modifier
                                            .background(color = Colors.TEAL900)
                                            .padding(horizontal = 4.dp)
                                    )
                                }
                            }
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
                            val startText = get<SimpleDateFormat>().format(start).split(' ')
                            val endText = get<SimpleDateFormat>().format(end).split(' ')
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
                            horizontalArrangement = Center
                        ) {
                            Column(modifier = Modifier.padding(end = 4.dp)) {
                                Text(text = "🔼", fontSize = 10.sp)
                                Text(text = "🔽", fontSize = 10.sp)
                            }
                            Column(modifier = Modifier.padding(end = 4.dp)) {
                                Text(text = "${environmentalLog.maxBy { it.temperature }.temperature} ℃", fontSize = 10.sp, color = Color.Yellow)
                                Text(text = "${environmentalLog.minBy { it.temperature }.temperature} ℃", fontSize = 10.sp, color = Color.Yellow)
                            }
                            Column(modifier = Modifier.padding(end = 4.dp)) {
                                Text(text = "${environmentalLog.maxBy { it.humidity }.humidity} %", fontSize = 10.sp, color = Color.Cyan)
                                Text(text = "${environmentalLog.minBy { it.humidity }.humidity} %", fontSize = 10.sp, color = Color.Cyan)
                            }
                            Column {
                                Text(text = "${environmentalLog.maxBy { it.pressure }.pressure} hPa", fontSize = 10.sp, color = Color.Green)
                                Text(text = "${environmentalLog.minBy { it.pressure }.pressure} hPa", fontSize = 10.sp, color = Color.Green)
                            }
                        }
                    }
                },
                backgroundColor = Colors.TEAL900
            )
        }
    }

    @Composable
    fun ModifyTemperature(currentTemperature: Float) {
        Divider(color = Color(0x20ffffff))
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = {
                    viewModel.downTemperature()
                    haptic()
                },
                interactionSource = remember { MutableInteractionSource() }
            ) {
                Text(
                    text = "🔽",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = "$currentTemperature ℃",
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            IconButton(
                onClick = {
                    viewModel.upTemperature()
                    haptic()
                },
                interactionSource = remember { MutableInteractionSource() }
            ) {
                Text(
                    text = "🔼",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }
        }
    }

    private fun haptic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getSystemService(Vibrator::class.java)?.vibrate(
                VibrationEffect
                    .startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK)
                    .compose()
            )
        }
    }
}