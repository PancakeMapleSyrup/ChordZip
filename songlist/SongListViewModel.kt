package com.example.guitarchordmanager.songlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

// 데이터 모델
data class Song(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val artist: String = "Unknown Artist",
    val isFavorite: Boolean = false
)

@HiltViewModel
class SongListViewModel @Inject constructor() : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(
        // 초기 더미 데이터
        listOf(
            Song(title = "Hype Boy", artist = "NewJeans", isFavorite = true),
            Song(title = "Ditto", artist = "NewJeans"),
            Song(title = "Seven", artist = "Jung Kook"),
            Song(title = "I AM", artist = "IVE")
        )
    )
    val songs = _songs.asStateFlow()

    // 노래 추가
    fun addSong(title: String, artist: String) {
        if (title.isBlank()) return

        // 가수 이름을 안 적으면 기본값 설정
        val artist = if (artist.isBlank()) "Unknown Artist" else artist
        val newSong = Song(title = title, artist = artist)
        _songs.update { it + newSong }
    }

    // 즐겨찾기 토글 (상태 변경)
    fun toggleFavorite(songId: String) {
        _songs.update { list ->
            list.map {
                if (it.id == songId) it.copy(isFavorite = !it.isFavorite) else it
            }
        }
    }

    // 노래 정보 수정 기능
    fun updateSong(id: String, newTitle: String, newArtist: String) {
        _songs.update { list ->
            list.map {
                if (it.id == id ) it.copy(title = newTitle, artist = newArtist) else it
            }
        }
    }

    // 노래 삭제 기능
    fun deleteSong(id: String) {
        _songs.update { list -> list.filter { it.id != id } }
    }

    // 순서 변경 (드래그 앤 드롭)
    // 중요: 즐겨찾기가 아닌 항목들끼리만 순서를 바꾼다.
    fun reorderByKeys(fromId: String, toId: String) {
        _songs.update { list ->
            // 전체 리스트 복사
            val currentList = list.toMutableList()

            // 움직인 아이템과 목표 지점 아이템의 '전체 리스트 기준' 인덱스 찾기
            val fromIndex = currentList.indexOfFirst { it.id == fromId }
            val toIndex = currentList.indexOfFirst { it.id == toId }

            // 둘 다 유효한 인덱스일 때만 교체 진행
            if (fromIndex != -1 && toIndex != -1) {
                val item = currentList.removeAt(fromIndex)
                currentList.add(toIndex, item)
            }

            currentList // 업데이트된 리스트 반환
        }
    }
}
