package bg.uni_plovdiv.fmi.tapetransfer.ui.prices

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bg.uni_plovdiv.fmi.tapetransfer.R
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.DiscountEntity
import bg.uni_plovdiv.fmi.tapetransfer.databinding.ItemPriceRowBinding

class DiscountAdapter(
    private val onDiscountClick: (DiscountEntity) -> Unit,
) : ListAdapter<DiscountEntity, DiscountAdapter.DiscountViewHolder>(DiscountDiffCallback) {

    var canEdit: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscountViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPriceRowBinding.inflate(inflater, parent, false)
        return DiscountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiscountViewHolder, position: Int) {
        val next = if (position + 1 < itemCount) getItem(position + 1) else null
        holder.bind(getItem(position), next)
    }

    inner class DiscountViewHolder(
        private val binding: ItemPriceRowBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(discount: DiscountEntity, next: DiscountEntity?) {
            val context = binding.root.context
            binding.textTitle.text = if (next != null) {
                context.getString(R.string.discount_range, discount.minQuantity, next.minQuantity - 1)
            } else {
                context.getString(R.string.discount_range_open, discount.minQuantity)
            }
            binding.textValue.text = context.getString(R.string.discount_value, discount.percent)
            binding.imageEdit.visibility = if (canEdit) View.VISIBLE else View.GONE
            binding.root.isClickable = canEdit
            binding.root.setOnClickListener { if (canEdit) onDiscountClick(discount) }
        }
    }

    object DiscountDiffCallback : DiffUtil.ItemCallback<DiscountEntity>() {
        override fun areItemsTheSame(oldItem: DiscountEntity, newItem: DiscountEntity): Boolean {
            return oldItem.minQuantity == newItem.minQuantity
        }

        override fun areContentsTheSame(oldItem: DiscountEntity, newItem: DiscountEntity): Boolean {
            return oldItem == newItem
        }
    }
}
