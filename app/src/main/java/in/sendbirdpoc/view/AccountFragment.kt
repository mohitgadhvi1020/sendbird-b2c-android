package `in`.sendbirdpoc.view

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import dagger.hilt.android.AndroidEntryPoint
import `in`.sendbirdpoc.R
import `in`.sendbirdpoc.base.BaseFragment
import `in`.sendbirdpoc.databinding.FragmentAccountBinding

@AndroidEntryPoint
class AccountFragment :
    BaseFragment<FragmentAccountBinding>(R.layout.fragment_account) {

    override fun setUpViews() {
        binding.apply {
            tvLogout.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Logout?")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        dialog.dismiss()
                        pref.isLoggedIn = false
                        startActivity(
                            Intent(requireContext(), LoginActivity::class.java).setFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            )
                        )
                        requireActivity().finish()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }
}