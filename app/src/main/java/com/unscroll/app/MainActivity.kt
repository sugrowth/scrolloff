package com.unscroll.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import com.unscroll.app.navigation.UnscrollApp
import com.unscroll.app.theme.UnscrollTheme
import com.unscroll.app.viewmodel.AppViewModel
import com.unscroll.app.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels {
        val container = (application as UnscrollApplication).container
        AppViewModelFactory(container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UnscrollTheme {
                Surface {
                    UnscrollApp(appViewModel = appViewModel)
                }
            }
        }
    }
}
