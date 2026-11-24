package bg.uni_plovdiv.fmi.tapetransfer.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.UserEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.UserRole
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.AuthRepository
import bg.uni_plovdiv.fmi.tapetransfer.data.settings.ThemeManager
import bg.uni_plovdiv.fmi.tapetransfer.data.settings.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: UserEntity? = null,
    val isAdmin: Boolean = false,
    val pendingUsers: List<UserEntity> = emptyList(),
    val employees: List<UserEntity> = emptyList(),
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val themeManager: ThemeManager,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> =
        combine(
            authRepository.currentUser,
            authRepository.pendingUsers,
            authRepository.approvedUsers,
        ) { user, pending, approved ->
            val isAdmin = user?.role == UserRole.ADMIN
            val currentUserId = user?.id
            ProfileUiState(
                user = user,
                isAdmin = isAdmin,
                pendingUsers = if (isAdmin) pending else emptyList(),
                employees = if (isAdmin) approved.filter { it.id != currentUserId } else emptyList(),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut

    fun getThemeMode(): ThemeMode = themeManager.getThemeMode()

    fun setThemeMode(mode: ThemeMode) {
        themeManager.setThemeMode(mode)
    }

    fun approve(user: UserEntity) {
        viewModelScope.launch { authRepository.approveUser(user) }
    }

    fun reject(user: UserEntity) {
        viewModelScope.launch { authRepository.deleteUser(user) }
    }

    fun deleteEmployee(user: UserEntity) {
        viewModelScope.launch { authRepository.deleteUser(user) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _isLoggedOut.value = true
        }
    }
}
