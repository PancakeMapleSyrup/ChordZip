package com.example.chordzip.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.chordzip.ui.theme.Gray100
import com.example.chordzip.ui.theme.Gray400
import com.example.chordzip.ui.theme.TossBlue
import com.example.chordzip.ui.theme.Typography


// 일반 텍스트 필드
@Composable
fun SimpleTextField(
    value: String,                      // 입력창에 현재 표시될 텍스트 값
    onValueChange: (String) -> Unit,    // 사용자가 글자를 칠 때마다 실행될 함수 (상태 업데이트용)
    placeholder: String = "",           // 입력값이 없을 때 흐릿하게 보이는 힌트 텍스트
    keyboardType: KeyboardType = KeyboardType.Text,     // 키보드 모양 (기본값: 문자 키패드)
    imeAction: ImeAction = ImeAction.Default,                       // 엔터 키 아이콘 설정 (기본값: Default -> 알아서 판단함, 보통 완료/닫기)
    keyboardActions: KeyboardActions = KeyboardActions.Default,     // 엔터 키 눌렀을 때 동작 설정 (기본값: 빈 동작)
    containerColor: Color = Gray100,     // 입력창 배경색 (기본값: 연한 회색)
    clearOnFocus: Boolean = false        // 포커스를 받았을 때 입력창을 비울지 여부
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = containerColor,         // 포커스 됐을 때 배경색 (Gray100)
            unfocusedContainerColor = containerColor,       // 평소 배경색 (Gray100)
            disabledContainerColor = containerColor,        // 비활성화됐을 때 배경색 (Gray100)
            focusedIndicatorColor = Color.Transparent,      // 포커스 됐을 때 밑줄 색 -> 투명 (안 보임)
            unfocusedIndicatorColor = Color.Transparent,    // 평소 밑줄 색 -> 투명 (안 보임)
            disabledIndicatorColor = Color.Transparent,     // 비활성화됐을 때 밑줄 색상 -> 투명
            cursorColor = TossBlue      // 커서 색상
        ),
        textStyle = Typography.bodyLarge,
        singleLine = true,

        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged{ focusState ->
                // 스위치가 켜져 있고(true), 포커스를 받았을 때만 창 안의 내용을 지움
                if (focusState.isFocused && clearOnFocus) {
                    onValueChange("")
                }
            }
        ,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        placeholder = {     // 플레이스홀더
            if (placeholder.isNotEmpty()) {
                Text(text = placeholder, color = Gray400)
            }
        },
    )
}


// 로그인 텍스트 필드
@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    containerColor: Color = Gray100
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = containerColor,         // 포커스 됐을 때 배경색 (Gray100)
            unfocusedContainerColor = containerColor,       // 평소 배경색 (Gray100)
            disabledContainerColor = containerColor,        // 비활성화됐을 때 배경색 (Gray100)
            focusedIndicatorColor = Color.Transparent,      // 포커스 됐을 때 밑줄 색 -> 투명 (안 보임)
            unfocusedIndicatorColor = Color.Transparent,    // 평소 밑줄 색 -> 투명 (안 보임)
            disabledIndicatorColor = Color.Transparent,     // 비활성화됐을 때 밑줄 색상 -> 투명
            cursorColor = TossBlue      // 커서 색상
        ),
        textStyle = Typography.bodyLarge,
        singleLine = true,

        // 레이아웃: 가로를 꽉 채우고, 높이는 56dp로 고정 (표준 터치 영역 크기)
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),

        // 비밀번호 처리: isPassword가 true면 '••••'로 변환, 아니면 그대로 표시
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,

        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        placeholder = {
            Text(
                text = placeholder,
                style = Typography.bodyLarge.copy(
                    color= Gray400,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    )
}

// 라벨이 있는 텍스트 필드
@Composable
fun TextFieldWithLabel(
    label: String,                      // 입력창 위에 표시될 라벨
    value: String,                      // 입력창에 현재 표시될 텍스트 값
    onValueChange: (String) -> Unit,    // 사용자가 글자를 칠 때마다 실행될 함수 (상태 업데이트용)
    placeholder: String = "",           // 입력값이 없을 때 흐릿하게 보이는 힌트 텍스트
    keyboardType: KeyboardType = KeyboardType.Text,     // 키보드 모양 (기본값: 문자 키패드)
    imeAction: ImeAction = ImeAction.Default,                       // 엔터 키 아이콘 설정 (기본값: Default -> 알아서 판단함, 보통 완료/닫기)
    keyboardActions: KeyboardActions = KeyboardActions.Default,     // 엔터 키 눌렀을 때 동작 설정 (기본값: 빈 동작)
    containerColor: Color = Gray100,     // 입력창 배경색 (기본값: 연한 회색)
    clearOnFocus: Boolean = false        // 포커스를 받았을 때 입력창을 비울지 여부
) {
    Column {
        // 라벨
        Text(label, style = Typography.bodySmall, color = Gray400)

        Spacer(modifier = Modifier.height(4.dp))

        // 입력창
        TextField(
            value = value,
            onValueChange = onValueChange,          // 사용자가 키보드를 누를 때마다 호출되어, 뷰모델에 값이 바뀌었음을 알려줌.
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged{ focusState ->
                    // 스위치가 켜져 있고(true), 포커스를 받았을 때만 창 안의 내용을 지움
                    if (focusState.isFocused && clearOnFocus) {
                        onValueChange("")
                    }
                }
            ,
            textStyle = Typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = containerColor,         // 포커스 됐을 때 배경색 (Gray100)
                unfocusedContainerColor = containerColor,       // 평소 배경색 (Gray100)
                focusedIndicatorColor = Color.Transparent,      // 포커스 됐을 때 밑줄 색 -> 투명 (안 보임)
                unfocusedIndicatorColor = Color.Transparent     // 평소 밑줄 색 -> 투명 (안 보임)
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,      // 입력창을 한 줄로 제한. 엔터를 쳐도 줄바꿈되지 않고 옆으로 스크롤됨.
            placeholder = {     // 플레이스홀더
                if (placeholder.isNotEmpty()) {
                    Text(text = placeholder, color = Gray400.copy(alpha = 0.5f))
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,    // 키보드의 종류 설정
                imeAction = imeAction
            ),
            keyboardActions = keyboardActions
        )
    }
}