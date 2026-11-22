package bg.uni_plovdiv.fmi.tapetransfer.ui.revenue

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bg.uni_plovdiv.fmi.tapetransfer.R
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.MonthRevenue
import bg.uni_plovdiv.fmi.tapetransfer.databinding.ItemMonthRevenueBinding
import bg.uni_plovdiv.fmi.tapetransfer.util.Formatters

class MonthRevenueAdapter : ListAdapter<MonthRevenue, MonthRevenueAdapter.MonthViewHolder>(MonthDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val binding = ItemMonthRevenueBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MonthViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MonthViewHolder(private val binding: ItemMonthRevenueBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MonthRevenue) {
            val context = binding.root.context
            binding.textMonth.text = Formatters.formatMonth(item.year, item.month)
            binding.textOrders.text = context.getString(R.string.revenue_month_orders, item.orderCount)
            binding.textTotal.text = Formatters.formatPrice(item.total)
        }
    }

    object MonthDiffCallback : DiffUtil.ItemCallback<MonthRevenue>() {
        override fun areItemsTheSame(oldItem: MonthRevenue, newItem: MonthRevenue): Boolean {
            return oldItem.year == newItem.year && oldItem.month == newItem.month
        }

        override fun areContentsTheSame(oldItem: MonthRevenue, newItem: MonthRevenue): Boolean {
            return oldItem == newItem
        }
    }
}
