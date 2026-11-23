package bg.uni_plovdiv.fmi.tapetransfer.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bg.uni_plovdiv.fmi.tapetransfer.R
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.UserEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.UserRole
import bg.uni_plovdiv.fmi.tapetransfer.databinding.ItemEmployeeBinding
import bg.uni_plovdiv.fmi.tapetransfer.util.Formatters

class EmployeeAdapter(
    private val onDeleteClick: (UserEntity) -> Unit,
) : ListAdapter<UserEntity, EmployeeAdapter.EmployeeViewHolder>(PendingUserAdapter.UserDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEmployeeBinding.inflate(inflater, parent, false)
        return EmployeeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EmployeeViewHolder(
        private val binding: ItemEmployeeBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserEntity) {
            val context = binding.root.context
            binding.textInitials.text = Formatters.formatInitials(user.fullName)
            binding.textName.text = user.fullName
            binding.textInfo.text = context.getString(
                R.string.employee_info,
                user.username,
                user.role.displayName,
            )

            val canDelete = user.role == UserRole.OPERATOR
            binding.buttonDelete.visibility = if (canDelete) View.VISIBLE else View.GONE
            binding.buttonDelete.setOnClickListener { onDeleteClick(user) }
        }
    }
}
