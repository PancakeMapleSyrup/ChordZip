package com.example.chordzip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.chordzip.ui.theme.GuitarChordManagerTheme
import com.example.chordzip.navigation.AppNavigation
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