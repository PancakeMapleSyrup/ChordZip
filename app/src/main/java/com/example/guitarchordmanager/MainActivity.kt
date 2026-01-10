package com.example.guitarchordmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.guitarchordmanager.ui.theme.GuitarChordManagerTheme
import com.example.guitarchordmanager.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GuitarChordManagerTheme {
                // 네비게이션 시작!
                AppNavigation()
            }
        }
    }
}