package com.geckour.homeapi.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geckour.homeapi.model.RequestData

object Item {

    @Composable
    fun ListItem(item: RequestData, first: Boolean, onHaptic: () -> Unit) {
        val interactionSource = remember { MutableInteractionSource() }
        if (first.not()) Divider(color = Color(0x20ffffff))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = item.onClick != null,
                    onClick = {
                        item.onClick?.invoke()
                        onHaptic()
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
    fun GridItem(items: List<RequestData>, sideLength: Dp, onHaptic: () -> Unit) {
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
                                onHaptic()
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


}