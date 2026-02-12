package com.example.chordzip.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chordzip.ui.theme.Gray400
import com.example.chordzip.ui.theme.Gray900
import kotlin.math.roundToInt

private val BOARD_COLOR = Gray900
private val DISABLED_COLOR = Gray400
private val ROOT_COLOR = Color.Red

@Composable
fun InteractiveFretboard(
    modifier: Modifier = Modifier,
    positions: List<Int>,   // 외부에서 받는 운지 상태 (0~5번줄)
    onPositionChanged: (List<Int>) -> Unit = {}, // 상태 변경 시 호출할 콜백
    startFret: Int = 1,   // 시작 프렛 번호 (기본값: 1)
    onStartFretChanged: (Int) -> Unit = {},
    isInteractive : Boolean = true,  // false일 결우 읽기 모드로 전환
    // 디자인 커스텀을 위한 파라미터들 (기본값은 ChordEditorDialog용 사이즈)
    boardPadding: androidx.compose.ui.unit.Dp = 16.dp,
    dotRadius: androidx.compose.ui.unit.Dp = 10.dp,
    lineThickness: androidx.compose.ui.unit.Dp = 2.dp,
    nutThickness: androidx.compose.ui.unit.Dp = 6.dp,
    markerThickness: androidx.compose.ui.unit.Dp = 3.dp,    // 마커 선 두께
    markerPaddingTop: androidx.compose.ui.unit.Dp = 15.dp,   // 마커가 너트 위로 얼마나 떨어질지 결정하는 파라미터 (기본값 15.dp)
    fretLabelFontSize: androidx.compose.ui.unit.TextUnit = 24.sp
) {
    val stringCount = 6 // 줄 개수 6개
    val fretCount = 5   // 프랫 5칸 (너트는 따로 구현되어있음)
    // 텍스트를 그리기 위한 도구(TextMeasurer)
    val textMeasurer = rememberTextMeasurer()
    val view = LocalView.current // 햅틱 피드백용
    // 근음 찾기: 활성화된 줄 중 가장 낮은 줄
    val rootStringIndex = positions.indexOfFirst { it != -1 }   // 0번 인덱스(6번줄)부터 확인해서 처음으로 -1이 아닌 줄을 찾는다.

    // 최신 상태 참조 (클로저 캡처 문제 방지)
    val currentPositions by rememberUpdatedState(positions)
    val currentOnChanged by rememberUpdatedState(onPositionChanged)
    val currentStartFret by rememberUpdatedState(startFret)
    val currentOnStartFretChanged by rememberUpdatedState(onStartFretChanged)

    val paddingPx = with(LocalDensity.current) { boardPadding.toPx() }
    val dotRadiusPx = with(LocalDensity.current) { dotRadius.toPx() }
    val lineThicknessPx = with(LocalDensity.current) { lineThickness.toPx() }
    val nutThicknessPx = with(LocalDensity.current) { nutThickness.toPx() }
    val markerThicknessPx = with(LocalDensity.current) { markerThickness.toPx() }
    val markerPaddingTopPx = with(LocalDensity.current) { markerPaddingTop.toPx() }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                if (!isInteractive) return@pointerInput // 읽기 모드(false)면 터치 감지 로직을 아예 실행하지 않음
                detectTapGestures { tapOffset ->
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

                    val width = size.width
                    val height = size.height
                    val showLabel = isInteractive || currentStartFret > 1   // 라벨 표시 여부 결정. 인터랙티브 모드이거나, 시작 프렛이 1보다 크면 라벨을 보여줌
                    val fretLabelWidth = if (showLabel) width * 0.15f else 0f
                    val boardStartX = fretLabelWidth + paddingPx
                    val boardEndX = width - paddingPx // 지판이 끝나는 실제 위치
                    val boardWidth = boardEndX - boardStartX
                    val stringSpacing = boardWidth / (stringCount - 1)
                    val fretSpacing = height / (fretCount + 1)
                    val topMargin = fretSpacing * 0.8f
                    val labelCenterY = topMargin + (fretSpacing * 0.5f) // 화살표 영역 좌표 계산

                    // 왼쪽 사이드바 (화살표 영역) 터치
                    if (showLabel && tapOffset.x < fretLabelWidth) {
                        // 위쪽 화살표 영역 (중심 Y보다 위쪽)
                        if (tapOffset.y < labelCenterY) {
                            if (currentStartFret > 1) { // 1보다 클 때만 동작
                                currentOnStartFretChanged(currentStartFret - 1)
                            }
                        } else { // 아래쪽 화살표 영역 (중심 Y보다 아래쪽)
                            if (currentStartFret < 20) { // 20보다 작을 때만 동작
                                currentOnStartFretChanged(currentStartFret + 1)
                            }
                        }
                        return@detectTapGestures    // 화살표를 눌렀다면 지판 로직은 실행 X
                    }

                    // 지판 영역 터치
                    val adjustedX = tapOffset.x - boardStartX
                    // stringSpacing의 절반 정도까지는 오른쪽 여백으로 허용하여 1번줄 터치로 인정
                    if (adjustedX > boardWidth + paddingPx) return@detectTapGestures

                    // 몇 번 줄을 눌렀는지 계산 (반올림하여 가장 가까운 줄 찾기)
                    val clickedStringIndex = (adjustedX / stringSpacing).times(1).let {
                        it.roundToInt()
                    }.coerceIn(0, stringCount - 1)

                    // 현재 상태 변경을 위해 리스트 복사
                    val newPositions = currentPositions.toMutableList()
                    val currentState = newPositions[clickedStringIndex]

                    // 너트 위쪽(상단)을 눌렀는지 확인
                    if (tapOffset.y < topMargin) {
                        // 현재 프렛(1~) 이 눌려있으면 -> O(0)으로 초기화
                        if (currentState == 0) {    // 너트를 눌렀을 때 Open(0)이면 -> Mute(-1)로 변경
                            newPositions[clickedStringIndex] = -1 // Mute
                        } else {    // 너트를 눌렀을 때 Mute(-1)이거나 현재 프렛(1~)이 눌려있으면 -> Open(0)으로 변경
                            newPositions[clickedStringIndex] = 0 // Open
                        }
                    } else {
                        // [하단 터치] 프렛 계산 (1~5)
                        val clickedFret = ((tapOffset.y - topMargin) / fretSpacing).toInt() + 1

                        // 범위를 벗어난 터치 무시 (혹시 모를 버그 방지)
                        if (clickedFret in 1..fretCount) {
                            if (currentState == clickedFret) {
                                // 이미 눌린 곳 다시 클릭 -> Mute(-1)로 변경
                                newPositions[clickedStringIndex] = -1
                            } else {
                                // 새로운 곳 클릭 -> 해당 프렛으로 변경
                                newPositions[clickedStringIndex] = clickedFret
                            }
                        }
                    }
                    // 부모에게 변경된 상태 전달
                    currentOnChanged(newPositions)
                }
            }
    ) { // 그리기 로직
        val width = size.width
        val height = size.height    // --- 캔버스 크기
        val showLabel = isInteractive || startFret > 1
        val fretLabelWidth = if (showLabel) width * 0.15f else 0f  // 왼쪽의 15%는 프렛 번호와 화살표를 위한 공간
        val boardStartX = fretLabelWidth + paddingPx
        val boardEndX = width - paddingPx
        val boardWidth = boardEndX - boardStartX
        val stringSpacing = boardWidth / (stringCount - 1)
        val fretSpacing = height / (fretCount + 1)
        val topMargin = fretSpacing * 0.8f

        // 세로줄 그리기
        for (i in 0 until stringCount) {
            val x = boardStartX + (i * stringSpacing)
            drawLine(
                color = BOARD_COLOR,
                start = Offset(x, topMargin), // 너트 위치에서 시작
                end = Offset(x, topMargin + (fretCount * fretSpacing)), // 마지막 프렛까지
                strokeWidth = lineThicknessPx
            )
        }
        // 가로줄 그리기
        val currentNutThickness = if (startFret == 1) nutThicknessPx else lineThicknessPx
        val nutYOffset = if (startFret == 1) nutThicknessPx / 2 else 0f
        drawLine( // 너트
            color = BOARD_COLOR,
            start = Offset(boardStartX, topMargin + nutYOffset), // Y좌표에 오프셋 추가
            end = Offset(boardEndX, topMargin + nutYOffset),     // Y좌표에 오프셋 추가
            strokeWidth = currentNutThickness
        )
        // 나머지 프렛 바 (1번 ~ 5번)
        for (i in 1..fretCount) {
            val y = topMargin + (i * fretSpacing)
            drawLine(
                color = BOARD_COLOR,
                start = Offset(boardStartX, y),
                end = Offset(boardEndX, y),
                strokeWidth = lineThicknessPx
            )
        }

        if (showLabel) {
            // 프렛 번호와 화살표
            val labelCenterY = topMargin + (fretSpacing * 0.5f) // 첫 칸의 중간 높이
            val labelCenterX = fretLabelWidth / 2               // 왼쪽 영역의 가로 가운데
            // 텍스트 그리기 (숫자)
            val textLayoutResult = textMeasurer.measure(
                text = AnnotatedString(startFret.toString()),
                style = TextStyle(
                    color = BOARD_COLOR,
                    fontSize = fretLabelFontSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
            // 텍스트를 중앙에 배치하기 위한 좌표 계산
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = labelCenterX - (textLayoutResult.size.width / 2),
                    y = labelCenterY - (textLayoutResult.size.height / 2)
                )
            )
            // 화살표는 Interactive = true 일 때만 그림
            if (isInteractive) {
                // 화살표 색상 계산
                val upArrowColor = if (startFret > 1) BOARD_COLOR else DISABLED_COLOR
                val downArrowColor = if (startFret < 20) BOARD_COLOR else DISABLED_COLOR
                val arrowSize = 8.dp.toPx()
                // 위 화살표 (▲) 그리기
                val upArrowY = labelCenterY - (textLayoutResult.size.height / 2) - 12.dp.toPx()
                drawPath(
                    path = Path().apply {
                        moveTo(labelCenterX, upArrowY - arrowSize)
                        lineTo(labelCenterX - arrowSize, upArrowY)
                        lineTo(labelCenterX + arrowSize, upArrowY)
                        close()
                    },
                    color = upArrowColor
                )
                // 아래쪽 화살표 (▼)
                val downArrowY = labelCenterY + (textLayoutResult.size.height / 2) + 12.dp.toPx()
                drawPath(
                    path = Path().apply {
                        moveTo(labelCenterX, downArrowY + arrowSize)
                        lineTo(labelCenterX - arrowSize, downArrowY)
                        lineTo(labelCenterX + arrowSize, downArrowY)
                        close()
                    },
                    color = downArrowColor
                )
            }
        }

        // 마커(점, X, O) 그리기
        val markerY = topMargin - markerPaddingTopPx // 마커 위치 (너트 위)
        val markerRadius = dotRadiusPx

        positions.forEachIndexed { index, state ->
            val x = boardStartX + (index * stringSpacing)
            // 색상 결정: 근음(rootStringIndex)이면 빨간색, 아니면 기본색
            val itemColor = if (index == rootStringIndex) ROOT_COLOR else BOARD_COLOR

            when (state) {
                -1 -> { // X (mute)
                    val xSize = markerRadius
                    drawLine(
                        color = itemColor,
                        start = Offset(x - xSize, markerY - xSize),
                        end = Offset(x + xSize, markerY + xSize),
                        strokeWidth = markerThicknessPx
                    )
                    drawLine(
                        color = itemColor,
                        start = Offset(x + xSize, markerY - xSize),
                        end = Offset(x - xSize, markerY + xSize),
                        strokeWidth = markerThicknessPx
                    )
                }
                0 -> { // O (open)
                    val strokeWidth = markerThicknessPx
                    // 반지름 보정 (테두리가 바깥으로 튀어나가지 않고 안쪽으로 파고듭니다): (원래 반지름) - (두께의 절반)
                    val adjustedRadius = markerRadius - (strokeWidth / 2)
                    drawCircle(
                        color = itemColor,
                        radius = adjustedRadius,
                        center = Offset(x, markerY),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                    )
                }
                else -> { // 1~5 (프렛 누름) -> 점 그리기
                    val y = topMargin + (state * fretSpacing) - (fretSpacing / 2)
                    drawCircle(
                        color = itemColor,
                        radius = dotRadiusPx,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}