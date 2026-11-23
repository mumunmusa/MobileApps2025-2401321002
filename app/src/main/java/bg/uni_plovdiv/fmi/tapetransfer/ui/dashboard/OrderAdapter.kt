package bg.uni_plovdiv.fmi.tapetransfer.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bg.uni_plovdiv.fmi.tapetransfer.R
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.OrderStatus
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.TapeOrderEntity
import bg.uni_plovdiv.fmi.tapetransfer.databinding.ItemOrderBinding
import bg.uni_plovdiv.fmi.tapetransfer.util.Formatters
import com.google.android.material.R as MaterialR
import com.google.android.material.color.MaterialColors

class OrderAdapter(
    private val onOrderClick: (TapeOrderEntity) -> Unit,
    private val onNextStepClick: (TapeOrderEntity) -> Unit,
) : ListAdapter<TapeOrderEntity, OrderAdapter.OrderViewHolder>(OrderDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
        holder.itemView.setOnClickListener { onOrderClick(order) }
        holder.binding.buttonNextStep.setOnClickListener { onNextStepClick(order) }
    }

    class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: TapeOrderEntity) {
            val context = binding.root.context

            binding.textClient.text = order.clientName
            binding.textMeta.text = context.getString(
                R.string.order_meta,
                Formatters.formatOrderNumber(order.id),
                order.outputFormat.displayName,
                order.durationMinutes,
            )
            binding.textMedia.text = order.mediaType.displayName
            binding.textStatus.text = order.status.displayName
            binding.textPrice.text = Formatters.formatPrice(order.price)

            val filledSegments = when (order.status) {
                OrderStatus.PENDING -> 1
                OrderStatus.IN_PROGRESS -> 2
                OrderStatus.DONE -> 3
            }
            colorSegment(binding.segment1, filledSegments >= 1)
            colorSegment(binding.segment2, filledSegments >= 2)
            colorSegment(binding.segment3, filledSegments >= 3)

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
        }

        private fun colorSegment(segment: View, filled: Boolean) {
            val colorAttr = if (filled) MaterialR.attr.colorOnSurface else MaterialR.attr.colorOutlineVariant
            segment.background.mutate().setTint(MaterialColors.getColor(segment, colorAttr))
        }
    }

    object OrderDiffCallback : DiffUtil.ItemCallback<TapeOrderEntity>() {
        override fun areItemsTheSame(oldItem: TapeOrderEntity, newItem: TapeOrderEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TapeOrderEntity, newItem: TapeOrderEntity): Boolean {
            return oldItem == newItem
        }
    }
}
