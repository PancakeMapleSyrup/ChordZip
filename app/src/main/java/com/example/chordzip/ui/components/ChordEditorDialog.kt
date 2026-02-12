package com.example.chordzip.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import com.example.chordzip.ui.theme.*
import com.example.chordzip.data.ChordLibrary
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput

// TODO: ChordEditorDialog에서 코드 추가를 눌렀을 때 창이 자동으로 닫히는 로직이 어딨는지 찾아야됨.
// TODO: EDITOR에서 사용자가 원래 저장된 코드와 이름이 겹치는 코드를 저장했을 때 사용자 지정 코드가 원래 코드를 덮어서 나와야 하는데 그러지 못하고 있음. 해결해야 함.
// TODO: LIBRARY에서 저장된 코드를 삭제할 수 있는 기능을 추가해야 함.
// TODO:

// 화면 상태 정의 (에디터, 보관함)
enum class EditorScreenState { EDITOR, LIBRARY }

@Composable
fun ChordEditorDialog(
    title: String = "코드 생성",
    confirmButtonText: String = "코드 추가",
    initialName: String = "",
    initialPositions: List<Int> = listOf(-1, -1, -1, -1, -1, -1),
    initialStartFret: Int = 1,
    onDismiss: () -> Unit,
    onConfirm: (String, List<Int>, Int) -> Unit,
    onDelete : (() -> Unit)? = null // 삭제 버튼 (null이면 버튼 숨김)
) {
    // 텍스트 필드에 들어갈 코드 이름 (예: "Cm7(b5)")
    var chordNameInput by remember { mutableStateOf(initialName) }
    // 운지표 (기본값은 빈 상태)
    var fretPositions by remember { mutableStateOf(initialPositions) }
    var startFret by remember { mutableIntStateOf(initialStartFret) }
    // 코드 자동 완성 모드 상태 (기본값: 꺼짐)
    var isAutoMode by remember { mutableStateOf(false) }
    // 화면 전환 상태 (0: 에디터, 1: 라이브러리)
    var currentScreen by remember { mutableStateOf(EditorScreenState.EDITOR) }

    // 텍스트가 바뀔 때마다, 알고 있는 코드라면 운지표를 자동으로 업데이트 (편의 기능)
    LaunchedEffect(chordNameInput) {
        // 자동 모드가 꺼져 있으면 실행 안 함
        if (!isAutoMode) return@LaunchedEffect

        // 입력된 이름이 초기값과 다를 때만 표준 운지법을 불러옴
        // (수정 모드에서 열자마자 내가 커스텀한 운지가 초기화되는 것 방지)
        if (chordNameInput != initialName) {
            val foundChord = ChordLibrary.findVoicing(chordNameInput)

            // 유효한 코드인지 확인 (전부 -1인 빈 코드가 아닌 경우)
            if (foundChord.positions.count { it == -1 } != 6) {
                fretPositions = foundChord.positions
                startFret = foundChord.startFret // 저장된 startFret도 같이 불러옴
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)  // 화면을 넓게 쓰기 위함
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(670.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            // 애니메이션 컨텐츠
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    if (targetState == EditorScreenState.LIBRARY) {
                        // 오른쪽(라이브러리)으로 이동: 오른쪽에서 등장, 왼쪽으로 퇴장
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        // 왼쪽(에디터)으로 이동: 왼쪽에서 등장, 오른쪽으로 퇴장
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "ScreenTransition",
                modifier = Modifier.fillMaxSize()
            ) { screen ->
                when (screen) {
                    EditorScreenState.EDITOR -> {
                        // 메인 에디터 화면
                        ChordEditorMainContent(
                            title = title,
                            chordNameInput = chordNameInput,
                            onChordNameChange = { chordNameInput = it },
                            fretPositions = fretPositions,
                            onFretPositionsChange = { fretPositions = it },
                            startFret = startFret,
                            onStartFretChange = { startFret = it },
                            isAutoMode = isAutoMode,
                            onAutoModeChange = { isAutoMode = it },
                            onNavigateToLibrary = { currentScreen = EditorScreenState.LIBRARY }, // 이동 트리거
                            onDismiss = onDismiss,
                            onConfirm = onConfirm,
                            onDelete = onDelete,
                            confirmButtonText = confirmButtonText
                        )
                    }
                    EditorScreenState.LIBRARY -> {
                        // 사용자 지정 코드 관리 화면
                        ChordLibraryContent(
                            onBack = { currentScreen = EditorScreenState.EDITOR },
                            onChordSelected = { selectedChord, isQuickAdd ->
                                if (isQuickAdd) {
                                    // 바로 추가 모드: 즉시 confirm 호출 후 종료
                                    onConfirm(selectedChord.name, selectedChord.positions, selectedChord.startFret)
                                } else {
                                    // 일반 모드: 데이터를 에디터로 불러오고 화면 전환
                                    chordNameInput = selectedChord.name
                                    fretPositions = selectedChord.positions
                                    startFret = selectedChord.startFret
                                    currentScreen = EditorScreenState.EDITOR
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// 메인 에디터 화면 컴포넌트
@Composable
fun ChordEditorMainContent(
    title: String,
    chordNameInput: String,
    onChordNameChange: (String) -> Unit,
    fretPositions: List<Int>,
    onFretPositionsChange: (List<Int>) -> Unit,
    startFret: Int,
    onStartFretChange: (Int) -> Unit,
    isAutoMode: Boolean,
    onAutoModeChange: (Boolean) -> Unit,
    onNavigateToLibrary: () -> Unit, // 보관함으로 이동하는 콜백
    onDismiss: () -> Unit,
    onConfirm: (String, List<Int>, Int) -> Unit,
    onDelete: (() -> Unit)?,
    confirmButtonText: String
) {
    val context = androidx.compose.ui.platform.LocalContext.current // 토스트 메시지용
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .swipeDetector(
                onSwipeLeft = { onNavigateToLibrary() }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 헤더 (제목 + 닫기)
            Text(
                text = title,
                style = Typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = Gray900
            )

            // 보관함으로 이동하는 화살표 버튼
            TextButton(
                onClick = onNavigateToLibrary,
                colors = ButtonDefaults.textButtonColors(contentColor = Gray400)
            ) {
                Text(
                    text = "코드 라이브러리",
                    fontWeight = FontWeight.SemiBold,
                    color = Gray400
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Go to Library"
                )
            }
        }

        // 메인 컨텐츠 (좌: 선택기, 우: 운지표)
        Row(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()) {
            // 좌측 코드 선택 패널
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .padding(end = 24.dp),
                verticalArrangement = Arrangement.Center
            ){
                //  사용자 직접 입력 칸
                SimpleTextField(
                    value = chordNameInput,
                    onValueChange = onChordNameChange,
                    placeholder = "직접 입력 또는 아래 버튼 클릭"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 근음 (C D E F G A B)
                Text("Root", style = Typography.labelMedium, color = Gray400)
                Spacer(modifier = Modifier.height(8.dp))
                val roots = listOf("C", "D", "E", "F", "G", "A", "B")
                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(roots) { root ->
                        InputChip(label = root, onClick = { onChordNameChange(chordNameInput + root) }, backgroundColor = Color.White)
                    }
                    // 지우기 버튼
                    item {
                        InputChip(
                            label = "⌫",
                            onClick = { onChordNameChange("") },
                            backgroundColor = Gray100, // 약간 더 어두운 색으로 강조
                            contentColor = Color(0xFFFF3553)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Accidental (#, b)
                Text("Accidental", style = Typography.labelMedium, color = Gray400)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(4.dp)
                ) {
                    InputChip(
                        label = "#",
                        onClick = { onChordNameChange(chordNameInput + "#") },
                        modifier = Modifier.weight(1f),
                        backgroundColor = Color(0xFFEEF2F9)
                    )
                    InputChip(
                        label = "b",
                        onClick = { onChordNameChange(chordNameInput + "b") },
                        modifier = Modifier.weight(1f),
                        backgroundColor = Color(0xFFEEF2F9)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quality & Number
                Text("Quality", style = Typography.labelMedium, color = Gray400)
                Spacer(modifier = Modifier.height(8.dp))

                // 숫자 줄 (2, 3, 4, 5, 6, 7, 9, 11)
                val numbers = listOf("2", "3", "4", "5", "6", "7", "9", "11")
                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(numbers) { num ->
                        InputChip(
                            label = num,
                            onClick = { onChordNameChange(chordNameInput + num) },
                            fontSize = 12.sp,
                            backgroundColor = Color(0xFFEEF2F9)
                        )
                    }
                }

                // 기호 줄 (M, m, sus, add, dim, aug, (, ), /)
                val symbols = listOf("M", "m", "sus", "add", "dim", "aug", "(", ")", "/")
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(symbols) { sym ->
                        InputChip(
                            label = sym,
                            onClick = { onChordNameChange(chordNameInput + sym) },
                            fontSize = 12.sp,
                            backgroundColor = Color(0xFFE8F3FF)
                        )
                    }
                }
            }

            // 구분선
            VerticalDivider(color = Gray100, modifier = Modifier.padding(horizontal = 8.dp))

            // 우측 운지표 프리뷰
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center // 카드 수직 중앙 정렬
            ) {
                // 토글 스위치 영역 (우측 정렬)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp, end = 40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End // 오른쪽 끝으로 보냄
                ) {
                    // 코드 저장 버튼
                    SaveChordButton(
                        onSaveClick = {
                            if (chordNameInput.isNotBlank()) {
                                // 라이브러리에 저장 로직
                                ChordLibrary.addCustomChord(chordNameInput, startFret, fretPositions)

                                // 토스트 메시지
                                android.widget.Toast.makeText(context, "$chordNameInput 코드가 저장되었습니다!", android.widget.Toast.LENGTH_SHORT).show()

                                true // 성공했음을 알려줘서 버튼 색을 바꿈
                            } else {
                                android.widget.Toast.makeText(context, "코드 이름을 입력해주세요.", android.widget.Toast.LENGTH_SHORT).show()
                                false // 실패했으므로 버튼 색 유지
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp)) // 버튼 사이 간격

                    // 자동 완성 토글
                    Text(
                        text = "자동 완성",
                        style = Typography.labelMedium,
                        color = if(isAutoMode) TossBlue else Gray400, // 켜지면 파란색 글씨
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    AutoModeToggle(
                        checked = isAutoMode,
                        onCheckedChange = onAutoModeChange
                    )
                }

                // 운지표 카드
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // 그림자 효과
                    modifier = Modifier
                        .width(240.dp) // 캔버스(160dp)보다 약간 넓게
                        .wrapContentHeight() // 내용물 크기에 맞게 높이 조절
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 상단: 코드 이름 (회색 타이틀 바)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .background(Gray100.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chordNameInput.ifBlank { "..." },
                                style = Typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Gray900,
                                    fontSize = 18.sp
                                )
                            )
                        }

                        // 하단: 운지표 (Interactive Canvas)
                        Box(
                            modifier = Modifier.padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            InteractiveFretboard(
                                modifier = Modifier.size(200.dp, 300.dp),
                                positions = fretPositions,
                                onPositionChanged = onFretPositionsChange,
                                startFret = startFret,
                                onStartFretChanged = onStartFretChange
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "운지표를 터치해 직접 수정할 수 있어요",
                    style = Typography.bodySmall.copy(color = Gray400),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 하단 버튼 영역
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically, // 세로 중앙 정렬
            modifier = Modifier.fillMaxWidth()
        ) {
            // onDelete가 null이 아닐 때만 삭제 버튼 표시
            if (onDelete != null) {
                TextButton(
                    onClick = onDelete
                ) {
                    Text("삭제", color = Color(0xFFFF3B30)) // 빨간색으로 경고 느낌
                }
                Spacer(modifier = Modifier.width(4.dp)) // 버튼 사이 약간의 간격
            }

            TextButton(onClick = onDismiss) {
                Text("취소", color = Gray400)
            }
            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { onConfirm(chordNameInput, fretPositions, startFret) },
                colors = ButtonDefaults.buttonColors(containerColor = TossBlue),
                shape = RoundedCornerShape(14.dp),
                enabled = chordNameInput.isNotBlank() // 이름 없으면 버튼 비활성
            ) {
                Text(confirmButtonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 애니메이션이 있는 저장 버튼
@Composable
fun SaveChordButton(
    onSaveClick: () -> Boolean
) {
    // 저장 완료 상태 관리 (true면 파란색 변신)
    var isSaved by remember { mutableStateOf(false) }
    // 상태가 변하면 1.5초 뒤에 다시 원래대로 복귀
    LaunchedEffect(isSaved) {
        if (isSaved) {
            kotlinx.coroutines.delay(1500) // 1.5초 유지
            isSaved = false
        }
    }
    // 색상 애니메이션
    val backgroundColor by animateColorAsState(
        targetValue = if (isSaved) TossBlue else Gray100,
        label = "saveBg",
        animationSpec = tween(durationMillis = 300)
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSaved) Color.White else Gray400,
        label = "saveContent",
        animationSpec = tween(durationMillis = 300)
    )
    // 클릭 애니메이션
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.9f else 1f, label = "scale")

    // 햅틱 피드백
    val view = LocalView.current

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    // 콜백 실행 후 성공(true)했으면 애니메이션 트리거
                    if (onSaveClick()) {
                        isSaved = true
                    }
                }
            )
            .padding(horizontal = 12.dp, vertical = 6.dp), // 내부 패딩
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Add,
                contentDescription = "Save",
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isSaved) "완료" else "저장",
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
        }
    }
}

// 사용자 지정 코드 라이브러리 화면
@Composable
fun ChordLibraryContent(
    onBack: () -> Unit,
    onChordSelected: (com.example.chordzip.data.ChordVoicing, Boolean) -> Unit   // 선택 콜백 추가
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    // 바로 추가 모드 상태
    var isQuickAddMode by remember { mutableStateOf(false) }

    // 삭제 모드 상태
    var isDeleteMode by remember { mutableStateOf(false) }

    // 리스트 갱신용 트리거 (삭제 후 UI 업데이트를 위해 필요)
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // 모든 코드를 가져와서 근음 별로 그룹화합니다.
    val groupedChords = remember(refreshTrigger) {
        // C, C#, D... 순서대로 정렬하기 위한 기준 리스트
        val rootOrder = listOf("C", "C#", "D", "Eb", "D#", "E", "F", "F#", "G", "G#", "A", "Bb", "A#", "B")

        // ChordLibrary의 모든 코드를 가져옴 (여기서는 예시로 getChordsByRoot를 루프 돌리거나, 전체 맵을 접근)
        // 만약 ChordLibrary.standardChords가 private라면, public fun getAll(): List<ChordVoicing> { return standardChords.values.toList() } 를 ChordLibrary에 추가해주세요.
        // 현재는 편의상 rootOrder를 순회하며 수집합니다.
        rootOrder.mapNotNull { root ->
            val chords = ChordLibrary.getChordsByRoot(root)
            if (chords.isNotEmpty()) root to chords else null
        }
    }

    // 스크롤 제어를 위한 상태
    val listState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    // 각 근음이 그리드에서 몇 번째 인덱스에 위치하는지 계산 (스크롤 이동용)
    val scrollIndices = remember(groupedChords) {
        val map = mutableMapOf<String, Int>()
        var currentIndex = 0
        groupedChords.forEach { (root, list) ->
            // 근음의 첫 글자만 키로 저장 (C# -> C)
            // 이미 C가 저장되어 있는데 C#이 오면? -> 덮어쓰지 않고 C를 누르면 C로 가고, C#은 스크롤로만 접근
            val key = root.first().toString()
            if (!map.containsKey(key)) {
                map[key] = currentIndex
            }

            // 인덱스 증가: 헤더(1개) + 아이템 개수(list.size)
            currentIndex += 1 + list.size
        }
        map
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .swipeDetector(
                onSwipeRight = { onBack() }
            )
    ) {
        // 헤더 영역 (제목 + 빠른 이동 탭)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 왼쪽: 제목 및 뒤로가기
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 뒤로가기 버튼
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = Gray900
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "코드 라이브러리",
                    style = Typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = Gray900
                )
            }

            // 오른쪽: [삭제] + [바로 추가 토글] + [알파벳 탭]
            // 화면이 좁을 수 있으므로 Row 안에 배치하되, 탭은 스크롤 가능하게(LazyRow) 처리
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.weight(1f)  // 남은 공간 차지
            ) {
                // 삭제 텍스트/아이콘 색상 애니메이션
                val deleteContentColor by animateColorAsState(
                    targetValue = if (isDeleteMode) Color(0xFFFF3553) else Gray400,
                    label = "delContentColor",
                    animationSpec = tween(300)
                )
                // 배경 색상 애니메이션 (누르는 범위 표시)
                val deleteBackgroundColor by animateColorAsState(
                    targetValue = if (isDeleteMode) Color(0xFFFF3553).copy(alpha = 0.1f) else Gray100,
                    label = "delBgColor",
                    animationSpec = tween(300)
                )
                // 삭제 모드 버튼
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(deleteBackgroundColor)
                        .clickable {
                            isDeleteMode = !isDeleteMode
                            if (isDeleteMode) isQuickAddMode = false // 삭제모드가 켜지면 바로추가는 꺼짐
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Mode",
                        tint = deleteContentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "삭제",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = deleteContentColor,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 코드 바로 추가 토글
                // 텍스트/아이콘 색상 애니메이션
                val quickAddContentColor by animateColorAsState(
                    targetValue = if (isQuickAddMode) TossBlue else Gray400,
                    label = "quickAddContentColor",
                    animationSpec = tween(300)
                )

                // 배경 색상 애니메이션
                val quickAddBackgroundColor by animateColorAsState(
                    targetValue = if (isQuickAddMode) TossBlue.copy(alpha = 0.1f) else Gray100,
                    label = "quickAddBgColor",
                    animationSpec = tween(300)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(quickAddBackgroundColor) // 배경색 적용
                        .clickable {
                            isQuickAddMode = !isQuickAddMode
                            if (isQuickAddMode) isDeleteMode = false // 바로추가가 켜지면 삭제모드는 꺼짐
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp) // 삭제 버튼과 동일한 패딩
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn, // 또는 Icons.Default.Check
                        contentDescription = "Quick Add Mode",
                        tint = quickAddContentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "바로 추가",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = quickAddContentColor,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))
            }

            // 오른쪽: 빠른 이동 탭 (C D E F G A B)
            val tabs = listOf("C", "D", "E", "F", "G", "A", "B")
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(tabs.size) { index ->
                    val tab = tabs[index]
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent) // 배경색 없음
                            .border(2.dp, Gray100, CircleShape) // 테두리 살짝
                            .clickable {
                                val targetIndex = scrollIndices[tab]
                                if (targetIndex != null) {
                                    coroutineScope.launch {
                                        val currentIndex = listState.firstVisibleItemIndex
                                        val distance = kotlin.math.abs(currentIndex - targetIndex)

                                        // 거리가 10개 아이템 이상 차이나면?
                                        // -> 목표 지점 바로 근처(5칸 앞)로 '순간 이동' 먼저 시킴
                                        if (distance > 10) {
                                            val snapIndex = if (targetIndex > currentIndex) {
                                                targetIndex - 5 // 목표보다 위쪽 5칸 앞으로 이동
                                            } else {
                                                targetIndex + 5 // 목표보다 아래쪽 5칸 뒤로 이동
                                            }
                                            // 안전하게 범위 체크 후 순간이동
                                            if (snapIndex in 0 until listState.layoutInfo.totalItemsCount) {
                                                listState.scrollToItem(snapIndex)
                                            }
                                        }

                                        // 그 다음, 남은 짧은 거리를 부드럽게 스르륵 이동
                                        listState.animateScrollToItem(targetIndex)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Gray900
                        )
                    }
                }
            }
        }

        // 메인 그리드 (헤더 + 코드들)
        LazyVerticalGrid(
            columns = GridCells.Fixed(6), // 3열 그리드
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.weight(1f)
        ) {
            groupedChords.forEach { (root, chords) ->
                // C코드, C#코드 ...
                item(span = { GridItemSpan(6) }) { // 3칸을 모두 차지하도록 설정
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp)
                    ) {
                        Text(
                            text = "$root 코드",
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Gray900
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(thickness = 1.dp, color = Gray100) // 구분선
                    }
                }

                items(
                    items = chords,
                    key = { it.name } // 각 코드의 이름을 키로 설정
                ) { chord ->
                    ChordOptionCard(
                        chord = chord,
                        isQuickAdd = isQuickAddMode,
                        isDelete = isDeleteMode,
                        onClick = {
                            if (isDeleteMode) {
                                // 삭제 모드일 때
                                val success = ChordLibrary.removeChord(chord.name)
                                if (success) {
                                    android.widget.Toast.makeText(context, "${chord.name} 코드가 삭제되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
                                    refreshTrigger++    // UI 갱신
                                }
                            } else {
                                onChordSelected(chord, isQuickAddMode)
                            }
                        }
                    )
                }
            }

            // 데이터가 없을 경우 처리
            if(groupedChords.isEmpty()) {
                item(span = { GridItemSpan(6) }) {
                    Box(modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("표시할 코드가 없습니다.", color = Gray400)
                    }
                }
            }
        }
    }
}

@Composable
fun InputChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    backgroundColor: Color = Gray100, // 배경색 커스텀 가능하게 추가
    contentColor: Color = Gray900
) {
    // 햅틱 피드백을 위해 현재 View 가져오기
    val view = LocalView.current

    Card(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) // 탭 피드백
            onClick()
        },
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // 그림자 추가
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = label,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

// ChordChip 스타일을 적용한 라이브러리용 카드
@Composable
fun ChordOptionCard(
    chord: com.example.chordzip.data.ChordVoicing,
    isQuickAdd: Boolean, // 바로 추가 모드 여부
    isDelete: Boolean,  // 삭제 모드 여부
    onClick: () -> Unit
) {
    // 저장 완료(클릭 피드백) 상태 관리
    var isAdded by remember { mutableStateOf(false) }
    // 상태 복귀 타이머 (1.5초 후 원래 색으로 복귀)
    LaunchedEffect(isAdded) {
        if (isAdded) {
            kotlinx.coroutines.delay(1500) // 1.5초 유지
            isAdded = false
        }
    }

    // 클릭 애니메이션 상태 설정 (ChordChip과 동일)
    val interactionSource = remember { MutableInteractionSource() }
    val view = LocalView.current

    val headerBackgroundColor by animateColorAsState(
        targetValue = when {
            isAdded -> TossBlue
            isDelete -> Color(0xFFFF3553).copy(alpha = 0.1f)
            isQuickAdd -> TossBlue.copy(alpha = 0.1f)
            else -> Gray100
        },
        animationSpec = tween(300),
        label = "headerBg"
    )

    val headerContentColor by animateColorAsState(
        targetValue = when{
            isAdded -> Color.White
            isDelete -> Color(0xFFFF3553)
            isQuickAdd -> TossBlue
            else -> Gray900
        },
        animationSpec = tween(300),
        label = "headerContent"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp, // 라이브러리 목록이므로 그림자는 살짝 낮게 설정
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // 그리드에 맞게 높이 조정 (160dp는 너무 짧아서 200dp로 바꿈)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) // 햅틱 피드백 추가
                    onClick()
                    // 바로 추가 모드일 때만 애니메이션 트리거
                    if (isQuickAdd) {
                        isAdded = true
                    }
                }
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // [상단] 코드 이름 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(headerBackgroundColor),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isAdded) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Added",
                        tint = headerContentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = chord.name,
                    style = Typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = headerContentColor,
                        fontSize = 16.sp
                    ),
                    maxLines = 1
                )
            }

            // [하단] 운지표 영역
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                InteractiveFretboard(
                    modifier = Modifier.fillMaxSize(),
                    positions = chord.positions,
                    startFret = chord.startFret,
                    isInteractive = false, // 읽기 전용
                    boardPadding = 4.dp,
                    dotRadius = 6.dp,        // 점 크기
                    lineThickness = 1.dp,    // 선 두께
                    nutThickness = 2.5.dp,     // 너트 두께
                    markerThickness = 2.dp,// X, O 마커 두께
                    markerPaddingTop = 10.dp,
                    fretLabelFontSize = 12.sp
                )
            }
        }
    }
}

