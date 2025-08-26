package `in`.sendbirdpoc.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sendbird.android.channel.GroupChannel
import com.sendbird.uikit.activities.ChannelActivity
import com.sendbird.uikit.interfaces.providers.ChannelModuleProvider
import com.sendbird.uikit.modules.ChannelModule
import com.sendbird.uikit.modules.components.ChannelHeaderComponent
import com.sendbird.uikit.providers.ModuleProviders
import `in`.sendbirdpoc.databinding.ChatScreenHeaderBinding

/**
 * In this sample, the UI of the channel header is changed, and the title and description are updated.
 *
 * step 1. Create a [CustomChannelHeaderComponent] and set it to [ChannelModule.setHeaderComponent].
 * step 2. Create a [ChannelModuleProvider] and set it to [ModuleProviders.channel].
 * step 3. Start [ChannelActivity] with the channel url.
 *
 * The settings for the custom Provider are set up here to show the steps in the sample,
 * but in your application it is recommended to set it up in the Application class.
 */

/**
 * This class is used to customize the channel header.
 * In [onCreateView], you can inflate a custom view and return it.
 *
 * step 1. Inherit [ChannelHeaderComponent] and override [onCreateView].
 * step 2. Create a custom view and return it in [onCreateView].
 * (Optional) step 3. Override [notifyChannelChanged] and [notifyHeaderDescriptionChanged] to update the custom view.
 */
class CustomChannelHeaderComponent(
    val onlineMemberCount: Int,
    val onAddMemberClick: () -> Unit,
    val onBackClick: () -> Unit
) : ChannelHeaderComponent() {
    var binding: ChatScreenHeaderBinding? = null

    override fun onCreateView(
        context: Context,
        inflater: LayoutInflater,
        parent: ViewGroup,
        args: Bundle?
    ): View {
        binding = ChatScreenHeaderBinding.inflate(inflater, parent, false)

        binding?.ivAddMember?.setOnClickListener {
            onAddMemberClick.invoke()
        }

        binding?.ivBack?.setOnClickListener {
            onBackClick.invoke()
        }
        binding?.tvResponseTime?.text = "${onlineMemberCount} Online"

        return requireNotNull(binding).root
    }

    override fun notifyChannelChanged(channel: GroupChannel) {
        binding?.tvChannelName?.text = channel.name
    }

    override fun notifyHeaderDescriptionChanged(description: String?) {
    }
}
