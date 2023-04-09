package com.geckour.homeapi.ui.main

import android.view.MotionEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geckour.homeapi.R
import com.geckour.homeapi.ui.Colors
import kotlinx.coroutines.launch

object Bar {

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun TopBar(
        onSetRoom: (room: MainViewModel.Room) -> Unit,
        onSendSignal: (signal: Boolean) -> Unit,
        onHaptic: () -> Unit,
        currentRoom: MainViewModel.Room
    ) {
        TopAppBar(contentPadding = PaddingValues(horizontal = 16.dp)) {
            Text(
                text = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
            Button(
                modifier = Modifier.padding(end = 8.dp),
                onClick = {
                    onSetRoom(MainViewModel.Room.LIVING)
                    onHaptic()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (currentRoom == MainViewModel.Room.LIVING) Colors.TEAL700 else Colors.TEAL900
                )
            ) {
                Text(text = "居室")
            }
            Button(
                onClick = {
                    onSetRoom(MainViewModel.Room.KITCHEN)
                    onHaptic()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (currentRoom == MainViewModel.Room.KITCHEN) Colors.TEAL700 else Colors.TEAL900
                )
            ) {
                Text(text = "台所")
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun BottomBar(
        pagerState: PagerState,
        onRequestEnvironmentalData: () -> Unit,
        onRequestLogData: () -> Unit,
        onHaptic: () -> Unit
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
                DialogButton.LogDialogButton(onRequestData = onRequestLogData, onHaptic = onHaptic)
            }
            BottomNavigation {
                Page.Screen.values().forEachIndexed { index, screen ->
                    val selected = index == pagerState.currentPage
                    BottomNavigationItem(
                        modifier = Modifier.background(color = MaterialTheme.colors.background),
                        icon = {
                            when (screen) {
                                Page.Screen.CEILING_LIGHT -> Text(text = "\uD83D\uDCA1")
                                Page.Screen.AIR_COND -> Text(text = "\uD83C\uDF2C")
                                Page.Screen.AMP -> Text(text = "\uD83D\uDD0A")
                            }
                        },
                        label = { Text(text = screen.title, fontSize = if (selected) 12.sp else 10.sp) },
                        selected = selected,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                            onHaptic()
                        },
                        selectedContentColor = MaterialTheme.colors.onBackground,
                        unselectedContentColor = Color.Gray
                    )
                }
            }
        }
    }
}