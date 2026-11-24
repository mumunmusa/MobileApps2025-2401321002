package bg.uni_plovdiv.fmi.tapetransfer.ui.revenue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import bg.uni_plovdiv.fmi.tapetransfer.R
import bg.uni_plovdiv.fmi.tapetransfer.databinding.FragmentRevenueBinding
import bg.uni_plovdiv.fmi.tapetransfer.util.Formatters
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RevenueFragment : Fragment() {

    private var _binding: FragmentRevenueBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RevenueViewModel by viewModels()
    private val adapter = MonthRevenueAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRevenueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener { findNavController().popBackStack() }

        binding.recyclerMonths.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMonths.adapter = adapter

        binding.textMonthLabel.text = getString(
            R.string.revenue_month_label,
            getString(R.string.revenue_month),
            Formatters.formatMonth(System.currentTimeMillis()),
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: RevenueUiState) {
        binding.textMonthTotal.text = Formatters.formatPrice(state.monthTotal)
        binding.textYearTotal.text = Formatters.formatPrice(state.yearTotal)
        binding.textAllTotal.text = Formatters.formatPrice(state.allTotal)
        adapter.submitList(state.months)
        binding.textEmpty.visibility = if (state.months.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerMonths.adapter = null
        _binding = null
    }
}
