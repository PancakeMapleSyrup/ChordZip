package com.example.chordzip.songlist

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

import com.example.chordzip.data.Song
import com.example.chordzip.data.SongDao
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
    private val songDao: SongDao  // DAO를 직접 주입받음
) : ViewModel() {
    private val _inputTitle = MutableStateFlow("")
    private val _inputArtist = MutableStateFlow("")

    private val _dDayState = MutableStateFlow(DDayState())
    val dDayState = _dDayState.asStateFlow()



    // ViewModel에서 데이터를 가공해서 UiState로 만듦
    val uiState: StateFlow<SongListUiState> = combine(
        songDao.getAllSongs(), // DB에서 실시간으로 노래 목록을 받아옴
        _inputTitle,
        _inputArtist
    ) { songs, title, artist ->
        SongListUiState(
            favoriteSongs = songs.filter { it.isFavorite }, // DB 데이터 필터링
            normalSongs = songs.filter { !it.isFavorite },
            inputTitle = title,
            inputArtist = artist
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SongListUiState()
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
            // [DB 저장] 새로운 Song 객체를 만들어 DAO에게 전달
            val newSong = Song(title = title, artist = finalArtist)
            songDao.insertSong(newSong)

            // 입력창 초기화
            _inputTitle.value = ""
            _inputArtist.value = ""
        }
    }

    // 즐겨찾기 토글 (상태 변경)
    fun toggleFavorite(songId: String) {
        viewModelScope.launch {
            // DB에서 ID로 노래를 찾음
            val currentSong = songDao.getSongById(songId) ?: return@launch
            // 상태를 반대로 바꿔서 다시 저장 (덮어쓰기)
            songDao.insertSong(currentSong.copy(isFavorite = !currentSong.isFavorite))
        }
    }

    // 노래 정보 수정 기능
    fun updateSong(id: String, newTitle: String, newArtist: String) {
        viewModelScope.launch {
            val currentSong = songDao.getSongById(id) ?: return@launch
            songDao.insertSong(currentSong.copy(title = newTitle, artist = newArtist))
        }
    }

    // 노래 삭제 기능
    fun deleteSong(id: String) {
        // 삭제하려면 객체가 필요하므로 먼저 찾고 삭제
        viewModelScope.launch {
            // 이제 빨간 줄이 사라질 겁니다.
            val songToDelete = songDao.getSongById(id)
            if (songToDelete != null) {
                songDao.deleteSong(songToDelete)
            }
        }
    }

    // [참고] 순서 변경 기능 (Room DB 도입 시 주의점)
    fun reorderByKeys(fromId: String, toId: String) {
        /*
           TODO: Room DB에서 순서를 바꾸려면 'order'라는 정수형 필드(컬럼)가 추가로 필요합니다.
           현재 DB 구조에는 순서를 기억하는 칸이 없으므로, 이 기능은 잠시 비활성화합니다.
           나중에 DB 버전을 올리면서 순서 기능을 구현할 수 있습니다.
        */

        // viewModelScope.launch {
        //    repository.swapSongs(fromId, toId)
        // }
    }
}
