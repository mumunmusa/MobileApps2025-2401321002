package bg.uni_plovdiv.fmi.tapetransfer.ui.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.OrderStatus
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.TapeOrderEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.TapeOrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersListViewModel @Inject constructor(
    private val orderRepository: TapeOrderRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val showDone: Boolean = savedStateHandle.get<Boolean>("showDone") ?: false

    val orders: StateFlow<List<TapeOrderEntity>> = orderRepository.allOrders
        .map { allOrders ->
            if (showDone) {
                allOrders.filter { it.status == OrderStatus.DONE }
            } else {
                allOrders.filter { it.status != OrderStatus.DONE }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun moveToNextStatus(order: TapeOrderEntity) {
        viewModelScope.launch {
            orderRepository.moveToNextStatus(order)
        }
    }
}
