package com.geckour.homeapi.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Bottom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.geckour.homeapi.BootReceiver
import com.geckour.homeapi.R
import com.geckour.homeapi.model.RequestData
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) sendBroadcast(BootReceiver.newIntent(this))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(colors = if (isSystemInDarkTheme()) DarkColors else DarkColors) {
                Contents()
                Loading()
                Error()

                EnvironmentalDialog()
            }
        }

        if (BootReceiver.activityRecognitionPermissionApproved(this).not()) {
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    @Composable
    fun Contents() {
        Column(verticalArrangement = Bottom, modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()

            Scaffold(
                bottomBar = {
                    Column {
                        Environmental()
                        BottomNavigation {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination

                            MainViewModel.Screen.values().forEach { screen ->
                                BottomNavigationItem(
                                    modifier = Modifier.background(color = MaterialTheme.colors.background),
                                    icon = {},
                                    label = { Text(text = screen.title) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.title } == true,
                                    onClick = {
                                        navController.navigate(screen.title) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
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
                colors = ButtonDefaults.buttonColors(backgroundColor = Colors.TEAL700),
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
                    fontSize = 22.sp,
                    color = Color(0xc0ffffff)
                )
            }
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = "$currentTemperature ℃",
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xc0ffffff)
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
                    fontSize = 22.sp,
                    color = Color(0xc0ffffff)
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