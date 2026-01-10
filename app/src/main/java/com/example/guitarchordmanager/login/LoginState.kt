package com.example.guitarchordmanager.login

data class LoginUiState(
    val id: String = "",
    val pw: String = "",
    val isLoading: Boolean = false, // 로딩 중인지 여부
    val errorMessage: String? = null // 로그인 실패 시 에러 메시지
) {
    // 버튼 활성화 여부를 여기서 계산
    val isButtonEnabled: Boolean
        get() = id.isNotEmpty() && pw.isNotEmpty()
}