package com.example.chordzip.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// DAO. Data Access Object
// 데이터베이스에 명령(저장, 삭제, 조회)을 내리는 인터페이스

@Dao    // 이 인터페이스가 DAO임을 알려줌
interface SongDao {
    // [조회] 모든 노래를 가져옴
    // Flow를 반환하면 데이터가 바뀔 때마다 자동으로 UI에 알려줌
    @Query("SELECT * FROM songs")
    fun getAllSongs(): Flow<List<Song>>

    // [조회] 특정 ID를 가진 노래 하나만 가져옴
    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongById(songId: String): Song?

    // [저장/수정] 노래를 저장함
    // OnConflictStrategy.REPLACE: 만약 이미 같은 ID가 있으면 덮어씀 (수정 효과)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    // [삭제] 노래를 삭제함
    @Delete
    suspend fun deleteSong(song: Song)
}