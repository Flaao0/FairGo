package com.example.fairgo.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairgo.data.network.models.AuthRequest
import com.example.fairgo.data.network.models.RegisterRequest
import com.example.fairgo.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    fun onNameChanged(value: String) {
        _name.value = value
    }

    fun onEmailChanged(value: String) {
        _email.value = value
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
    }

    fun login() {
        val username = email.value.trim()
        val currentPassword = password.value.trim()

        if (username.isBlank() || currentPassword.isBlank()) {
            _error.value = "Введите логин и пароль"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccess.value = false

            val result = authRepository.login(
                AuthRequest(
                    username = username,
                    password = currentPassword
                )
            )

            _isLoading.value = false
            result
                .onSuccess { _isSuccess.value = true }
                .onFailure { _error.value = it.message ?: "Ошибка входа" }
        }
    }

    fun register() {
        val username = name.value.trim().ifEmpty { email.value.trim() }
        val phoneNumber = email.value.trim()
        val currentPassword = password.value.trim()

        if (username.isBlank() || phoneNumber.isBlank() || currentPassword.isBlank()) {
            _error.value = "Заполните все поля"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccess.value = false

            val result = authRepository.register(
                RegisterRequest(
                    username = username,
                    password = currentPassword,
                    phoneNumber = phoneNumber,
                    isDriver = false
                )
            )

            _isLoading.value = false
            result
                .onSuccess { _isSuccess.value = true }
                .onFailure { _error.value = it.message ?: "Ошибка регистрации" }
        }
    }

    fun resetSuccessState() {
        _isSuccess.value = false
    }

    fun clearError() {
        _error.value = null
    }
}

