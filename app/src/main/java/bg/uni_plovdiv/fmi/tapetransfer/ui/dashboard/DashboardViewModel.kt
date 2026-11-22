package bg.uni_plovdiv.fmi.tapetransfer.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.OrderStatus
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.TapeOrderEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.AuthRepository
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.OrderSummary
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.TapeOrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val orders: List<TapeOrderEntity> = emptyList(),
    val summary: OrderSummary = OrderSummary(activeCount = 0, doneCount = 0, totalRevenue = 0.0),
    val pendingCount: Int = 0,
    val inProgressCount: Int = 0,
    val selectedFilter: OrderStatus? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val orderRepository: TapeOrderRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val selectedFilter = MutableStateFlow<OrderStatus?>(null)

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName

    val uiState: StateFlow<DashboardUiState> =
        combine(orderRepository.allOrders, selectedFilter) { orders, filter ->
            val visibleOrders = if (filter == null) orders else orders.filter { it.status == filter }
            DashboardUiState(
                orders = visibleOrders,
                summary = TapeOrderRepository.calculateSummary(orders),
                pendingCount = orders.count { it.status == OrderStatus.PENDING },
                inProgressCount = orders.count { it.status == OrderStatus.IN_PROGRESS },
                selectedFilter = filter,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    init {
        viewModelScope.launch {
            _userName.value = authRepository.getCurrentUser()?.fullName ?: ""
        }
    }

    fun selectFilter(status: OrderStatus?) {
        selectedFilter.value = status
    }

    fun moveToNextStatus(order: TapeOrderEntity) {
        viewModelScope.launch {
            orderRepository.moveToNextStatus(order)
        }
    }
}
