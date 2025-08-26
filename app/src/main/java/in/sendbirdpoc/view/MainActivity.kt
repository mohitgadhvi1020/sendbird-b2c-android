package `in`.sendbirdpoc.view

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.sendbird.android.LogLevel
import com.sendbird.android.SendbirdChat
import com.sendbird.android.exception.SendbirdError
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.android.params.InitParams
import com.sendbird.uikit.SendbirdUIKit
import dagger.hilt.android.AndroidEntryPoint
import `in`.sendbirdpoc.R
import `in`.sendbirdpoc.base.BaseActivity
import `in`.sendbirdpoc.databinding.ActivityMainBinding
import `in`.sendbirdpoc.util.SendbirdUIKitAdapterImpl
import `in`.sendbirdpoc.util.Utils.replaceFragment

@AndroidEntryPoint
class MainActivity :
    BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    override fun setUpViews() {
        if (pref.isLoggedIn) {

            SendbirdUIKit.init(
                SendbirdUIKitAdapterImpl(pref.loginUserSendbirdId ?: "", ""),
                this@MainActivity
            )

            SendbirdChat.init(
                InitParams(
                    "F4174B90-8E71-40A3-B07E-A35F6AA9AAE3",
                    applicationContext,
                    logLevel = LogLevel.VERBOSE,
                    useCaching = true
                ),
                object : InitResultHandler {
                    override fun onMigrationStarted() {
                        Log.i("Application", "Called when there's an migration in local cache.")
                    }

                    override fun onInitFailed(e: SendbirdException) {
                        when (e.code) {
                            SendbirdError.ERR_INITIALIZATION_CANCELED -> {
                                Log.i(
                                    "Application",
                                    "Called when initialize failed for some reason such as empth APP_ID. Retry SendbirdChat.init() again with correct InitParams."
                                )
                            }

                            SendbirdError.ERR_DATABASE_ERROR_ENCRYPTION -> {
                                Log.i(
                                    "Application",
                                    "Called when initialize failed when InitParams.LocalCacheConfig.SqlcipherConfig is set but SqlCipher dependency is not set. Please add the dependency and try SendbirdChat.init again."
                                )
                            }

                            else -> {
                                Log.i(
                                    "Application",
                                    "Called when initialize failed. SDK will still operate properly as if useLocalCaching is set to false."
                                )
                            }
                        }
                    }

                    override fun onInitSucceed() {
                        Log.i("Application", "Called when initialization is completed.")

                        SendbirdChat.connect(
                            userId = pref.loginUserSendbirdId ?: "",
                            authToken = pref.loginUserSendbirdAccessToken
                        ) { _, e ->
                            if (e != null) {
                                e.printStackTrace()
                                Toast.makeText(
                                    this@MainActivity,
                                    "Failed to connect to Sendbird",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@connect
                            }
                        }
                    }
                }
            )
        }


        if (intent.extras?.getBoolean("isFromMemberList") == true) {
            binding.bottomNavigation.selectedItemId = R.id.chat
            replaceFragment(ChannelListFragment(), R.id.fcvHome, null, false)
        } else {
            replaceFragment(PropertyListFragment(), R.id.fcvHome, null, false)
        }
    }

    override fun setUpListeners() {
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.search -> replaceFragment(PropertyListFragment(), R.id.fcvHome, null, false)
                R.id.chat -> {

                    if (!pref.isLoggedIn) {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        replaceFragment(
                            ChannelListFragment(),
                            R.id.fcvHome,
                            null,
                            false
                        )
                    }
                }

                R.id.account -> {

                    if (pref.isLoggedIn) {
                        replaceFragment(
                            AccountFragment(),
                            R.id.fcvHome,
                            null,
                            true
                        )
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Please login to continue",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            true
        }
    }

    override fun setUpObservers() {
    }

    override fun performApiCall() {

    }
}