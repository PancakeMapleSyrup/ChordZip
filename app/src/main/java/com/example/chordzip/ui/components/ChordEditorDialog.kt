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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.example.chordzip.ui.theme.*
import com.example.chordzip.data.ChordLibrary

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
    // 코드 이름 전체 지우기 로직
    val onClearInput = { chordNameInput = "" }
    // 코드 자동 완성 모드 상태 (기본값: 꺼짐)
    var isAutoMode by remember { mutableStateOf(false) }

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
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
                        color = Gray900,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // 메인 컨텐츠 (좌: 선택기, 우: 운지표)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
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
                            onValueChange = { chordNameInput = it },
                            placeholder = "직접 입력 또는 아래 버튼 클릭"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Root (C D E F G A B)
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
                                InputChip(label = root, onClick = { chordNameInput += root }, backgroundColor = Color.White)
                            }
                            // 지우기 버튼
                            item {
                                InputChip(
                                    label = "⌫",
                                    onClick = onClearInput,
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
                                onClick = { chordNameInput += "#" },
                                modifier = Modifier.weight(1f),
                                backgroundColor = Color(0xFFEEF2F9)
                            )
                            InputChip(
                                label = "b",
                                onClick = { chordNameInput += "b" },
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
                                    onClick = { chordNameInput += num },
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
                                    onClick = { chordNameInput += sym },
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
                            Text(
                                text = "자동 완성",
                                style = Typography.labelMedium,
                                color = if(isAutoMode) TossBlue else Gray400, // 켜지면 파란색 글씨
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            AutoModeToggle(
                                checked = isAutoMode,
                                onCheckedChange = { isAutoMode = it }
                            )
                        }

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
                                        text = chordNameInput.ifBlank { "Chord" },
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
                                        onPositionChanged = { fretPositions = it },
                                        startFret = startFret,
                                        onStartFretChanged = { newFret -> startFret = newFret}
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