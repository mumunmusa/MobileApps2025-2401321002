package bg.uni_plovdiv.fmi.tapetransfer.ui.revenue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.MonthRevenue
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.RevenuePeriod
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.TapeOrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class RevenueUiState(
    val monthTotal: Double = 0.0,
    val yearTotal: Double = 0.0,
    val allTotal: Double = 0.0,
    val months: List<MonthRevenue> = emptyList(),
)

@HiltViewModel
class RevenueViewModel @Inject constructor(
    orderRepository: TapeOrderRepository,
) : ViewModel() {

    val uiState: StateFlow<RevenueUiState> = orderRepository.allOrders
        .map { orders ->
            RevenueUiState(
                monthTotal = TapeOrderRepository.calculateSummary(orders, RevenuePeriod.MONTH).totalRevenue,
                yearTotal = TapeOrderRepository.calculateSummary(orders, RevenuePeriod.YEAR).totalRevenue,
                allTotal = TapeOrderRepository.calculateSummary(orders, RevenuePeriod.ALL).totalRevenue,
                months = TapeOrderRepository.revenueByMonth(orders),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RevenueUiState())
}
