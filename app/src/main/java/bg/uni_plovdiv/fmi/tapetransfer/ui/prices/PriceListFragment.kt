package bg.uni_plovdiv.fmi.tapetransfer.ui.prices

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import bg.uni_plovdiv.fmi.tapetransfer.R
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.DiscountEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.PriceEntity
import bg.uni_plovdiv.fmi.tapetransfer.databinding.DialogEditValueBinding
import bg.uni_plovdiv.fmi.tapetransfer.databinding.FragmentPriceListBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class PriceListFragment : Fragment() {

    private var _binding: FragmentPriceListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PriceListViewModel by viewModels()
    private val priceAdapter = PriceAdapter(onPriceClick = { price -> editPrice(price) })
    private val discountAdapter = DiscountAdapter(onDiscountClick = { discount -> editDiscount(discount) })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPriceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener { findNavController().popBackStack() }

        binding.recyclerPrices.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPrices.adapter = priceAdapter
        binding.recyclerDiscounts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDiscounts.adapter = discountAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: PriceListUiState) {
        priceAdapter.canEdit = state.isAdmin
        discountAdapter.canEdit = state.isAdmin
        priceAdapter.submitList(state.prices)
        discountAdapter.submitList(state.discounts)

        val firstTier = state.discounts.firstOrNull()
        binding.textNoDiscount.text = if (firstTier != null) {
            getString(R.string.discount_none, firstTier.minQuantity - 1)
        } else {
            ""
        }

        binding.textReadOnly.visibility = if (state.isAdmin) View.GONE else View.VISIBLE
        binding.textAdminHint.visibility = if (state.isAdmin) View.VISIBLE else View.GONE
    }

    private fun editPrice(price: PriceEntity) {
        val currentValue = String.format(Locale.US, "%.2f", price.pricePerHour)
        showEditDialog(
            title = getString(R.string.price_edit_title, price.mediaType.displayName),
            hint = getString(R.string.price_edit_hint),
            value = currentValue,
            isDecimal = true,
        ) { text -> viewModel.updatePrice(price, text) }
    }

    private fun editDiscount(discount: DiscountEntity) {
        showEditDialog(
            title = getString(R.string.discount_edit_title, discount.minQuantity),
            hint = getString(R.string.discount_edit_hint),
            value = discount.percent.toString(),
            isDecimal = false,
        ) { text -> viewModel.updateDiscount(discount, text) }
    }

    private fun showEditDialog(
        title: String,
        hint: String,
        value: String,
        isDecimal: Boolean,
        onSave: (String) -> Boolean,
    ) {
        val dialogBinding = DialogEditValueBinding.inflate(layoutInflater)
        dialogBinding.inputValue.hint = hint
        dialogBinding.editValue.setText(value)
        dialogBinding.editValue.inputType = if (isDecimal) {
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        } else {
            InputType.TYPE_CLASS_NUMBER
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.order_save) { _, _ ->
                val saved = onSave(dialogBinding.editValue.text.toString())
                val message = if (saved) R.string.price_saved else R.string.error_value_invalid
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerPrices.adapter = null
        binding.recyclerDiscounts.adapter = null
        _binding = null
    }
}
