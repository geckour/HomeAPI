package com.geckour.homeapi.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geckour.homeapi.model.RequestData

object Page {

    enum class Screen(val title: String) {
        CEILING_LIGHT("天井灯"),
        AIR_COND("エアコン"),
        AMP("アンプ")
    }

    @Composable
    fun CeilingLight(items: Map<Screen, List<RequestData>>, onHaptic: () -> Unit) {
        items[Screen.CEILING_LIGHT]?.let {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colors.background), reverseLayout = true
            ) {
                itemsIndexed(it) { i, item -> Item.ListItem(item = item, first = i == 0, onHaptic = onHaptic) }
            }
        }
    }

    @Composable
    fun AirConductor(
        items: Map<Screen, List<RequestData>>,
        currentTemperature: Float,
        onDownTemperature: () -> Unit,
        onUpTemperature: () -> Unit,
        onHaptic: () -> Unit
    ) {
        items[Screen.AIR_COND]?.let {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colors.background), reverseLayout = true
            ) {

                itemsIndexed(it) { i, item -> Item.ListItem(item = item, first = i == 0, onHaptic = onHaptic) }
                item {
                    ModifyTemperature(
                        currentTemperature = currentTemperature,
                        onDownTemperature = onDownTemperature,
                        onUpTemperature = onUpTemperature,
                        onHaptic = onHaptic
                    )
                }
            }
        }
    }

    @Composable
    fun ModifyTemperature(currentTemperature: Float, onDownTemperature: () -> Unit, onUpTemperature: () -> Unit, onHaptic: () -> Unit) {
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
                    onDownTemperature()
                    onHaptic()
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
                    onUpTemperature()
                    onHaptic()
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

    @Composable
    fun Amp(items: Map<Screen, List<RequestData>>, onHaptic: () -> Unit) {
        val spanCount = 4
        var size by remember { mutableStateOf(IntSize.Zero) }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colors.background)
                .onGloballyPositioned { layoutCoordinates -> size = layoutCoordinates.size },
            reverseLayout = true
        ) {
            items[Screen.AMP]?.chunked(spanCount)?.let {
                items(it) { item ->
                    Item.GridItem(
                        items = item.reversed(),
                        sideLength = with(LocalDensity.current) { (size.width / spanCount).toDp() },
                        onHaptic = onHaptic
                    )
                }
            }
        }
    }
}