package com.example.chordzip.data

import com.example.chordzip.songdetail.SongPart
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// 이 클래스는 앱 전체에서 공용으로 사용
// @Entity: 이 클래스가 데이터베이스의 '테이블'이 된다는 뜻이다.
// tableName = "songs": 테이블 이름을 "songs"로 지정한다.
@Entity(tableName = "songs")
data class Song(
    // @PrimaryKey: 주민등록번호처럼 이 데이터를 식별하는 고유 키
    // UUID를 사용해 랜덤하고 고유한 문자열을 ID로 쓴다.
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    val title: String,                      // 노래 제목
    val artist: String = "Unknown Artist",  // 가수
    val isFavorite: Boolean = false,        // 즐겨찾기 여부
    val bpm: String = "-",                  // BPM
    val capo: String = "None",              // 카포
    val tuning: String = "Standard",        // 튜닝
    val youtubeLink: String = "",           // 유튜브 링크

    // 파트 리스트 (Intro, Chorus 등)
    // 이 리스트는 Converters를 통해 문자열로 저장됨
    val parts: List<SongPart> = emptyList()
)

data class SongPart(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val memo: String = "",
    val chords: List<Chord> = emptyList()
)

data class Chord(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val positions: List<Int>,
    val startFret: Int = 1
)