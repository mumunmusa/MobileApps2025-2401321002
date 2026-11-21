package bg.uni_plovdiv.fmi.tapetransfer.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.uni_plovdiv.fmi.tapetransfer.R
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.AuthRepository
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.RegisterResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val isLoading: Boolean = false,
    val errorMessage: Int? = null,
    val isRegistered: Boolean = false,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun register(fullName: String, username: String, password: String, confirmPassword: String) {
        if (fullName.isBlank() || username.isBlank() || password.isBlank()) {
            _uiState.value = RegisterUiState(errorMessage = R.string.error_empty_fields)
            return
        }
        if (password != confirmPassword) {
            _uiState.value = RegisterUiState(errorMessage = R.string.error_passwords_mismatch)
            return
        }

        _uiState.value = RegisterUiState(isLoading = true)

        viewModelScope.launch {
            val result = authRepository.register(username, fullName, password)
            _uiState.value = when (result) {
                RegisterResult.SUCCESS -> RegisterUiState(isRegistered = true)
                RegisterResult.INVALID_USERNAME -> RegisterUiState(errorMessage = R.string.error_invalid_username)
                RegisterResult.INVALID_PASSWORD -> RegisterUiState(errorMessage = R.string.error_invalid_password)
                RegisterResult.USERNAME_TAKEN -> RegisterUiState(errorMessage = R.string.error_username_taken)
            }
        }
    }
}
