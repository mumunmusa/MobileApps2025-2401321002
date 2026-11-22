package bg.uni_plovdiv.fmi.tapetransfer.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.TapeOrderEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.AuthRepository
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.TapeOrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val orderRepository: TapeOrderRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val orderId: Int = savedStateHandle.get<Int>("orderId") ?: -1

    val order: StateFlow<TapeOrderEntity?> = orderRepository.orderById(orderId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _acceptedBy = MutableStateFlow<String?>(null)
    val acceptedBy: StateFlow<String?> = _acceptedBy

    fun loadAcceptedBy(userId: Int?) {
        if (userId == null || _acceptedBy.value != null) return
        viewModelScope.launch {
            _acceptedBy.value = authRepository.getUserById(userId)?.fullName
        }
    }

    fun moveToNextStatus() {
        val current = order.value ?: return
        viewModelScope.launch {
            orderRepository.moveToNextStatus(current)
        }
    }
}
