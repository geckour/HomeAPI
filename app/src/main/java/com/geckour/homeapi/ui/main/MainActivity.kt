package com.geckour.homeapi.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement.Bottom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geckour.homeapi.ui.Colors
import com.geckour.homeapi.ui.DarkColors
import com.geckour.homeapi.ui.LightColors
import com.geckour.homeapi.ui.login.LoginActivity
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {

        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(colors = if (isSystemInDarkTheme()) DarkColors else LightColors) {
                val data by viewModel.data.collectAsStateWithLifecycle()
                val scaffoldState = rememberScaffoldState()
                var currentRange by remember { mutableStateOf(Dialog.Range.DAY) }

                Content(scaffoldState = scaffoldState, data = data, currentRange)
                Loading(data = data)
                Error(data = data, scaffoldState = scaffoldState)

                data.environmentalData?.let {
                    Dialog.Environmental(environmentalData = it, onClearData = viewModel::clearEnvironmentalData, onHaptic = ::haptic)
                }
                val environmentalLogData = data.environmentalLogData
                val soilHumidityLogData = data.soilHumidityLogData
                if (environmentalLogData != null && soilHumidityLogData != null) {
                    Dialog.EnvironmentalLog(
                        environmentalLogData = environmentalLogData,
                        soilHumidityLogData = soilHumidityLogData,
                        simpleDateFormat = get(),
                        onClearData = viewModel::clearEnvironmentalLogData,
                        onNewRange = {
                            currentRange = it
                            viewModel.requestLogDialogDataWithRange(range = it)
                        },
                        onHaptic = ::haptic,
                        currentRange = currentRange
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content(scaffoldState: ScaffoldState, data: MainViewModel.MainData, currentRange: Dialog.Range) {
        Column(verticalArrangement = Bottom, modifier = Modifier.fillMaxSize()) {
            val pagerState = rememberPagerState()

            Scaffold(
                scaffoldState = scaffoldState,
                topBar = {
                    Bar.TopBar(
                        onSetRoom = viewModel::setRoom,
                        onSendSignal = viewModel::sendSignal,
                        onHaptic = ::haptic,
                        currentRoom = data.room
                    )
                },
                bottomBar = {
                    Bar.BottomBar(
                        pagerState = pagerState,
                        onRequestEnvironmentalData = viewModel::requestEnvironmentalData,
                        onRequestLogData = viewModel::requestLogDialogDataWithRange,
                        onHaptic = ::haptic,
                        currentRange = currentRange
                    )
                }
            ) { innerPadding ->
                HorizontalPager(pageCount = 3, state = pagerState, contentPadding = innerPadding) { page ->
                    when (page) {
                        0 -> {
                            Page.CeilingLight(items = viewModel.items, onHaptic = ::haptic)
                        }
                        1 -> {
                            Page.AirConductor(
                                items = viewModel.items,
                                onHaptic = ::haptic,
                                onDownTemperature = viewModel::downTemperature,
                                onUpTemperature = viewModel::upTemperature,
                                currentTemperature = data.temperature
                            )
                        }
                        2 -> {
                            Page.Amp(items = viewModel.items, onHaptic = ::haptic)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Loading(data: MainViewModel.MainData) {
        if (data.isLoading) {
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
    fun Error(data: MainViewModel.MainData, scaffoldState: ScaffoldState) {
        data.error?.let {
            if ((it as? HttpException)?.code() in 400..401) {
                startActivity(LoginActivity.newIntent(this))
            }
            rememberCoroutineScope().launch {
                scaffoldState.snackbarHostState.showSnackbar(it.message.orEmpty(), actionLabel = "OK")
            }
        }
    }
}