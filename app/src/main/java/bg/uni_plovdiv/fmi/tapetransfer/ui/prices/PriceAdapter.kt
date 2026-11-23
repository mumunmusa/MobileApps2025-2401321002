package bg.uni_plovdiv.fmi.tapetransfer.ui.prices

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bg.uni_plovdiv.fmi.tapetransfer.R
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.PriceEntity
import bg.uni_plovdiv.fmi.tapetransfer.databinding.ItemPriceRowBinding
import bg.uni_plovdiv.fmi.tapetransfer.util.Formatters

class PriceAdapter(
    private val onPriceClick: (PriceEntity) -> Unit,
) : ListAdapter<PriceEntity, PriceAdapter.PriceViewHolder>(PriceDiffCallback) {

    var canEdit: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PriceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPriceRowBinding.inflate(inflater, parent, false)
        return PriceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PriceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PriceViewHolder(
        private val binding: ItemPriceRowBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(price: PriceEntity) {
            val context = binding.root.context
            binding.textTitle.text = price.mediaType.displayName
            binding.textValue.text = context.getString(
                R.string.price_per_hour,
                Formatters.formatPrice(price.pricePerHour),
            )
            binding.imageEdit.visibility = if (canEdit) View.VISIBLE else View.GONE
            binding.root.isClickable = canEdit
            binding.root.setOnClickListener { if (canEdit) onPriceClick(price) }
        }
    }

    object PriceDiffCallback : DiffUtil.ItemCallback<PriceEntity>() {
        override fun areItemsTheSame(oldItem: PriceEntity, newItem: PriceEntity): Boolean {
            return oldItem.mediaType == newItem.mediaType
        }

        override fun areContentsTheSame(oldItem: PriceEntity, newItem: PriceEntity): Boolean {
            return oldItem == newItem
        }
    }
}
