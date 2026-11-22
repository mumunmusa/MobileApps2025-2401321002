package bg.uni_plovdiv.fmi.tapetransfer.ui.orders

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
import bg.uni_plovdiv.fmi.tapetransfer.databinding.FragmentOrdersListBinding
import bg.uni_plovdiv.fmi.tapetransfer.ui.dashboard.OrderAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrdersListFragment : Fragment() {

    private var _binding: FragmentOrdersListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrdersListViewModel by viewModels()

    private val adapter = OrderAdapter(
        onOrderClick = { order -> openOrderDetail(order.id) },
        onNextStepClick = { order -> viewModel.moveToNextStatus(order) },
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentOrdersListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.showDone) {
            binding.textTitle.setText(R.string.orders_done_title)
            binding.textEmpty.setText(R.string.orders_done_empty)
        } else {
            binding.textTitle.setText(R.string.orders_active_title)
            binding.textEmpty.setText(R.string.orders_active_empty)
        }

        binding.buttonBack.setOnClickListener { findNavController().popBackStack() }

        binding.recyclerOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerOrders.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.orders.collect { orders ->
                    adapter.submitList(orders)
                    binding.textCount.text = getString(R.string.orders_count, orders.size)
                    binding.textEmpty.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun openOrderDetail(orderId: Int) {
        val args = Bundle()
        args.putInt("orderId", orderId)
        findNavController().navigate(R.id.action_ordersList_to_orderDetail, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerOrders.adapter = null
        _binding = null
    }
}
