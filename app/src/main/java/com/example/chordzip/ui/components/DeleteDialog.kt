package com.example.chordzip.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.chordzip.ui.theme.*

@Composable
fun DeleteDialog(
    title: String,          // 예: "노래를 삭제할까요?"
    description: String,    // 예: " 'Hype boy' 항목이 삭제됩니다."
    confirmText: String = "삭제", // 기본값은 "삭제"지만 "나가기", "차단" 등으로 변경 가능
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(White)
                .padding(24.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            // 제목 (외부에서 받은 문자열 표시)
            Text(
                text = title,
                style = Typography.headlineLarge.copy(fontSize = 20.sp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 내용 (노래 제목 강조)
            Text(
                text = description,
                style = Typography.bodyLarge.copy(color = Gray400),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 버튼 영역 (취소 / 삭제)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 취소 버튼 (회색 배경)
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gray100,
                        contentColor = Gray900
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(50.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("취소", color = Gray400, fontWeight = FontWeight.Bold)
                }

                // 삭제 버튼 (빨간색 배경 - 경고 의미)
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF3553),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(50.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(confirmText, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
