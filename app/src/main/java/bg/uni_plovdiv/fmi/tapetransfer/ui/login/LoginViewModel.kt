package bg.uni_plovdiv.fmi.tapetransfer.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.uni_plovdiv.fmi.tapetransfer.R
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.AuthRepository
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isCheckingSession: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: Int? = null,
    val isLoggedIn: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState(isCheckingSession = true))
    val uiState: StateFlow<LoginUiState> = _uiState

    init {
        viewModelScope.launch {
            val loggedIn = authRepository.hasActiveSession()
            _uiState.value = LoginUiState(isLoggedIn = loggedIn)
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(errorMessage = R.string.error_empty_fields)
            return
        }

        _uiState.value = LoginUiState(isLoading = true)

        viewModelScope.launch {
            val result = authRepository.login(username, password)
            _uiState.value = when (result) {
                LoginResult.SUCCESS -> LoginUiState(isLoggedIn = true)
                LoginResult.INVALID_CREDENTIALS -> LoginUiState(errorMessage = R.string.error_invalid_credentials)
                LoginResult.NOT_APPROVED -> LoginUiState(errorMessage = R.string.error_not_approved)
            }
        }
    }

    fun onNavigatedToDashboard() {
        _uiState.value = LoginUiState()
    }
}
