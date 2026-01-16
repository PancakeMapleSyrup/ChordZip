package com.example.guitarchordmanager.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {
    // 상태 관리 (private으로 숨기고, 읽기 전용만 공개)
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()    // _uiState를 일기 전용으로 바꿔서 저장

    // 아이디 입력 시 호출
    fun updateId(newId: String) {
        _uiState.update { it.copy(id = newId) }
    }

    // 비밀번호 입력 시 호출
    fun updatePw(newPw: String) {
        _uiState.update { it.copy(pw = newPw) }
    }

    // 로그인 버튼 클릭 시 호출
    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // 로딩 시작
            _uiState.update{ it.copy(isLoading = true) }

            // (가짜) 서버 통신 시뮬레이션
            delay(1000)

            // 로딩 끝
            _uiState.update { it.copy(isLoading = false) }

            // 성공 콜백 실행 -> 화면 이동은 UI가 알아서 하게 둠
            onSuccess()
        }
    }
}