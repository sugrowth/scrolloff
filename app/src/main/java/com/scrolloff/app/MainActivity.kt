package com.scrolloff.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.scrolloff.app.theme.ScrollOffTheme
import com.scrolloff.app.viewmodel.AppViewModel
import com.scrolloff.app.ui.AppBlockerScreen

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScrollOffTheme {
                AppBlockerScreen(viewModel = appViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appViewModel.refreshPermissions()
    }
}
