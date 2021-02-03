package com.geckour.homeapi.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Bottom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.AmbientHapticFeedback
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Column(verticalArrangement = Bottom, modifier = Modifier.fillMaxSize(1f)) {
                    LazyColumn(verticalArrangement = Bottom, modifier = Modifier.weight(1f)) {
                        items(viewModel.items) { item -> Item(item = item) }
                    }
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Button(
                            onClick = {
                                viewModel.requestEnvironmentalData()
                                AmbientHapticFeedback
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(getColor(R.color.teal_700))),
                        ) {
                            Text(text = getString(R.string.request_environmental), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }

                Loading()
                Error()

                Environmental()
            }
        }
    }

    @Composable
    fun Item(item: RequestData) {
        Divider(color = Color(0x18ffffff))
        Button(
            onClick = item.onClick,
            modifier = Modifier.fillMaxSize(1f),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp),
            shape = RectangleShape,
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(
                text = item.name,
                modifier = Modifier.fillMaxWidth(1f),
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xc0ffffff)
            )
        }
    }

    @Composable
    fun Loading() {
        if (viewModel.data.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(1f)
                    .background(color = Color(0x80000000))
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
    fun Environmental() {
        viewModel.data.environmentalData?.let {
            AlertDialog(
                onDismissRequest = { viewModel.clearEnvironmentalData() },
                confirmButton = {
                    Button(
                        onClick = { viewModel.clearEnvironmentalData() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp)
                    ) {
                        Text(text = "OK")
                    }
                },
                title = { Text(text = "📡 環境値") },
                text = {
                    Text(
                        text = String.format(
                            "🌡 %.2f [℃]\n💧 %.2f [%%]\n🌪 %.2f [hPa]\n💡 %.2f [lux]",
                            it.temperature,
                            it.humidity,
                            it.pressure,
                            it.illuminance
                        )
                    )
                },
                properties = AndroidDialogProperties()
            )
        }
    }
}