package com.example.guitarchordmanager.songdetail

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.guitarchordmanager.ui.components.TextField
import com.example.guitarchordmanager.ui.theme.*
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun SongDetailScreen(
    title: String,
    artist: String,
    viewModel: SongDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // 다이얼로그 상태
    var showAddPartDialog by remember { mutableStateOf(false) }
    var showAddChordDialogForPartId by remember { mutableStateOf<String?>(null) } // 파트 ID 저장

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
                Column {
                    Text(title, style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text(artist, style = Typography.bodyMedium.copy(color = Gray400))
                }
            }

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
                items(uiState.parts, key = { it.id }) { part ->
                    ReorderableItem(partReorderState, key = part.id) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "partElev")

                        // 파트 아이템 (카드)
                        PartItem(
                            part = part,
                            elevation = elevation,
                            onAddChordClick = { showAddChordDialogForPartId = part.id },
                            onDeletePartClick = { viewModel.deletePart(part.id) },
                            onReorderChord = { from, to -> viewModel.reorderChords(part.id, from, to) },
                            onDeleteChord = { chordId -> viewModel.deleteChord(part.id, chordId) },
                            dragModifier = Modifier.draggableHandle() // 파트 드래그 핸들
                        )
                    }
                }
            }
        }

        // 플로팅 버튼 (파트 추가)
        FloatingActionButton(
            onClick = { showAddPartDialog = true },
            containerColor = TossBlue,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Part")
        }

        // --- 다이얼로그들 ---

        // 파트 추가 다이얼로그
        if (showAddPartDialog) {
            SimpleTextInputDialog(
                title = "새로운 파트 추가",
                placeholder = "예: Chorus, Verse 1",
                onDismiss = { showAddPartDialog = false },
                onConfirm = { name ->
                    viewModel.addPart(name)
                    showAddPartDialog = false
                }
            )
        }

        // 코드 추가 다이얼로그 (간단한 입력창 버전)
        if (showAddChordDialogForPartId != null) {
            SimpleTextInputDialog(
                title = "코드 추가",
                placeholder = "예: Am7, G/B",
                onDismiss = { showAddChordDialogForPartId = null },
                onConfirm = { chordName ->
                    viewModel.addChord(showAddChordDialogForPartId!!, chordName)
                    // showAddChordDialogForPartId = null // 계속 열어두려면 이 줄 삭제
                }
            )
        }
    }
}

// ------------------------------------
// 컴포넌트: 개별 파트 카드 (PartItem)
// ------------------------------------
@Composable
fun PartItem(
    part: SongPart,
    elevation: androidx.compose.ui.unit.Dp,
    onAddChordClick: () -> Unit,
    onDeletePartClick: () -> Unit,
    onReorderChord: (String, String) -> Unit,
    onDeleteChord: (String) -> Unit,
    dragModifier: Modifier
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
                    modifier = dragModifier
                )
                Spacer(modifier = Modifier.width(8.dp))

                // 파트 이름
                Text(
                    text = part.name,
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TossBlue),
                    modifier = Modifier.weight(1f)
                )

                // 코드 추가 버튼 (작은 +)
                IconButton(
                    onClick = onAddChordClick,
                    modifier = Modifier.size(32.dp).background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Chord", tint = TossBlue, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 파트 삭제 버튼 (X)
                IconButton(
                    onClick = onDeletePartClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Delete Part", tint = Gray400, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // [코드 그리드] 파트 내부의 코드들
            // 중첩 스크롤 문제를 피하기 위해 높이를 제한하거나 계산해야 합니다.
            // 여기서는 고정 높이를 주어 그리드 내에서 스크롤/드래그가 가능하게 합니다.
            val chordGridState = rememberLazyGridState()
            val chordReorderState = rememberReorderableLazyGridState(chordGridState) { from, to ->
                val fromId = from.key as? String
                val toId = to.key as? String
                if (fromId != null && toId != null) {
                    onReorderChord(fromId, toId)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp, max = 240.dp) // 높이 제한 (내용물에 따라 늘어나되 최대값 설정)
            ) {
                if (part.chords.isEmpty()) {
                    Text(
                        "코드를 추가해주세요",
                        style = Typography.bodySmall.copy(color = Gray400),
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyVerticalGrid(
                        state = chordGridState,
                        columns = GridCells.Adaptive(minSize = 60.dp), // 반응형 그리드
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(part.chords, key = { it.id }) { chord ->
                            ReorderableItem(chordReorderState, key = chord.id) { isDragging ->
                                val chordElevation by animateDpAsState(if (isDragging) 4.dp else 0.dp, label = "chord")

                                // 코드 칩 (Chord Chip)
                                ChordChip(
                                    name = chord.name,
                                    elevation = chordElevation,
                                    // 드래그 핸들을 따로 두지 않고 칩 전체를 길게 눌러 드래그
                                    modifier = Modifier.longPressDraggableHandle(true),
                                    onClick = { /* 코드 수정/삭제 다이얼로그 띄우기 가능 */ }
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
    elevation: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = elevation,
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = name,
                style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Gray900)
            )
        }
    }
}

// ------------------------------------
// 다이얼로그 컴포넌트 (재사용)
// ------------------------------------
@Composable
fun SimpleTextInputDialog(
    title: String,
    placeholder: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = placeholder
            )
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onConfirm(text) },
                colors = ButtonDefaults.buttonColors(containerColor = TossBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Gray400)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
