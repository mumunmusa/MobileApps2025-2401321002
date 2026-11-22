package bg.uni_plovdiv.fmi.tapetransfer.ui.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.uni_plovdiv.fmi.tapetransfer.R
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.MediaType
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.OrderStatus
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.OutputFormat
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.TapeOrderEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.AuthRepository
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.TapeOrderRepository
import bg.uni_plovdiv.fmi.tapetransfer.util.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditUiState(
    val orderToEdit: TapeOrderEntity? = null,
    val clientNameError: Int? = null,
    val phoneError: Int? = null,
    val durationError: Int? = null,
    val priceError: Int? = null,
    val isSaved: Boolean = false,
)

data class OrderForm(
    val clientName: String,
    val phone: String,
    val mediaType: MediaType,
    val outputFormat: OutputFormat,
    val durationText: String,
    val priceText: String,
    val notes: String,
)

@HiltViewModel
class AddEditOrderViewModel @Inject constructor(
    private val orderRepository: TapeOrderRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val orderId: Int = savedStateHandle.get<Int>("orderId") ?: -1

    val isEditMode: Boolean = orderId != -1

    var isFormFilled = false

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState

    init {
        if (isEditMode) {
            viewModelScope.launch {
                val order = orderRepository.orderById(orderId).first()
                _uiState.value = _uiState.value.copy(orderToEdit = order)
            }
        }
    }

    fun save(form: OrderForm) {
        val duration = form.durationText.trim().toIntOrNull()
        val price = form.priceText.trim().replace(',', '.').toDoubleOrNull()

        val clientNameError = if (Validators.isValidClientName(form.clientName)) null else R.string.error_client_required
        val phoneError = if (Validators.isValidPhone(form.phone)) null else R.string.error_phone_invalid
        val durationError = if (Validators.isValidDuration(duration)) null else R.string.error_duration_invalid
        val priceError = if (Validators.isValidPrice(price)) null else R.string.error_price_invalid

        _uiState.value = _uiState.value.copy(
            clientNameError = clientNameError,
            phoneError = phoneError,
            durationError = durationError,
            priceError = priceError,
        )

        val hasErrors = clientNameError != null || phoneError != null || durationError != null || priceError != null
        if (hasErrors || duration == null || price == null) return

        viewModelScope.launch {
            val existing = _uiState.value.orderToEdit
            if (existing != null) {
                val updated = existing.copy(
                    clientName = form.clientName.trim(),
                    clientPhone = form.phone.trim(),
                    mediaType = form.mediaType,
                    outputFormat = form.outputFormat,
                    durationMinutes = duration,
                    price = price,
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
                    durationMinutes = duration,
                    price = price,
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
