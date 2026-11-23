package bg.uni_plovdiv.fmi.tapetransfer.ui.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.uni_plovdiv.fmi.tapetransfer.R
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.DiscountEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.MediaType
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.OrderStatus
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.OutputFormat
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.PriceEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.TapeOrderEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.AuthRepository
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.PriceRepository
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.TapeOrderRepository
import bg.uni_plovdiv.fmi.tapetransfer.util.PriceCalculator
import bg.uni_plovdiv.fmi.tapetransfer.util.PriceQuote
import bg.uni_plovdiv.fmi.tapetransfer.util.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditUiState(
    val orderToEdit: TapeOrderEntity? = null,
    val clientNameError: Int? = null,
    val phoneError: Int? = null,
    val quantityError: Int? = null,
    val durationError: Int? = null,
    val priceError: Int? = null,
    val isSaved: Boolean = false,
)

data class OrderForm(
    val clientName: String,
    val phone: String,
    val mediaType: MediaType,
    val outputFormat: OutputFormat,
    val quantityText: String,
    val durationText: String,
    val priceText: String,
    val notes: String,
)

data class PriceList(
    val prices: List<PriceEntity> = emptyList(),
    val discounts: List<DiscountEntity> = emptyList(),
)

@HiltViewModel
class AddEditOrderViewModel @Inject constructor(
    private val orderRepository: TapeOrderRepository,
    private val authRepository: AuthRepository,
    priceRepository: PriceRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val orderId: Int = savedStateHandle.get<Int>("orderId") ?: -1

    val isEditMode: Boolean = orderId != -1

    var isFormFilled = false

    var isManualPrice = false

    var lastAutoPrice = ""

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState

    val priceList: StateFlow<PriceList> =
        combine(priceRepository.prices, priceRepository.discounts) { prices, discounts ->
            PriceList(prices, discounts)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PriceList())

    init {
        if (isEditMode) {
            viewModelScope.launch {
                val order = orderRepository.orderById(orderId).first()
                _uiState.value = _uiState.value.copy(orderToEdit = order)
            }
        }
    }

    fun calculateQuote(mediaType: MediaType, durationText: String, quantityText: String): PriceQuote? {
        val minutes = durationText.trim().toIntOrNull() ?: return null
        val quantity = quantityText.trim().toIntOrNull() ?: 1
        if (minutes <= 0 || quantity <= 0) return null

        val list = priceList.value
        val price = list.prices.find { it.mediaType == mediaType } ?: return null
        return PriceCalculator.calculate(price.pricePerHour, minutes, quantity, list.discounts)
    }

    fun save(form: OrderForm) {
        val quantity = form.quantityText.trim().toIntOrNull()
        val duration = form.durationText.trim().toIntOrNull()
        val price = form.priceText.trim().replace(',', '.').toDoubleOrNull()

        val clientNameError = if (Validators.isValidClientName(form.clientName)) null else R.string.error_client_required
        val phoneError = if (Validators.isValidPhone(form.phone)) null else R.string.error_phone_invalid
        val quantityError = if (Validators.isValidQuantity(quantity)) null else R.string.error_quantity_invalid
        val durationError = if (Validators.isValidDuration(duration)) null else R.string.error_duration_invalid
        val priceError = if (Validators.isValidPrice(price)) null else R.string.error_price_invalid

        _uiState.value = _uiState.value.copy(
            clientNameError = clientNameError,
            phoneError = phoneError,
            quantityError = quantityError,
            durationError = durationError,
            priceError = priceError,
        )

        val hasErrors = clientNameError != null || phoneError != null || quantityError != null ||
            durationError != null || priceError != null
        if (hasErrors || quantity == null || duration == null || price == null) return

        val quote = calculateQuote(form.mediaType, form.durationText, form.quantityText)
        val discountPercent = if (quote != null && quote.total == price) quote.discountPercent else 0

        viewModelScope.launch {
            val existing = _uiState.value.orderToEdit
            if (existing != null) {
                val updated = existing.copy(
                    clientName = form.clientName.trim(),
                    clientPhone = form.phone.trim(),
                    mediaType = form.mediaType,
                    outputFormat = form.outputFormat,
                    quantity = quantity,
                    durationMinutes = duration,
                    price = price,
                    discountPercent = discountPercent,
                    notes = form.notes.trim(),
                )
                orderRepository.update(updated)
            } else {
                val currentUser = authRepository.getCurrentUser()
                val newOrder = TapeOrderEntity(
                    clientName = form.clientName.trim(),
                    clientPhone = form.phone.trim(),
                    mediaType = form.mediaType,
                    outputFormat = form.outputFormat,
                    quantity = quantity,
                    durationMinutes = duration,
                    price = price,
                    discountPercent = discountPercent,
                    status = OrderStatus.PENDING,
                    notes = form.notes.trim(),
                    createdByUserId = currentUser?.id,
                )
                orderRepository.add(newOrder)
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
