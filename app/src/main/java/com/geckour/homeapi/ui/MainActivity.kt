package com.geckour.homeapi.ui

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Bottom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geckour.homeapi.R
import com.geckour.homeapi.model.RequestData

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Contents()
                Loading()
                Error()

                EnvironmentalDialog()
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Contents() {
        Column(verticalArrangement = Bottom, modifier = Modifier.fillMaxSize()) {
            var currentScreen by remember { mutableStateOf(MainViewModel.Screen.CEILING_LIGHT) }

            when (currentScreen) {
                MainViewModel.Screen.CEILING_LIGHT -> {
                    LazyColumn(modifier = Modifier.weight(1f), reverseLayout = true) {
                        viewModel.items[currentScreen]?.let {
                            itemsIndexed(it) { i, item -> ListItem(item = item, i == 0) }
                        }
                    }
                }
                MainViewModel.Screen.AIR_COND -> {
                    LazyColumn(modifier = Modifier.weight(1f), reverseLayout = true) {
                        viewModel.items[currentScreen]?.let {
                            itemsIndexed(it) { i, item -> ListItem(item = item, i == 0) }
                        }
                    }
                }
                MainViewModel.Screen.AMP -> {
                    val spanCount = 4
                    var size by mutableStateOf(IntSize.Zero)
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { layoutCoordinates -> size = layoutCoordinates.size },
                        reverseLayout = true
                    ) {
                        viewModel.items[currentScreen]?.chunked(spanCount)?.let {
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
            Environmental()
            Tabs(currentScreen) { screen ->
                currentScreen = screen
            }
        }
    }

    @Composable
    fun Tabs(currentScreen: MainViewModel.Screen, onScreenChanged: (screen: MainViewModel.Screen) -> Unit) {
        TabRow(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = MainViewModel.Screen.valueOf(currentScreen.name).ordinal,
            divider = {},
            backgroundColor = Color.Transparent,
            contentColor = Color(0xc0ffffff)
        ) {
            viewModel.items.keys.forEach {
                ScreenTab(screen = it, currentScreen = currentScreen, onScreenChanged = onScreenChanged)
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
                fontSize = 22.sp,
                color = Color(0xc0ffffff)
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
                        fontSize = 36.sp,
                        color = Color(0xc0ffffff)
                    )
                    Text(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomEnd),
                        text = item.name,
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        color = Color(0xc0ffffff)
                    )
                }
            }
        }
    }

    @Composable
    fun ScreenTab(screen: MainViewModel.Screen, currentScreen: MainViewModel.Screen, onScreenChanged: (screen: MainViewModel.Screen) -> Unit) {
        Tab(
            selected = currentScreen.ordinal == screen.ordinal,
            onClick = {
                onScreenChanged(screen)
                haptic()
            },
            text = {
                Text(
                    text = screen.title,
                    fontWeight = FontWeight.Bold
                )
            },
            modifier = Modifier.height(56.dp)
        )
    }

    @Composable
    fun Environmental() {
        Box(
            modifier = Modifier
                .padding(vertical = 20.dp)
                .fillMaxWidth()
        ) {
            Button(
                onClick = {
                    viewModel.requestEnvironmentalData()
                    haptic()
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(getColor(R.color.teal_700))),
                modifier = Modifier.align(Alignment.Center),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(text = getString(R.string.request_environmental), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }

    @Composable
    fun Loading() {
        if (viewModel.data.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color(0x80000000))
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF03DAC5))
            }
        }
    }

    @Composable
    fun Error() {
        viewModel.data.error?.let {
            Snackbar { Text(text = it.message.orEmpty()) }
        }
    }

    @Composable
    fun EnvironmentalDialog() {
        viewModel.data.environmentalData?.let {
            AlertDialog(
                onDismissRequest = { viewModel.clearEnvironmentalData() },
                confirmButton = {
                    Button(
                        onClick = { viewModel.clearEnvironmentalData() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp)
                    ) {
                        Text(text = "OK", color = Color(0xc0ffffff))
                    }
                },
                title = {
                    Text(
                        text = "📡 環境値",
                        fontSize = 22.sp,
                        lineHeight = 33.sp,
                        color = Color(0xc0ffffff),
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
                        fontSize = 18.sp,
                        lineHeight = 27.sp,
                        color = Color(0xc0ffffff)
                    )
                },
                backgroundColor = Color(0xFF1F2727)
            )
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