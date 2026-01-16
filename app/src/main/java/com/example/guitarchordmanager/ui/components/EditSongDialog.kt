package com.example.guitarchordmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.guitarchordmanager.ui.theme.*

@Composable
fun EditSongDialog(
    initialTitle: String,
    initialArtist: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var artist by remember { mutableStateOf(initialArtist) }

    Dialog(onDismissRequest = onDismiss) {
        // 하얀색 둥근 카드 배경
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(White)
                .padding(24.dp)
        ) {
            Text(
                text = "노래 정보 수정",
                style = Typography.headlineLarge.copy(fontSize = 20.sp),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 제목 수정
            TextFieldWithLabel(
                label = "노래 제목",
                value = title,
                onValueChange = { title = it },
                placeholder = "제목",
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 가수 수정
            TextFieldWithLabel(
                label = "가수 이름",
                value = artist,
                onValueChange = { artist = it },
                placeholder = "가수",
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = { onConfirm(title, artist) }
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 버튼 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // 취소 버튼 (텍스트만)
                TextButton(onClick = onDismiss) {
                    Text("취소", color = Gray400, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))

                // 확인 버튼 (파란색)
                Button(
                    onClick = { onConfirm(title, artist) },
                    colors = ButtonDefaults.buttonColors(containerColor = TossBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("수정", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}