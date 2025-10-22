package com.unscroll.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.unscroll.app.theme.UnscrollTheme
import com.unscroll.app.viewmodel.AppViewModel
import com.unscroll.app.ui.AppBlockerScreen

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UnscrollTheme {
                AppBlockerScreen(viewModel = appViewModel)
            }
        }
    }
}
