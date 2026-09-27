package com.geckour.homeapi.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geckour.homeapi.receiver.requestSleepData
import com.geckour.homeapi.ui.AppTheme
import com.geckour.homeapi.ui.Colors
import com.geckour.homeapi.ui.login.LoginActivity
import com.geckour.homeapi.util.getRequestPermissionsLauncher
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException

class MainActivity : AppCompatActivity() {

    companion object {

        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val permissions = buildList {
            requestSleepData(this@MainActivity) { add(Manifest.permission.ACTIVITY_RECOGNITION) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        // Requests them at once since the system rejects a request made while another one is in progress
        if (permissions.isNotEmpty()) {
            getRequestPermissionsLauncher().launch(permissions.toTypedArray())
        }

        setContent {
            AppTheme {
                val data by viewModel.data.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }
                var currentRange by remember { mutableStateOf(Dialog.Range.DAY) }

                Content(
                    snackbarHostState = snackbarHostState,
                    data = data,
                    currentRange = currentRange,
                )
                Loading(data = data)
                Error(data = data, snackbarHostState = snackbarHostState)

                data.environmentalData?.let {
                    Dialog.Environmental(environmentalData = it, onClearData = viewModel::clearEnvironmentalData, onHaptic = ::haptic)
                }
                val environmentalLogData = data.environmentalLogData
                val soilHumidityLogData = data.soilHumidityLogData
                val co2LogData = data.co2LogData
                if (environmentalLogData != null && soilHumidityLogData != null && co2LogData != null) {
                    Dialog.EnvironmentalLog(
                        environmentalLogData = environmentalLogData,
                        soilHumidityLogData = soilHumidityLogData,
                        co2LogData = co2LogData,
                        simpleDateFormat = get(),
                        onNormalize = viewModel::normalizeEnvironmentalLogData,
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

    @Composable
    fun Content(
        snackbarHostState: SnackbarHostState,
        data: MainViewModel.MainData,
        currentRange: Dialog.Range,
    ) {
        // The pager is the single source of truth of the current page, so both of swiping and the navigation bar can drive it
        val pagerState = rememberPagerState(pageCount = { Page.Screen.entries.size })
        Scaffold(
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
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { innerPadding ->
            HorizontalPager(
                modifier = Modifier.padding(innerPadding),
                state = pagerState,
            ) { page ->
                when (Page.Screen.entries[page]) {
                    Page.Screen.CEILING_LIGHT -> {
                        Page.CeilingLight(items = viewModel.items, onHaptic = ::haptic)
                    }

                    Page.Screen.AIR_COND -> {
                        Page.AirConductor(
                            items = viewModel.items,
                            onHaptic = ::haptic,
                            onDownTemperature = viewModel::downTemperature,
                            onUpTemperature = viewModel::upTemperature,
                            currentTemperature = data.temperature
                        )
                    }

                    Page.Screen.AMP -> {
                        Page.Amp(items = viewModel.items, onHaptic = ::haptic)
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
                    .background(color = MaterialTheme.colorScheme.surface)
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Colors.TEAL200)
            }
        }
    }

    @Composable
    fun Error(data: MainViewModel.MainData, snackbarHostState: SnackbarHostState) {
        data.error?.let {
            if ((it as? HttpException)?.code() in 400..401) {
                startActivity(LoginActivity.newIntent(this))
            }

            LaunchedEffect(Unit) {
                snackbarHostState.showSnackbar(it.message.orEmpty(), actionLabel = "OK")
            }
        }
    }
}