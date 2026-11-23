package bg.uni_plovdiv.fmi.tapetransfer.ui.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import bg.uni_plovdiv.fmi.tapetransfer.R
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.OrderStatus
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.TapeOrderEntity
import bg.uni_plovdiv.fmi.tapetransfer.databinding.FragmentOrderDetailBinding
import bg.uni_plovdiv.fmi.tapetransfer.util.ClientReport
import bg.uni_plovdiv.fmi.tapetransfer.util.Formatters
import bg.uni_plovdiv.fmi.tapetransfer.util.LabelPrinter
import bg.uni_plovdiv.fmi.tapetransfer.util.QrCodeGenerator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrderDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener { findNavController().popBackStack() }

        binding.buttonEdit.setOnClickListener {
            val args = Bundle()
            args.putInt("orderId", viewModel.orderId)
            findNavController().navigate(R.id.action_orderDetail_to_addEditOrder, args)
        }

        binding.buttonNextStep.setOnClickListener { viewModel.moveToNextStatus() }
        binding.buttonDelete.setOnClickListener { confirmDelete() }
        binding.cardQrLabel.setOnClickListener { showLargeQr() }
        binding.buttonSms.setOnClickListener { sendSms() }
        binding.buttonShare.setOnClickListener { shareReport() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.order.collect { order ->
                        if (order != null) showOrder(order)
                    }
                }
                launch {
                    viewModel.acceptedBy.collect { showAcceptedBy() }
                }
                launch {
                    viewModel.isAdmin.collect { isAdmin ->
                        binding.buttonDelete.visibility = if (isAdmin) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.isDeleted.collect { isDeleted ->
                        if (isDeleted) {
                            Toast.makeText(requireContext(), R.string.order_deleted, Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }

    private fun showOrder(order: TapeOrderEntity) {
        viewModel.loadAcceptedBy(order.createdByUserId)

        binding.textOrderNumber.text = Formatters.formatOrderNumber(order.id)
        binding.textClient.text = order.clientName
        binding.textPhone.text = order.clientPhone.ifBlank { getString(R.string.detail_no_phone) }
        binding.textMedia.text = order.mediaType.displayName
        binding.textFormat.text = getString(R.string.detail_format_value, order.outputFormat.displayName)
        binding.textDuration.text = getString(R.string.detail_duration_value, order.durationMinutes)
        binding.textPrice.text = Formatters.formatPrice(order.price)

        val step = when (order.status) {
            OrderStatus.PENDING -> 1
            OrderStatus.IN_PROGRESS -> 2
            OrderStatus.DONE -> 3
        }
        binding.textStatus.text = getString(R.string.detail_status_step, order.status.displayName, step)
        colorSegment(binding.segment1, step >= 1)
        colorSegment(binding.segment2, step >= 2)
        colorSegment(binding.segment3, step >= 3)

        if (order.notes.isBlank()) {
            binding.cardNotes.visibility = View.GONE
        } else {
            binding.cardNotes.visibility = View.VISIBLE
            binding.textNotes.text = order.notes
        }

        showQrLabel(order)
        binding.buttonSms.visibility = if (order.clientPhone.isBlank()) View.GONE else View.VISIBLE

        when (order.status) {
            OrderStatus.PENDING -> {
                binding.buttonNextStep.visibility = View.VISIBLE
                binding.buttonNextStep.setText(R.string.action_start_processing)
                binding.buttonNextStep.setIconResource(R.drawable.ic_play)
            }
            OrderStatus.IN_PROGRESS -> {
                binding.buttonNextStep.visibility = View.VISIBLE
                binding.buttonNextStep.setText(R.string.action_mark_done)
                binding.buttonNextStep.setIconResource(R.drawable.ic_check)
            }
            OrderStatus.DONE -> {
                binding.buttonNextStep.visibility = View.GONE
            }
        }

        showAcceptedBy()
    }

    private fun showQrLabel(order: TapeOrderEntity) {
        val content = QrCodeGenerator.buildContent(order)
        binding.imageQr.setImageBitmap(QrCodeGenerator.generate(content, QR_SMALL_SIZE))
        binding.textQrNumber.text = Formatters.formatOrderNumber(order.id)
        binding.textQrInfo.text = getString(
            R.string.qr_label_info,
            order.mediaType.displayName,
            order.outputFormat.displayName,
        )
    }

    private fun sendSms() {
        val order = viewModel.order.value ?: return
        val message = ClientReport.buildMessage(requireContext(), order)

        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + Uri.encode(order.clientPhone)))
        intent.putExtra("sms_body", message)

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.error_no_sms_app, Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareReport() {
        val order = viewModel.order.value ?: return
        val message = ClientReport.buildMessage(requireContext(), order)

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, message)
        intent.putExtra(
            Intent.EXTRA_SUBJECT,
            getString(R.string.share_subject, Formatters.formatOrderNumber(order.id)),
        )

        startActivity(Intent.createChooser(intent, getString(R.string.share_chooser_title)))
    }

    private fun showLargeQr() {
        val order = viewModel.order.value ?: return
        val content = QrCodeGenerator.buildContent(order)

        val imageView = ImageView(requireContext())
        imageView.setImageBitmap(QrCodeGenerator.generate(content, QR_LARGE_SIZE))
        imageView.adjustViewBounds = true
        val padding = resources.getDimensionPixelSize(R.dimen.qr_dialog_padding)
        imageView.setPadding(padding, padding, padding, padding)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.qr_dialog_title, Formatters.formatOrderNumber(order.id)))
            .setView(imageView)
            .setNegativeButton(R.string.close, null)
            .setPositiveButton(R.string.qr_print) { _, _ -> LabelPrinter.print(requireActivity(), order) }
            .show()
    }

    private fun confirmDelete() {
        val order = viewModel.order.value ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_title)
            .setMessage(getString(R.string.delete_message, Formatters.formatOrderNumber(order.id), order.clientName))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete_confirm) { _, _ -> viewModel.deleteOrder() }
            .show()
    }

    private fun showAcceptedBy() {
        val order = viewModel.order.value ?: return
        val name = viewModel.acceptedBy.value ?: getString(R.string.detail_unknown_user)
        binding.textAcceptedBy.text = getString(
            R.string.detail_accepted_by,
            name,
            Formatters.formatDate(order.createdAt),
        )
    }

    private fun colorSegment(segment: View, filled: Boolean) {
        val colorRes = if (filled) R.color.ink else R.color.outline_variant_light
        segment.background.mutate().setTint(ContextCompat.getColor(requireContext(), colorRes))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val QR_SMALL_SIZE = 360
        private const val QR_LARGE_SIZE = 900
    }
}
