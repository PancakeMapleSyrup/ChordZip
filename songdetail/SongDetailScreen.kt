package com.example.guitarchordmanager.songdetail

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun SongDetailScreen(title: String, artist: String, onBackClick: () -> Unit) {
    Scaffold(
    ) { padding ->
        // songId를 이용해 ViewModel에서 노래 데이터를 가져와 화면을 구성합니다.
        Text(text = "선택된 곡 ID: $title", modifier = Modifier.padding(padding))
    }
}
