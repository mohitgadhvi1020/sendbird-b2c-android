package `in`.sendbirdpoc.view

import android.content.Intent
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import `in`.sendbirdpoc.R
import `in`.sendbirdpoc.api.utils.State
import `in`.sendbirdpoc.base.BaseActivity
import `in`.sendbirdpoc.databinding.ActivityLoginBinding
import `in`.sendbirdpoc.viewmodels.LoginViewModel

@AndroidEntryPoint
class LoginActivity :
    BaseActivity<ActivityLoginBinding>(R.layout.activity_login) {

    private val viewModel by viewModels<LoginViewModel>()

    override fun setUpViews() {

    }

    override fun setUpListeners() {
        binding.btnContinue.setOnClickListener {

            if (binding.etEmail.text.toString().trim().isEmpty()) {
                showToastShort("Enter email")
            } else if (binding.etPassword.text.toString().trim().isEmpty()) {
                showToastShort("Enter password")
            } else {
                viewModel.login(
                    mutableMapOf(
                        "email" to binding.etEmail.text.toString().trim(),
                        "password" to binding.etPassword.text.toString().trim(),
                    )
                )
            }
        }

        binding.tvSkip.setOnClickListener {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }

    }

    override fun setUpObservers() {

        viewModel.loginResponse.observe(this) { response ->
            when (response) {
                is State.Loading -> {
                    showProgressDialog()
                }

                is State.Error -> {
                    hideProgressDialog()
                }

                is State.Success -> {
                    hideProgressDialog()
                    pref.isLoggedIn = true
                    pref.loginUserSendbirdId = response.data.data.user.send_bird_id
                    pref.loginUserSendbirdAccessToken = response.data.data.user.send_bird_accessId
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }

                else -> {

                }
            }
        }

    }

    override fun performApiCall() {

    }
}