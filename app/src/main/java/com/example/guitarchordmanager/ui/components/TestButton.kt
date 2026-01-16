package com.example.guitarchordmanager.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.guitarchordmanager.ui.theme.*

@Composable
fun TestButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier, // [핵심] 외부에서 크기(width, height)나 패딩을 조절할 수 있게 열어둠
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp), // 모양도 마음대로
    containerColor: Color = TossBlue,         // 배경색도 마음대로
    disabledContainerColor: Color = Gray100,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit  // [핵심] 텍스트 대신 '내용물'을 받음 (Slot API)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 눌렀을 때 0.96배로 작아지는 애니메이션
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "scale"
    )

    // 활성/비활성 색상 애니메이션
    val backgroundColor by animateColorAsState(
        targetValue = if (enabled) containerColor else disabledContainerColor,
        label = "bg"
    )

    Box(
        modifier = modifier
            .scale(scale) // 애니메이션 적용
            .clip(shape)  // 모양 깎기
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // 물결 효과 제거 (Toss 스타일은 보통 물결 없이 스케일만 줄어듦)
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = contentAlignment
    ) {
        content() // 사용자가 넣은 내용물(텍스트, 아이콘 등) 표시
    }
}