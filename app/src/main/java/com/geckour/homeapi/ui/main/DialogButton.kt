package com.geckour.homeapi.ui.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geckour.homeapi.R
import com.geckour.homeapi.ui.Colors

object DialogButton {

    @Composable
    fun EnvironmentalDialogButton(onRequestData: () -> Unit, onHaptic: () -> Unit) {
        Button(
            onClick = {
                onRequestData()
                onHaptic()
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Colors.TEAL700),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(text = stringResource(R.string.request_environmental), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
    
    @Composable
    fun LogDialogButton(onRequestData: () -> Unit, onHaptic: () -> Unit) {
        Button(
            onClick = {
                onRequestData()
                onHaptic()
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Colors.TEAL700),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(text = stringResource(R.string.request_environmental_log), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}