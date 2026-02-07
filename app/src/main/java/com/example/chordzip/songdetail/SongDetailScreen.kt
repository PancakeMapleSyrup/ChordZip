package com.example.chordzip.songdetail

import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chordzip.ui.components.EditSongInfoDialog
import com.example.chordzip.ui.components.EditPartDialog
import com.example.chordzip.ui.components.AddPartDialog
import com.example.chordzip.ui.components.ChordEditorDialog
import com.example.chordzip.ui.components.DeleteDialog
import com.example.chordzip.ui.components.InteractiveFretboard
import com.example.chordzip.ui.theme.*
import com.example.chordzip.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.ceil
import androidx.core.net.toUri

@Composable
fun SongDetailScreen(
    viewModel: SongDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val song = uiState.song // ViewModel이 찾아온 노래

    // 로딩 중이거나 데이터를 못 찾았을 때 처리
    if (song == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = TossBlue) // 로딩 뺑뺑이
        }
        return
    }

    // 다이얼로그 상태
    var showAddPartDialog by remember { mutableStateOf(false) }
    var showAddChordDialogForPartId by remember { mutableStateOf<String?>(null) } // 파트 ID 저장
    var showEditInfoDialog by remember { mutableStateOf(false) }
    var editingPart by remember { mutableStateOf<SongPart?>(null) }
    var partToDelete by remember { mutableStateOf<SongPart?>(null) }
    var editingChordData by remember { mutableStateOf<Pair<String, Chord>?>(null) }

    // 메트로놈 기능
    var isMetronomePlaying by remember { mutableStateOf(false) }
    val bpmValue = remember(song.bpm) { song.bpm.toIntOrNull() ?: 0 }   // BPM 숫자 변환
    val hasValidBpm = bpmValue > 0
    LaunchedEffect(isMetronomePlaying, bpmValue) {
        if (isMetronomePlaying && hasValidBpm) {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100) // ToneGenerator 생성 (알림음, 볼륨 100)
            val interval = 60000L / bpmValue    // 박자 간격 계산 (60,000ms / BPM)
            try {
                while (isActive) { // 코루틴이 취소될 때까지 반복
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 50) // 50ms 동안 삑 소리
                    delay(interval)
                }
            } finally {
                toneGen.release() // 정리
            }
        }
    }
    // 페이지를 떠날 때 메트로놈 끄기
    DisposableEffect(Unit) {
        onDispose { isMetronomePlaying = false }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .statusBarsPadding()
        ) {
            // 상단 네비게이션 바
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Gray900)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column (
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showEditInfoDialog = true }
                        .padding(8.dp)
                ) {
                    Text(song.title, style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text(song.artist, style = Typography.bodyMedium.copy(color = Gray400))

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val highlightBgColor = Color(0xFFFFF9DB)
                        val highlightContentColor = Color(0xFFFFB800)

                        InfoBadge(label = "BPM", value = song.bpm)

                        val isCapoSet = song.capo != "0" && song.capo != "None" && song.capo != "-"
                        InfoBadge(
                            label = "Capo",
                            value = song.capo,
                            containerColor = if (isCapoSet) highlightBgColor else Gray100,
                            contentColor = if (isCapoSet) highlightContentColor else Gray900
                        )
                        val isTuningChanged = song.tuning != "Standard"
                        InfoBadge(
                            label = "Tune",
                            value = song.tuning,
                            containerColor = if (isTuningChanged) highlightBgColor else Gray100,
                            contentColor = if (isTuningChanged) highlightContentColor else Gray900
                        )
                    }
                }
            }

            val parts = uiState.song?.parts ?: emptyList() // 파트 리스트 데이터를 변수로 추출

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (parts.isEmpty()) {
                    // 파트가 없을 때: 안내 문구 표시
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "아직 생성된 파트가 없어요 😢",
                            style = Typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Gray400
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "우측 하단의 버튼을 눌러\n첫 번째 파트를 추가해보세요!",
                            style = Typography.bodyMedium.copy(color = Gray400),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // 파트 리스트 (드래그 가능)
                    val partListState = rememberLazyListState()
                    val partReorderState = rememberReorderableLazyListState(partListState) { from, to ->
                        val fromId = from.key as? String
                        val toId = to.key as? String
                        if (fromId != null && toId != null) {
                            viewModel.reorderParts(fromId, toId)
                        }
                    }

                    LazyColumn(
                        state = partListState,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items = uiState.song?.parts ?: emptyList(), key = { part -> part.id }) { part ->
                            ReorderableItem(partReorderState, key = part.id) { isDragging ->
                                val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "partElev")

                                // 파트 아이템 (카드)
                                PartItem(
                                    part = part,
                                    elevation = elevation,
                                    onAddChordClick = { showAddChordDialogForPartId = part.id },
                                    onDeletePartClick = { partToDelete = part },
                                    onReorderChord = { from, to -> viewModel.reorderChords(part.id, from, to) },
                                    onEditPartClick = { editingPart = part }, // 파트 수정 다이얼로그 호출
                                    onEditChordClick = { chord ->
                                        // 현재 파트의 ID와 클릭된 코드 객체를 쌍으로 저장 -> 수정 다이얼로그 열림
                                        editingChordData = part.id to chord
                                    },
                                    modifier = Modifier.draggableHandle() // 파트 드래그 핸들
                                )
                            }
                        }
                    }
                }
            }

            // 노래 수정 다이얼로그
            if (showEditInfoDialog) {
                EditSongInfoDialog(
                    initialTitle = song.title,
                    initialArtist = song.artist,
                    initialBpm = song.bpm,
                    initialCapo = song.capo,
                    initialTuning = song.tuning,
                    initialYoutubeLink = song.youtubeLink,
                    onDismiss = { showEditInfoDialog = false },
                    onConfirm = { t, a, b, c, tu, link ->
                        viewModel.updateSongInfo(t, a, b, c, tu, link)
                        showEditInfoDialog = false
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp), // 전체 여백
            verticalAlignment = Alignment.CenterVertically, // 세로 중앙 정렬
            horizontalArrangement = Arrangement.spacedBy(16.dp) // 버튼 사이 간격
        ) {
            // 유튜브 버튼
            val context = LocalContext.current
            val hasYoutubeLink = song.youtubeLink.isNotBlank() // 유튜브 링크가 있다면 true
            val youtubeColor = if (hasYoutubeLink) Color(0xFFea0034) else Gray400
            // 유튜브 버튼 애니메이션 상태 준비
            val ytInteractionSource = remember { MutableInteractionSource() }
            val ytIsPressed by ytInteractionSource.collectIsPressedAsState()
            val ytScale by animateFloatAsState(
                targetValue = if (ytIsPressed) 0.92f else 1f,
                label = "yt_scale"
            )

            Box(
                modifier = Modifier
                    .scale(ytScale) // 크기 애니메이션
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(youtubeColor)
                    .clickable(
                        interactionSource = ytInteractionSource,
                        indication = null, // 물결 제거
                        onClick = {
                            if (hasYoutubeLink) {
                                // 링크 연결됨 -> 유튜브 실행
                                try {
                                    val intent =
                                        Intent(Intent.ACTION_VIEW, song.youtubeLink.toUri())
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "링크를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // 링크 없음 -> 안내 및 수정 창 열기
                                Toast.makeText(context, "유튜브 링크를 설정해주세요.", Toast.LENGTH_SHORT).show()
                                showEditInfoDialog = true
                            }
                        }
                    )
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.youtube_activity),
                    contentDescription = "YouTube Activity",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 메트로놈 버튼
            val metronomeColor = if (hasValidBpm) {
                if(isMetronomePlaying) Color(0xFFFF9B00) else Color(0xFF5E6C84) // 재생중: 파랑, 정지(설정됨): 진한 회색
            } else {
                Gray400 // 설정 안됨: 연한 회색
            }

            val metronomeInteractionSource = remember { MutableInteractionSource() }
            val metronomeIsPressed by metronomeInteractionSource.collectIsPressedAsState()
            val metronomeScale by animateFloatAsState(
                targetValue = if (metronomeIsPressed) 0.92f else 1f,
                label = "metronome_scale"
            )

            Box(
                modifier = Modifier
                    .scale(metronomeScale)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(metronomeColor)
                    .clickable(
                        interactionSource = metronomeInteractionSource,
                        indication = null,
                        onClick = {
                            if (hasValidBpm) {
                                // BPM 설정됨 -> 재생 토글
                                isMetronomePlaying = !isMetronomePlaying
                                if (isMetronomePlaying) {
                                    Toast.makeText(context, "메트로놈 시작! ($bpmValue BPM)", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "메트로놈 정지!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // BPM 설정 안됨 -> 안내 및 다이얼로그 오픈
                                Toast.makeText(context, "BPM을 설정해주세요.", Toast.LENGTH_SHORT).show()
                                showEditInfoDialog = true
                            }
                        }
                    )
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.metronome),
                    contentDescription = "Metronome",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 파트 추가 버튼
            // 파트 추가 버튼 애니메이션 상태 준비
            val addInteractionSource = remember { MutableInteractionSource() }
            val addIsPressed by addInteractionSource.collectIsPressedAsState()
            val addScale by animateFloatAsState(
                targetValue = if (addIsPressed) 0.96f else 1f,
                label = "add_scale"
            )

            Box(
                modifier = Modifier
                    .scale(addScale)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(TossBlue)
                    .clickable(
                        interactionSource = addInteractionSource,
                        indication = null,
                        onClick = { showAddPartDialog = true }
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "파트 추가",
                        style = Typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }

    // 파트 추가 다이얼로그
    if (showAddPartDialog) {
        AddPartDialog(
            title = "새로운 파트 추가",
            placeholder = "예: Chorus, Verse 1",
            existingPartNames = uiState.song?.parts?.map { it.name } ?: emptyList(),
            onDismiss = { showAddPartDialog = false },
            onConfirm = { name ->
                viewModel.addPart(name)
                showAddPartDialog = false
            }
        )
    }

    // 파트 수정 다이얼로그 (이름 & 메모)
    if (editingPart != null) {
        EditPartDialog(
            initialName = editingPart!!.name,
            initialMemo = editingPart!!.memo,
            onDismiss = { editingPart = null },
            onConfirm = { newName, newMemo ->
                viewModel.updatePartInfo(editingPart!!.id, newName, newMemo)
                editingPart = null
            }
        )
    }

    // 코드 추가 다이얼로그
    if (showAddChordDialogForPartId != null) {
        ChordEditorDialog(
            title = "코드 생성",
            confirmButtonText = "코드 추가",
            onDismiss = { showAddChordDialogForPartId = null },
            onConfirm = { name, positions, startFret ->
                viewModel.addChord(
                    partId = showAddChordDialogForPartId!!,
                    chordName = name,
                    positions = positions,
                    startFret = startFret
                )
                // showAddChordDialogForPartId = null // 계속 열어두려면 이 줄 삭제
            },
            onDelete = null // 추가 모드에선 삭제 버튼 없음
        )
    }

    // 코드 수정 다이얼로그
    if (editingChordData != null) {
        val (partId, chord) = editingChordData!!

        ChordEditorDialog(
            title = "코드 수정",          // 제목 변경
            confirmButtonText = "수정 완료", // 버튼 텍스트 변경

            // [중요] 기존 코드의 정보를 초기값으로 전달
            initialName = chord.name,
            initialPositions = chord.positions,
            initialStartFret = chord.startFret,

            onDismiss = { editingChordData = null },
            onConfirm = { newName, newPositions, newStartFret ->
                // 뷰모델의 업데이트 함수 호출
                viewModel.updateChord(partId, chord.id, newName, newPositions, newStartFret)
                editingChordData = null // 다이얼로그 닫기
            },
            onDelete = {
                viewModel.deleteChord(partId, chord.id)
                editingChordData = null
            }
        )
    }

    // 파트 삭제 다이얼로그
    if (partToDelete != null) {
        DeleteDialog(
            title = "파트를 삭제할까요?",
            description = "'${partToDelete!!.name}' 파트가 삭제됩니다.\n안에 포함된 코드들도 함께 사라집니다.",
            confirmText = "삭제",
            onDismiss = { partToDelete = null }, // 취소 시 초기화
            onConfirm = {
                viewModel.deletePart(partToDelete!!.id) // 실제 삭제 수행
                partToDelete = null // 다이얼로그 닫기
            }
        )
    }
}


@Composable
fun PartItem(
    part: SongPart,
    elevation: androidx.compose.ui.unit.Dp,
    onAddChordClick: () -> Unit,
    onDeletePartClick: () -> Unit,
    onReorderChord: (String, String) -> Unit,
    onEditPartClick: () -> Unit,
    onEditChordClick: (Chord) -> Unit,
    modifier: Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Gray100.copy(alpha = 0.8f)), // 투명한 배경
        elevation = CardDefaults.cardElevation(elevation),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // [파트 헤더] 이름 + 드래그 핸들 + 삭제/추가 버튼
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 드래그 핸들
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "Move Part",
                    tint = Gray400,
                    modifier = modifier
                )
                Spacer(modifier = Modifier.width(8.dp))

                // 파트 이름 및 메모 영역
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onEditPartClick) // 클릭 시 수정 창 호출
                ) {
                    // 파트 이름
                    Text(
                        text = part.name,
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TossBlue)
                    )

                    // 메모가 있으면 표시
                    if (part.memo.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = part.memo,
                            style = Typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = Gray400
                            )
                        )
                    }
                }

                // 코드 추가 버튼 (작은 +)
                IconButton(
                    onClick = onAddChordClick,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Chord", tint = TossBlue, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 파트 삭제 버튼 (X)
                IconButton(
                    onClick = onDeletePartClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Delete Part", tint = Gray400, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            BoxWithConstraints {
                val screenWidth = maxWidth
                val spacing = 8.dp
                val minCellWidth = 140.dp   // 코드 박스 최소 너비 (운지표 공간 확보)
                val cellHeight = 240.dp     // 코드 박스 높이 (운지표 공간 확보)

                // 한 줄에 들어갈 수 있는 개수 계산
                // (화면너비 + 간격) / (최소너비 + 간격)
                val columns = maxOf(1, ((screenWidth + spacing) / (minCellWidth + spacing)).toInt())

                // 필요한 행 개수 계산
                val rows = ceil(part.chords.size.toFloat() / columns).toInt()

                // 전체 그리드 높이 계산 (행 높이 + 간격)
                val gridHeight = if (rows > 0) {
                    (cellHeight * rows) + (spacing * (rows - 1))
                } else {
                    0.dp
                }

                // 빈 파트일 때 안내 문구
                if (part.chords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("코드를 추가해주세요", style = Typography.bodySmall.copy(color = Gray400))
                    }
                } else {
                        val chordGridState = rememberLazyGridState()
                        val chordReorderState = rememberReorderableLazyGridState(chordGridState) { from, to ->
                            val fromId = from.key as? String
                            val toId = to.key as? String
                            if (fromId != null && toId != null) {
                                onReorderChord(fromId, toId)
                            }
                        }
                    LazyVerticalGrid(
                        state = chordGridState,
                        columns = GridCells.Fixed(columns), // 계산된 컬럼 수 고정
                        verticalArrangement = Arrangement.spacedBy(spacing),
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                        userScrollEnabled = false, // 중요: 내부 스크롤을 꺼야 외부 리스트(LazyColumn)가 스크롤 됨
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridHeight) // 중요: 계산된 높이만큼 강제로 늘림
                    ) {
                        items(part.chords, key = { it.id }) { chord ->
                            ReorderableItem(chordReorderState, key = chord.id) { isDragging ->
                                val chordElevation by animateDpAsState(
                                    if (isDragging) 8.dp else 0.dp,
                                    label = "chord"
                                )

                                ChordChip(
                                    name = chord.name,
                                    positions = chord.positions,
                                    startFret = chord.startFret,
                                    elevation = chordElevation,
                                    onClick = { onEditChordClick(chord) },
                                    modifier = Modifier.longPressDraggableHandle(true)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChordChip(
    name: String,
    positions: List<Int>,   // 상대 좌표
    startFret: Int,
    elevation: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "chord_scale"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = elevation,
        modifier = modifier
            .scale(scale)
            .height(240.dp) // 이 높이 안에 운지표가 그려짐
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단: 코드 이름
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Gray100.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name,
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Gray900)
                )
            }

            // 하단: 운지표 들어갈 공간 (Placeholder)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // ChordChip용 작은 사이즈 파라미터 적용
                InteractiveFretboard(
                    modifier = Modifier.fillMaxSize(),  // InteractiveFretboard는 modifier로 크기를 제어받는다
                    positions = positions,   // 변환된 상대 좌표
                    startFret = startFret,   // 계산된 시작 프렛
                    isInteractive = false, // 보기 전용 모드

                    // 디자인 최적화
                    boardPadding = 7.dp,
                    dotRadius = 8.dp,
                    lineThickness = 1.5.dp,
                    markerThickness = 2.dp,
                    nutThickness = 4.dp,
                    fretLabelFontSize = 18.sp
                )
            }
        }
    }
}


// ---------------------------
// 작은 정보 배지 컴포넌트
// ---------------------------
@Composable
fun InfoBadge(label: String, value: String, containerColor: Color = Gray100, contentColor: Color = Gray900) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(6.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = Typography.labelSmall.copy(color = Gray400, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.width(4.dp))
            Text(value, style = Typography.labelSmall.copy(color = contentColor, fontWeight = FontWeight.Bold))}
    }
}