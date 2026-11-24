package bg.uni_plovdiv.fmi.tapetransfer.ui.dashboard

import android.os.Bundle
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
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.OrderStatus
import bg.uni_plovdiv.fmi.tapetransfer.databinding.FragmentDashboardBinding
import bg.uni_plovdiv.fmi.tapetransfer.util.Formatters
import bg.uni_plovdiv.fmi.tapetransfer.util.QrCodeParser
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private val scanLauncher = registerForActivityResult(ScanContract()) { result -> onScanResult(result) }
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

        binding.fabNewOrder.setOnClickListener { openNewOrderForm() }
        binding.buttonScan.setOnClickListener { startScan() }
        binding.buttonProfile.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_profile)
        }
        binding.cardActive.setOnClickListener { openOrdersList(showDone = false) }
        binding.cardDone.setOnClickListener { openOrdersList(showDone = true) }
        binding.cardRevenue.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_revenue)
        }

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

    private fun startScan() {
        val options = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setPrompt(getString(R.string.scan_prompt))
            .setBeepEnabled(false)
            .setOrientationLocked(false)
        scanLauncher.launch(options)
    }

    private fun onScanResult(result: ScanIntentResult) {
        if (result.contents != null) {
            onCodeScanned(result.contents)
            return
        }

        val noPermission = result.originalIntent?.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION) == true
        if (noPermission) showMessage(getString(R.string.scan_no_permission))
    }

    private fun onCodeScanned(text: String?) {
        val orderId = QrCodeParser.parseOrderId(text)
        if (orderId == null) {
            showMessage(getString(R.string.scan_not_tapetransfer))
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            if (viewModel.orderExists(orderId)) {
                openOrderDetail(orderId)
            } else {
                showMessage(getString(R.string.scan_order_not_found, Formatters.formatOrderNumber(orderId)))
            }
        }
    }

    private fun showMessage(message: String) {
        if (_binding == null) return
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun openOrdersList(showDone: Boolean) {
        val args = Bundle()
        args.putBoolean("showDone", showDone)
        findNavController().navigate(R.id.action_dashboard_to_ordersList, args)
    }

    private fun openOrderDetail(orderId: Int) {
        val args = Bundle()
        args.putInt("orderId", orderId)
        findNavController().navigate(R.id.action_dashboard_to_orderDetail, args)
    }

    private fun openNewOrderForm() {
        findNavController().navigate(R.id.action_dashboard_to_addEditOrder)
    }

    private fun render(state: DashboardUiState) {
        binding.textActiveCount.text = state.activeCount.toString()
        binding.textActiveDetails.text = getString(
            R.string.dashboard_active_details,
            state.pendingCount,
            state.inProgressCount,
        )
        binding.textRevenue.text = Formatters.formatPrice(state.monthRevenue)
        binding.textPeriod.text = Formatters.formatMonth(System.currentTimeMillis())
        binding.textDoneCount.text = state.doneCount.toString()

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
