package bg.uni_plovdiv.fmi.tapetransfer.ui.prices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.DiscountEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.PriceEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.UserRole
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.AuthRepository
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.PriceRepository
import bg.uni_plovdiv.fmi.tapetransfer.util.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PriceListUiState(
    val prices: List<PriceEntity> = emptyList(),
    val discounts: List<DiscountEntity> = emptyList(),
    val isAdmin: Boolean = false,
)

@HiltViewModel
class PriceListViewModel @Inject constructor(
    private val priceRepository: PriceRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    val uiState: StateFlow<PriceListUiState> =
        combine(
            priceRepository.prices,
            priceRepository.discounts,
            authRepository.currentUser,
        ) { prices, discounts, user ->
            PriceListUiState(
                prices = prices,
                discounts = discounts,
                isAdmin = user?.role == UserRole.ADMIN,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PriceListUiState())

    fun updatePrice(price: PriceEntity, text: String): Boolean {
        val newValue = text.trim().replace(',', '.').toDoubleOrNull()
        if (!Validators.isValidPrice(newValue) || newValue == null) return false

        viewModelScope.launch {
            priceRepository.updatePrice(price.copy(pricePerHour = newValue))
        }
        return true
    }

    fun updateDiscount(discount: DiscountEntity, text: String): Boolean {
        val newPercent = text.trim().toIntOrNull()
        if (!Validators.isValidDiscount(newPercent) || newPercent == null) return false

        viewModelScope.launch {
            priceRepository.updateDiscount(discount.copy(percent = newPercent))
        }
        return true
    }
}