// 자동 모드 토글 스위치
@Composable
fun AutoModeToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. 크기 정의 (조금 더 큼직하게)
    val width = 52.dp
    val height = 32.dp
    val thumbSize = 27.dp
    val padding = 2.5.dp // 테두리와의 간격

    // 2. 애니메이션 (빠르고 쫀득하게: FastOutSlowInEasing 사용)
    val thumbPosition by animateDpAsState(
        targetValue = if (checked) width - thumbSize - padding else padding,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "ThumbPosition"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) TossBlue else Color(0xFFE1E4E8), // 꺼졌을 때 너무 어둡지 않은 회색
        animationSpec = tween(durationMillis = 250),
        label = "TrackColor"
    )

    // 3. 토글 트랙 (배경)
    Box(
        modifier = modifier
            .size(width, height)
            .clip(CircleShape)
            .background(trackColor)
            .clickable(
                // 물결 효과(Ripple) 제거하여 깔끔하게
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        // 4. 토글 썸 (움직이는 하얀 원)
        Box(
            modifier = Modifier
                .offset(x = thumbPosition) // x 좌표 이동 애니메이션
                .size(thumbSize)
                .clip(CircleShape)
                .background(Color.White)
                // 하얀 원에만 살짝 그림자를 주어 입체감 부여
                .shadow(elevation = 2.dp, shape = CircleShape)
        )
    }
}

// 스와이프 제스처 감지 Modifier
fun Modifier.swipeDetector(
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null
): Modifier = this.composed {
    val sensitivity = 50f   // 감도 (낮을수록 조금만 움직여도 인식)

    pointerInput(Unit) {
        var totalDrag = 0f
        detectHorizontalDragGestures(
            onDragEnd = {
                // 드래그가 끝났을 때 누적된 거리를 판단
                when {
                    totalDrag < -sensitivity -> onSwipeLeft?.invoke()  // 왼쪽으로 밀었음 (<-)
                    totalDrag > sensitivity -> onSwipeRight?.invoke()  // 오른쪽으로 밀었음 (->)
                }
                totalDrag = 0f
            },
            onHorizontalDrag = { change, dragAmount ->
                change.consume()    // 제스처 소비
                totalDrag += dragAmount
            }
        )
    }
}