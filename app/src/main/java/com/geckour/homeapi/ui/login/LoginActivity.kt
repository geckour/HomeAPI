package com.geckour.homeapi.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geckour.homeapi.ui.Colors
import com.geckour.homeapi.ui.DarkColors
import com.geckour.homeapi.ui.LightColors
import com.geckour.homeapi.ui.main.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException

class LoginActivity : AppCompatActivity() {

    companion object {

        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }

    private val viewModel by viewModel<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(colors = if (isSystemInDarkTheme()) DarkColors else LightColors) {
                Content()
                Loading()
                Error()
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun Content() {
        if (viewModel.data.success) {
            startActivity(MainActivity.newIntent(this))
        }

        Column(
            modifier = Modifier
                .fillMaxHeight(.67f)
                .fillMaxWidth()
                .padding(60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val focusManager = LocalFocusManager.current
            var username by remember { mutableStateOf("") }
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .onPreviewKeyEvent { event ->
                        if (event.key in listOf(Key.Enter, Key.Tab)) {
                            focusManager.moveFocus(FocusDirection.Down)
                            return@onPreviewKeyEvent true
                        }
                        return@onPreviewKeyEvent false
                    },
                value = username,
                onValueChange = { username = it },
                label = { Text(text = "ユーザ名") },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    focusedBorderColor = MaterialTheme.colors.primary,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            var password by remember { mutableStateOf("") }
            var passwordVisible by remember { mutableStateOf(false) }
            OutlinedTextField(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .onPreviewKeyEvent { event ->
                        if (event.key in listOf(Key.Enter, Key.Tab)) {
                            focusManager.moveFocus(FocusDirection.Down)
                            return@onPreviewKeyEvent true
                        }
                        return@onPreviewKeyEvent false
                    },
                value = password,
                onValueChange = { password = it },
                label = { Text(text = "パスワード") },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    focusedBorderColor = MaterialTheme.colors.primary,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password, imeAction = ImeAction.Go),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = passwordVisible.not() }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "パスワード表示・非表示の切り替え"
                        )
                    }
                }
            )

            Button(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                onClick = { viewModel.login(username, password) },
                enabled = username.isNotBlank() && password.isNotBlank()
            ) {
                Text(modifier = Modifier.padding(8.dp), text = "ログイン", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    fun Loading() {
        if (viewModel.data.isLoading) {
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
    fun Error() {
        viewModel.data.error?.let {
            if ((it as? HttpException)?.code() == 401) {
                startActivity(newIntent(this))
            }
            Snackbar { Text(text = it.message.orEmpty()) }
        }
    }
}