package `in`.sendbirdpoc.util

import android.util.Log
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter
import com.sendbird.uikit.interfaces.UserInfo

class SendbirdUIKitAdapterImpl(val userID: String, val name: String) : SendbirdUIKitAdapter {
    override fun getAppId(): String {
        return "F4174B90-8E71-40A3-B07E-A35F6AA9AAE3"
    }

    override fun getAccessToken(): String? {
        return null
    }

    override fun getUserInfo(): UserInfo {
        return object : UserInfo {
            override fun getUserId(): String = userID

            override fun getNickname(): String? = name

            override fun getProfileUrl(): String? = ""

        }
    }

    override fun getInitResultHandler(): InitResultHandler {
        return object : InitResultHandler {
            override fun onInitFailed(e: SendbirdException) {
                if (e != null) {
                    Log.e("SendbirdInit", "Sendbird initialization failed: ${e.message}", e)
                } else {
                    Log.d("SendbirdInit", "Sendbird UIKit initialized successfully")
                }
            }

            override fun onInitSucceed() {}

            override fun onMigrationStarted() {}

        }
    }


}