package com.example.chordzip.data

// 코드 운지법 템플릿 데이터 클래스
data class ChordVoicing(
    val name: String,
    val startFret: Int = 1,      // 시작 프렛 (기본값 1)
    val positions: List<Int>     // 상대 좌표 (예: 1~5, 0=Open, -1=Mute)
)