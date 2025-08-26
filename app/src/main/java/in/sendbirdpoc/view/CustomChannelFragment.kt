package `in`.sendbirdpoc.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.handler.GroupChannelHandler
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.user.RestrictedUser
import com.sendbird.android.user.User
import com.sendbird.uikit.fragments.ChannelFragment
import dagger.hilt.android.AndroidEntryPoint
import `in`.sendbirdpoc.data.AppSharedPref
import javax.inject.Inject

@AndroidEntryPoint
class CustomChannelFragment : ChannelFragment() {

    @Inject
    lateinit var pref: AppSharedPref

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        SendbirdChat.addChannelHandler("REMOVAL_HANDLER", object : GroupChannelHandler() {
            override fun onMessageReceived(
                channel: BaseChannel,
                message: BaseMessage
            ) {

            }

            override fun onUserBanned(channel: BaseChannel, restrictedUser: RestrictedUser) {
                super.onUserBanned(channel, restrictedUser)
                if (pref.loginUserSendbirdId == restrictedUser.userId) {
                    handleUserRemoved()
                }
            }

            override fun onUserLeft(channel: GroupChannel, user: User) {
                super.onUserLeft(channel, user)
                if (pref.loginUserSendbirdId == user.userId) {
                    handleUserRemoved()
                }
            }
        })
    }

    private fun handleUserRemoved() {
        Toast.makeText(requireContext(), "You were removed from this channel", Toast.LENGTH_SHORT)
            .show()
        requireActivity().finish()
        startActivity(Intent(requireContext(), MainActivity::class.java))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        SendbirdChat.removeChannelHandler("REMOVAL_HANDLER")
    }
}
