package bg.uni_plovdiv.fmi.tapetransfer.ui.profile

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
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.UserEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.settings.ThemeMode
import bg.uni_plovdiv.fmi.tapetransfer.databinding.FragmentProfileBinding
import bg.uni_plovdiv.fmi.tapetransfer.util.Formatters
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private val adapter = PendingUserAdapter(
        onApproveClick = { user -> approve(user) },
        onRejectClick = { user -> confirmReject(user) },
    )
    private val employeeAdapter = EmployeeAdapter(
        onDeleteClick = { user -> confirmDeleteEmployee(user) },
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener { findNavController().popBackStack() }
        binding.buttonLogout.setOnClickListener { viewModel.logout() }
        binding.buttonPriceList.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_priceList)
        }

        binding.recyclerPending.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPending.adapter = adapter
        binding.recyclerEmployees.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerEmployees.adapter = employeeAdapter

        setupThemeButtons()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { state -> render(state) } }
                launch {
                    viewModel.isLoggedOut.collect { loggedOut ->
                        val isOnProfileScreen = findNavController().currentDestination?.id == R.id.profileFragment
                        if (loggedOut && isOnProfileScreen) {
                            findNavController().navigate(R.id.action_profile_to_login)
                        }
                    }
                }
            }
        }
    }

    private fun setupThemeButtons() {
        val checkedId = when (viewModel.getThemeMode()) {
            ThemeMode.LIGHT -> R.id.buttonThemeLight
            ThemeMode.DARK -> R.id.buttonThemeDark
            ThemeMode.SYSTEM -> R.id.buttonThemeSystem
        }
        binding.toggleTheme.check(checkedId)

        binding.toggleTheme.addOnButtonCheckedListener { _, buttonId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val mode = when (buttonId) {
                R.id.buttonThemeLight -> ThemeMode.LIGHT
                R.id.buttonThemeDark -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
            viewModel.setThemeMode(mode)
        }
    }

    private fun render(state: ProfileUiState) {
        val user = state.user ?: return

        binding.textInitials.text = Formatters.formatInitials(user.fullName)
        binding.textFullName.text = user.fullName
        binding.textUsername.text = getString(R.string.profile_username, user.username)
        binding.textRole.text = user.role.displayName

        binding.sectionPending.visibility = if (state.isAdmin) View.VISIBLE else View.GONE
        binding.textPendingCount.text = state.pendingUsers.size.toString()
        adapter.submitList(state.pendingUsers)
        binding.textPendingEmpty.visibility =
            if (state.pendingUsers.isEmpty()) View.VISIBLE else View.GONE

        binding.sectionEmployees.visibility = if (state.isAdmin) View.VISIBLE else View.GONE
        binding.textEmployeesCount.text = state.employees.size.toString()
        employeeAdapter.submitList(state.employees)
        binding.textEmployeesEmpty.visibility =
            if (state.employees.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun approve(user: UserEntity) {
        viewModel.approve(user)
        Toast.makeText(
            requireContext(),
            getString(R.string.pending_approved, user.fullName),
            Toast.LENGTH_SHORT,
        ).show()
    }

    private fun confirmReject(user: UserEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.reject_title)
            .setMessage(getString(R.string.reject_message, user.fullName))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.reject_confirm) { _, _ -> viewModel.reject(user) }
            .show()
    }

    private fun confirmDeleteEmployee(user: UserEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.employee_delete_title)
            .setMessage(getString(R.string.employee_delete_message, user.fullName))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete_confirm) { _, _ -> viewModel.deleteEmployee(user) }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerPending.adapter = null
        binding.recyclerEmployees.adapter = null
        _binding = null
    }
}
