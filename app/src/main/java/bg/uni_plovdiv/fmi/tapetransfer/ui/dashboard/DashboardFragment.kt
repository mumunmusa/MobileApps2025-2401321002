package bg.uni_plovdiv.fmi.tapetransfer.ui.dashboard

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
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.OrderStatus
import bg.uni_plovdiv.fmi.tapetransfer.databinding.FragmentDashboardBinding
import bg.uni_plovdiv.fmi.tapetransfer.util.Formatters
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private val adapter = OrderAdapter(
        onOrderClick = { order -> openOrderDetail(order.id) },
        onNextStepClick = { order -> viewModel.moveToNextStatus(order) },
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerOrders.adapter = adapter

        binding.fabNewOrder.setOnClickListener { openOrderForm(-1) }

        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val status = when (checkedIds.firstOrNull()) {
                R.id.chipPending -> OrderStatus.PENDING
                R.id.chipInProgress -> OrderStatus.IN_PROGRESS
                R.id.chipDone -> OrderStatus.DONE
                else -> null
            }
            viewModel.selectFilter(status)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { state -> render(state) } }
                launch {
                    viewModel.userName.collect { name ->
                        binding.textGreeting.text = getString(R.string.dashboard_greeting, name)
                    }
                }
            }
        }
    }

    private fun openOrderDetail(orderId: Int) {
        val args = Bundle()
        args.putInt("orderId", orderId)
        findNavController().navigate(R.id.action_dashboard_to_orderDetail, args)
    }

    private fun openOrderForm(orderId: Int) {
        val args = Bundle()
        args.putInt("orderId", orderId)
        findNavController().navigate(R.id.action_dashboard_to_addEditOrder, args)
    }

    private fun render(state: DashboardUiState) {
        binding.textActiveCount.text = state.summary.activeCount.toString()
        binding.textActiveDetails.text = getString(
            R.string.dashboard_active_details,
            state.pendingCount,
            state.inProgressCount,
        )
        binding.textRevenue.text = Formatters.formatPrice(state.summary.totalRevenue)
        binding.textDoneCount.text = state.summary.doneCount.toString()

        adapter.submitList(state.orders)

        val isEmpty = state.orders.isEmpty()
        binding.cardEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.textEmptyTitle.text = if (state.selectedFilter == null) {
            getString(R.string.dashboard_empty_title)
        } else {
            getString(R.string.dashboard_empty_filter)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerOrders.adapter = null
        _binding = null
    }
}
