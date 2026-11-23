package bg.uni_plovdiv.fmi.tapetransfer.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import bg.uni_plovdiv.fmi.tapetransfer.R
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.MediaType
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.OutputFormat
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.TapeOrderEntity
import bg.uni_plovdiv.fmi.tapetransfer.databinding.FragmentAddEditOrderBinding
import bg.uni_plovdiv.fmi.tapetransfer.util.Formatters
import bg.uni_plovdiv.fmi.tapetransfer.util.PriceQuote
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class AddEditOrderFragment : Fragment() {

    private var _binding: FragmentAddEditOrderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditOrderViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddEditOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.isEditMode) {
            binding.textTitle.setText(R.string.order_edit_title)
        }

        binding.buttonClose.setOnClickListener { findNavController().popBackStack() }
        binding.buttonSave.setOnClickListener { viewModel.save(readForm()) }
        binding.buttonPriceList.setOnClickListener {
            findNavController().navigate(R.id.action_addEditOrder_to_priceList)
        }
        binding.textPriceHint.setOnClickListener {
            viewModel.isManualPrice = false
            updateQuote()
        }

        binding.chipGroupMedia.setOnCheckedStateChangeListener { _, _ -> updateQuote() }
        binding.editQuantity.doAfterTextChanged { updateQuote() }
        binding.editDuration.doAfterTextChanged { updateQuote() }
        binding.editPrice.doAfterTextChanged { text ->
            val value = text.toString()
            viewModel.isManualPrice = value.isNotBlank() && value != viewModel.lastAutoPrice
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { state -> render(state) } }
                launch { viewModel.priceList.collect { updateQuote() } }
            }
        }
    }

    private fun updateQuote() {
        val quote = viewModel.calculateQuote(
            selectedMediaType(),
            binding.editDuration.text.toString(),
            binding.editQuantity.text.toString(),
        )

        if (quote == null) {
            binding.textPriceHint.setText(R.string.price_hint_empty)
            return
        }

        binding.textPriceHint.text = buildHint(quote)

        if (!viewModel.isManualPrice) {
            val priceText = String.format(Locale.US, "%.2f", quote.total)
            viewModel.lastAutoPrice = priceText
            binding.editPrice.setText(priceText)
        }
    }

    private fun buildHint(quote: PriceQuote): String {
        val rate = Formatters.formatPrice(quote.pricePerHour)
        val total = Formatters.formatPrice(quote.total)
        return if (quote.discountPercent > 0) {
            getString(R.string.price_hint_discount, quote.hours, rate, quote.discountPercent, total)
        } else {
            getString(R.string.price_hint, quote.hours, rate, total)
        }
    }

    private fun render(state: AddEditUiState) {
        val order = state.orderToEdit
        if (order != null && !viewModel.isFormFilled) {
            fillForm(order)
            viewModel.isFormFilled = true
        }

        binding.inputClientName.error = state.clientNameError?.let { getString(it) }
        binding.inputPhone.error = state.phoneError?.let { getString(it) }
        binding.inputQuantity.error = state.quantityError?.let { getString(it) }
        binding.inputDuration.error = state.durationError?.let { getString(it) }
        binding.inputPrice.error = state.priceError?.let { getString(it) }

        if (state.isSaved) {
            Toast.makeText(requireContext(), R.string.order_saved, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun fillForm(order: TapeOrderEntity) {
        binding.editClientName.setText(order.clientName)
        binding.editPhone.setText(order.clientPhone)
        binding.editQuantity.setText(order.quantity.toString())
        binding.editDuration.setText(order.durationMinutes.toString())
        binding.editPrice.setText(String.format(Locale.US, "%.2f", order.price))
        binding.editNotes.setText(order.notes)
        binding.chipGroupMedia.check(chipIdFor(order.mediaType))
        binding.toggleFormat.check(buttonIdFor(order.outputFormat))
    }

    private fun readForm(): OrderForm {
        return OrderForm(
            clientName = binding.editClientName.text.toString(),
            phone = binding.editPhone.text.toString(),
            mediaType = selectedMediaType(),
            outputFormat = selectedOutputFormat(),
            quantityText = binding.editQuantity.text.toString(),
            durationText = binding.editDuration.text.toString(),
            priceText = binding.editPrice.text.toString(),
            notes = binding.editNotes.text.toString(),
        )
    }

    private fun selectedMediaType(): MediaType {
        return when (binding.chipGroupMedia.checkedChipId) {
            R.id.chipSvhs -> MediaType.S_VHS
            R.id.chipVhsc -> MediaType.VHS_C
            R.id.chipVideo8 -> MediaType.VIDEO8
            R.id.chipHi8 -> MediaType.HI8
            R.id.chipDigital8 -> MediaType.DIGITAL8
            R.id.chipMiniDv -> MediaType.MINI_DV
            R.id.chipAudio -> MediaType.AUDIO
            else -> MediaType.VHS
        }
    }

    private fun chipIdFor(mediaType: MediaType): Int {
        return when (mediaType) {
            MediaType.VHS -> R.id.chipVhs
            MediaType.S_VHS -> R.id.chipSvhs
            MediaType.VHS_C -> R.id.chipVhsc
            MediaType.VIDEO8 -> R.id.chipVideo8
            MediaType.HI8 -> R.id.chipHi8
            MediaType.DIGITAL8 -> R.id.chipDigital8
            MediaType.MINI_DV -> R.id.chipMiniDv
            MediaType.AUDIO -> R.id.chipAudio
        }
    }

    private fun selectedOutputFormat(): OutputFormat {
        return when (binding.toggleFormat.checkedButtonId) {
            R.id.buttonMkv -> OutputFormat.MKV
            R.id.buttonMp3 -> OutputFormat.MP3
            else -> OutputFormat.MP4
        }
    }

    private fun buttonIdFor(format: OutputFormat): Int {
        return when (format) {
            OutputFormat.MP4 -> R.id.buttonMp4
            OutputFormat.MKV -> R.id.buttonMkv
            OutputFormat.MP3 -> R.id.buttonMp3
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
