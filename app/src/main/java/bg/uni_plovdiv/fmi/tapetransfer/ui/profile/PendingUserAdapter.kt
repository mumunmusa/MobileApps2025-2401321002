package bg.uni_plovdiv.fmi.tapetransfer.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bg.uni_plovdiv.fmi.tapetransfer.R
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.UserEntity
import bg.uni_plovdiv.fmi.tapetransfer.databinding.ItemPendingUserBinding
import bg.uni_plovdiv.fmi.tapetransfer.util.Formatters

class PendingUserAdapter(
    private val onApproveClick: (UserEntity) -> Unit,
    private val onRejectClick: (UserEntity) -> Unit,
) : ListAdapter<UserEntity, PendingUserAdapter.UserViewHolder>(UserDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPendingUserBinding.inflate(inflater, parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(
        private val binding: ItemPendingUserBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserEntity) {
            val context = binding.root.context
            binding.textInitials.text = Formatters.formatInitials(user.fullName)
            binding.textName.text = user.fullName
            binding.textInfo.text = context.getString(
                R.string.pending_user_info,
                user.username,
                Formatters.formatDate(user.createdAt),
            )
            binding.buttonApprove.setOnClickListener { onApproveClick(user) }
            binding.buttonReject.setOnClickListener { onRejectClick(user) }
        }
    }

    object UserDiffCallback : DiffUtil.ItemCallback<UserEntity>() {
        override fun areItemsTheSame(oldItem: UserEntity, newItem: UserEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserEntity, newItem: UserEntity): Boolean {
            return oldItem == newItem
        }
    }
}
