package com.geckour.homeapi.ui

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.InteractionState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Bottom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.ripple.ExperimentalRippleApi
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.AndroidDialogProperties
import com.geckour.homeapi.R
import com.geckour.homeapi.model.RequestData

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalRippleApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Column(verticalArrangement = Bottom, modifier = Modifier.fillMaxSize()) {
                    val currentScreen = mutableStateOf(MainViewModel.Screen.CEILING_LIGHT)

                    LazyColumn(modifier = Modifier.weight(1f), reverseLayout = true) {
                        viewModel.items[currentScreen.value]?.let {
                            items(it) { item -> Item(item = item) }
                        }
                    }
                    Tabs(currentScreen)
                    Environmental()
                }

                Loading()
                Error()

                EnvironmentalDialog()
            }
        }
    }

    @Composable
    fun Item(item: RequestData) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = {
                        item.onClick()
                        haptic()
                    },
                    indication = rememberRipple(color = Color(0xc0ffffff)),
                    interactionState = InteractionState()
                )
        ) {
            Text(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterStart),
                text = item.name,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xc0ffffff)
            )
        }
        Divider(color = Color(0x20ffffff))
    }

    @OptIn(ExperimentalLayout::class)
    @Composable
    fun Tabs(currentScreen: MutableState<MainViewModel.Screen>) {
        Divider(color = Color(0xff018786))
        TabRow(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = MainViewModel.Screen.valueOf(currentScreen.value.name).ordinal,
            backgroundColor = Color.Transparent,
            contentColor = Color(0xc0ffffff)
        ) {
            ScreenTab(screen = MainViewModel.Screen.CEILING_LIGHT, currentScreen = currentScreen)
            ScreenTab(screen = MainViewModel.Screen.AMP, currentScreen = currentScreen)
        }
    }

    @Composable
    fun ScreenTab(screen: MainViewModel.Screen, currentScreen: MutableState<MainViewModel.Screen>) {
        Tab(
            selected = currentScreen.value.ordinal == screen.ordinal,
            onClick = {
                currentScreen.value = screen
                haptic()
            },
            text = { Text(text = screen.title, fontWeight = FontWeight.Bold) }
        )
    }

    @Composable
    fun Environmental() {
        Box(
            modifier = Modifier
                .padding(16.dp)
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
                backgroundColor = Color(0xFF1F2727),
                properties = AndroidDialogProperties()
            )
        }
    }

    private fun haptic() {
        getSystemService(Vibrator::class.java)?.vibrate(
            VibrationEffect
                .startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK)
                .compose()
        )
    }
}