package com.example.guitarchordmanager.songlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

import com.example.guitarchordmanager.data.Song
import com.example.guitarchordmanager.data.repository.SongRepository
import kotlinx.coroutines.launch

// UI 상태를 정의하는 Data Class
data class SongListUiState(
    val favoriteSongs: List<Song> = emptyList(), // 즐겨찾기 목록
    val normalSongs: List<Song> = emptyList(),   // 일반 목록
    val inputTitle: String = "",                 // 입력 중인 제목
    val inputArtist: String = ""                 // 입력 중인 가수
)

// D-day 상태 관리를 위한 데이터 클래스
data class DDayState(
    val targetDate: LocalDate? = null,
    val goal: String = "",
    val dDayText: String = "" // "D-10" 등 계산된 텍스트
)

@HiltViewModel
class SongListViewModel @Inject constructor(
    private val repository: SongRepository  // Repository 주입 받음
) : ViewModel() {

    private val _inputTitle = MutableStateFlow("")
    private val _inputArtist = MutableStateFlow("")

    private val _dDayState = MutableStateFlow(DDayState())
    val dDayState = _dDayState.asStateFlow()


    // ViewModel에서 데이터를 가공해서 UiState로 만듦
    val uiState: StateFlow<SongListUiState> = combine(
        repository.getSongsStream(), // (A) DB에서 오는 노래 데이터
        _inputTitle,                // (B) 입력 중인 제목
        _inputArtist                // (C) 입력 중인 가수
    ) {
        songs, title, artist ->     // (A, B, C)가 바뀔 때마다 이 블록이 실행됨
        SongListUiState(
            favoriteSongs = songs.filter { it.isFavorite },
            normalSongs = songs.filter { !it.isFavorite },
            inputTitle = title,
            inputArtist = artist
        )
    }.stateIn(
        scope = viewModelScope, // 뷰모델이 살아있는 동안만 작동
        started = SharingStarted.WhileSubscribed(5000), // 화면 꺼지면 5초 뒤 구독 취소
        initialValue = SongListUiState() // 초기값
    )

    // D-day 설정 및 계산 함수
    fun setDDay(date: LocalDate, goal: String) {
        val today = LocalDate.now()
        val diff = ChronoUnit.DAYS.between(today, date)

        val text = when {
            diff > 0 -> "D-$diff"
            diff == 0L -> "Today"
            else -> "D+${-diff}"
        }

        // 상태 업데이트
        _dDayState.update {
            it.copy(targetDate = date, goal = goal, dDayText = text)
        }
    }

    // --- 입력 값 업데이트 함수 ---
    fun updateInputTitle(text: String) {
        _inputTitle.value = text
    }

    fun updateInputArtist(text: String) {
        _inputArtist.value = text
    }

    // 노래 추가 버튼 눌렀을 때
    fun addSong() {
        val title = _inputTitle.value
        val artist = _inputArtist.value

        if (title.isBlank()) return // 제목 없으면 무시

        val finalArtist = if (artist.isBlank()) "Unknown Artist" else artist
        viewModelScope.launch {
            // Repository에 저장 요청
            repository.addSong(Song(title = title, artist = finalArtist))

            // 입력창 초기화
            _inputTitle.value = ""
            _inputArtist.value = ""
        }
    }

    // 즐겨찾기 토글 (상태 변경)
    fun toggleFavorite(songId: String) {
        viewModelScope.launch {
            //  현재 노래 찾기
            val currentSong = repository.getSongById(songId) ?: return@launch
            //  상태 바꿔서 업데이트 요청
            repository.updateSong(currentSong.copy(isFavorite = !currentSong.isFavorite))
        }
    }

    // 노래 정보 수정 기능
    fun updateSong(id: String, newTitle: String, newArtist: String) {
        viewModelScope.launch {
            val currentSong = repository.getSongById(id) ?: return@launch
            repository.updateSong(currentSong.copy(title = newTitle, artist = newArtist))
        }
    }

    // 노래 삭제 기능
    fun deleteSong(id: String) {
        viewModelScope.launch {
            repository.deleteSong(id)
        }
    }

    fun reorderByKeys(fromId: String, toId: String) {
        viewModelScope.launch {
            repository.swapSongs(fromId, toId)
        } /** TODO: 이렇게 하면 앱이 켜져 있는 동안은 순서 변경이 완벽하게 유지됩니다!
                    (앱 껐다 켜도 유지되려면 나중에 Room DB 도입 시 order 필드 관리가 필요합니다) **/
    }
}
