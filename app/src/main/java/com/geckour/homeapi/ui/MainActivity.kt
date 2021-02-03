package com.geckour.homeapi.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Bottom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredWidth
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
                Column(verticalArrangement = Bottom, modifier = Modifier.fillMaxSize()) {
                    LazyColumn(verticalArrangement = Bottom, modifier = Modifier.weight(1f), reverseLayout = true) {
                        items(viewModel.items) { item -> Item(item = item) }
                    }
                    Tab()
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
        Button(
            onClick = item.onClick,
            modifier = Modifier.fillMaxSize(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp),
            shape = RectangleShape,
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(
                text = item.name,
                modifier = Modifier.fillMaxWidth(),
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
    fun Tab() {
        Divider(color = Color(0xff018786))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .preferredHeight(IntrinsicSize.Min)
        ) {
            TabItem(text = "天井灯") { viewModel.showCeilingLightItems() }
            VerticalDivider(modifier = Modifier.padding(vertical = 8.dp))
            TabItem(text = "アンプ") { viewModel.showAmpItems() }
        }
    }

    @Composable
    fun RowScope.TabItem(text: String, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp),
            shape = RectangleShape,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color(0xc0ffffff)
            )
        }
    }

    @Composable
    fun VerticalDivider(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .preferredWidth(1.dp)
                .background(color = Color(0x20ffffff))
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
                    AmbientHapticFeedback
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
}