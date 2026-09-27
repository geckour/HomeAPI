package com.geckour.homeapi.ui.main

import android.view.MotionEvent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.geckour.homeapi.R
import com.geckour.homeapi.ui.Colors
import kotlinx.coroutines.launch

object Bar {

    enum class Room(val id: String) {
        LIVING("0"),
        KITCHEN("1")
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun TopBar(
        onSetRoom: (room: Room) -> Unit,
        onSendSignal: (signal: Boolean) -> Unit,
        onHaptic: () -> Unit,
        currentRoom: Room
    ) {
        // Keeps the colors of Material 2's `primarySurface`
        val containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
        val contentColor = contentColorFor(containerColor)
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.app_name),
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInteropFilter { event ->
                            return@pointerInteropFilter when (event.action) {
                                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                                    onSendSignal(true)
                                    true
                                }
                                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                                    onSendSignal(false)
                                    true
                                }
                                else -> false
                            }
                        },
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                Button(
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = {
                        onSetRoom(Room.LIVING)
                        onHaptic()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentRoom == Room.LIVING) Colors.TEAL700 else Colors.TEAL900,
                        contentColor = if (currentRoom == Room.LIVING) MaterialTheme.colorScheme.onPrimary else contentColor
                    )
                ) {
                    Text(text = "居室")
                }
                Button(
                    modifier = Modifier.padding(end = 12.dp),
                    onClick = {
                        onSetRoom(Room.KITCHEN)
                        onHaptic()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentRoom == Room.KITCHEN) Colors.TEAL700 else Colors.TEAL900,
                        contentColor = if (currentRoom == Room.KITCHEN) MaterialTheme.colorScheme.onPrimary else contentColor
                    )
                ) {
                    Text(text = "台所")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = containerColor,
                titleContentColor = contentColor,
                actionIconContentColor = contentColor
            )
        )
    }

    @Composable
    fun BottomBar(
        pagerState: PagerState,
        onRequestEnvironmentalData: () -> Unit,
        onRequestLogData: (range: Dialog.Range) -> Unit,
        onHaptic: () -> Unit,
        currentRange: Dialog.Range
    ) {
        val coroutineScope = rememberCoroutineScope()
        Column {
            Row(
                modifier = Modifier
                    .padding(vertical = 20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                DialogButton.EnvironmentalDialogButton(onRequestData = onRequestEnvironmentalData, onHaptic = onHaptic)
                Spacer(modifier = Modifier.width(8.dp))
                DialogButton.LogDialogButton(onRequestData = onRequestLogData, onHaptic = onHaptic, currentRange = currentRange)
            }
            ShortNavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                Page.Screen.entries.forEachIndexed { index, screen ->
                    ShortNavigationBarItem(
                        // `targetPage` reflects the destination immediately on tapping, unlike `currentPage` which passes through the pages in between
                        selected = index == pagerState.targetPage,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                            onHaptic()
                        },
                        icon = {
                            when (screen) {
                                Page.Screen.CEILING_LIGHT -> Text(text = "💡")
                                Page.Screen.AIR_COND -> Text(text = "🌬")
                                Page.Screen.AMP -> Text(text = "🔊")
                            }
                        },
                        label = { Text(text = screen.title) },
                        colors = ShortNavigationBarItemDefaults.colors(
                            selectedTextColor = MaterialTheme.colorScheme.onBackground,
                            selectedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    }
}
