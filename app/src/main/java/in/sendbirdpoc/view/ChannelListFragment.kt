package `in`.sendbirdpoc.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.doAfterTextChanged
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.ChannelType
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.channel.query.GroupChannelListQueryOrder
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.GroupChannelHandler
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.Reaction
import com.sendbird.android.message.SendingStatus
import com.sendbird.android.params.GroupChannelListQueryParams
import com.sendbird.android.params.GroupChannelUpdateParams
import com.sendbird.android.params.MessageSearchQueryParams
import com.sendbird.android.user.User
import com.sendbird.uikit.activities.ChannelActivity
import com.sendbird.uikit.activities.adapter.MessageListAdapter
import com.sendbird.uikit.activities.viewholder.GroupChannelMessageViewHolder
import com.sendbird.uikit.activities.viewholder.MessageType
import com.sendbird.uikit.activities.viewholder.MessageViewHolder
import com.sendbird.uikit.consts.ClickableViewIdentifier
import com.sendbird.uikit.fragments.ChannelFragment
import com.sendbird.uikit.interfaces.OnItemClickListener
import com.sendbird.uikit.interfaces.OnItemLongClickListener
import com.sendbird.uikit.interfaces.providers.ChannelModuleProvider
import com.sendbird.uikit.model.MessageListUIParams
import com.sendbird.uikit.model.MessageUIConfig
import com.sendbird.uikit.model.TextUIConfig
import com.sendbird.uikit.modules.ChannelModule
import com.sendbird.uikit.providers.ModuleProviders
import dagger.hilt.android.AndroidEntryPoint
import `in`.sendbirdpoc.R
import `in`.sendbirdpoc.adapter.ChannelAdapter
import `in`.sendbirdpoc.adapter.ChannelFilterOptionsAdapter
import `in`.sendbirdpoc.base.BaseFragment
import `in`.sendbirdpoc.databinding.FragmentChannelListBinding
import `in`.sendbirdpoc.databinding.ViewCustomMessageMeBinding
import `in`.sendbirdpoc.model.PropertyListResponse

@AndroidEntryPoint
class ChannelListFragment :
    BaseFragment<FragmentChannelListBinding>(R.layout.fragment_channel_list) {

    private lateinit var adapterChannelListAdapter: ChannelAdapter
    private lateinit var adapterChannelFilterOptionsAdapter: ChannelFilterOptionsAdapter
    private var starredChannelCount = 0
    private var allChannelCount = 0
    private var archivedChannelCount = 0
    private var unreadChannelCount = 0

    override fun setUpViews() {
        binding.apply {
            options.ivStarUnstar.setOnClickListener {
                adapterChannelListAdapter.getLongPressSelectedChannel().forEach {
                    GroupChannel.getChannel(it) { channel, e ->
                        if (e != null) {
                            return@getChannel
                        }

                        val existingData = Gson().fromJson<PropertyListResponse.Property>(
                            channel?.data,
                            object : TypeToken<PropertyListResponse.Property>() {}.type
                        )

                        existingData.starred = !existingData.starred

                        val params = GroupChannelUpdateParams().apply {
                            data = Gson().toJson(existingData)
                        }

                        channel?.updateChannel(params) { updatedChannel, updateError ->
                            if (updateError != null) {
                                return@updateChannel
                            } else {
                                getChannelList()
                                adapterChannelListAdapter.setLongPressActiveFlagFalse()
                                options.root.visibility = View.GONE
                                toolbar.root.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }

            options.ivArchive.setOnClickListener {
                adapterChannelListAdapter.getLongPressSelectedChannel().forEach {
                    GroupChannel.getChannel(it) { channel, e ->
                        if (e != null) {
                            return@getChannel
                        }

                        val existingData = Gson().fromJson<PropertyListResponse.Property>(
                            channel?.data,
                            object : TypeToken<PropertyListResponse.Property>() {}.type
                        )

                        existingData.archived = !existingData.archived

                        val params = GroupChannelUpdateParams().apply {
                            data = Gson().toJson(existingData)
                        }

                        channel?.updateChannel(params) { updatedChannel, updateError ->
                            if (updateError != null) {
                                return@updateChannel
                            } else {
                                getChannelList()
                                adapterChannelListAdapter.setLongPressActiveFlagFalse()
                                options.root.visibility = View.GONE
                                toolbar.root.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }

            options.ivBack.setOnClickListener {
                adapterChannelListAdapter.setLongPressActiveFlagFalse()
                options.root.visibility = View.GONE
                toolbar.root.visibility = View.VISIBLE
            }

            edtSearch.doAfterTextChanged {
                val query = it.toString()
                if (query.isNotEmpty()) {
                    showProgressDialog()
                    val params = MessageSearchQueryParams(
                        keyword = query
                    )
                    val searchQuery = SendbirdChat.createMessageSearchQuery(params)

                    searchQuery.next { messages, e ->
                        if (e != null) {
                            Log.e("Sendbird", "Search error: ${e.message}")
                            showProgressDialog()
                            return@next
                        }

                        val filteredChannelList =
                            messages?.map { message -> message.channelUrl } ?: emptyList()
                        val filteredList = adapterChannelListAdapter.getData()
                            .filter { channelData -> filteredChannelList.contains(channelData.url) }

                        adapterChannelListAdapter.setData(filteredList)
                        hideProgressDialog()
                    }
                } else {
                    getChannelList()
                }
            }

            adapterChannelListAdapter = ChannelAdapter(
                mutableListOf(),
                object : ChannelAdapter.OnItemClickListener {
                    override fun onItemCLick(
                        data: GroupChannel,
                        isLongPressActive: Boolean,
                        position: Int
                    ) {
                        if (isLongPressActive) {
                            if (adapterChannelListAdapter.getLongPressSelectedChannel()
                                    .isNotEmpty()
                            ) {
                                options.tvCount.text =
                                    adapterChannelListAdapter.getLongPressSelectedChannel().size.toString()

                                options.root.visibility = View.VISIBLE
                                toolbar.root.visibility = View.GONE

                            } else {
                                options.root.visibility = View.GONE
                                toolbar.root.visibility = View.VISIBLE
                            }
                        } else {
                            val onlineMemberCount = data.members.count {
                                it.connectionStatus == User.ConnectionStatus.ONLINE
                            }
                            ModuleProviders.channel = ChannelModuleProvider { context, _ ->
                                val module = ChannelModule(context)
                                module.setHeaderComponent(
                                    CustomChannelHeaderComponent(
                                        onlineMemberCount = onlineMemberCount,
                                        onAddMemberClick = {
                                            showProgressDialog()
                                            val query = data.createMutedUserListQuery(
                                                limit = 5
                                            )

                                            if (query.hasNext) {
                                                query.next { users: List<User>?, e: SendbirdException? ->
                                                    val isUserMuted =
                                                        users?.any { it.userId == (pref.loginUserSendbirdId) } == true

                                                    if (isUserMuted) {
                                                        hideProgressDialog()
                                                        Toast.makeText(
                                                            requireContext(),
                                                            "You are block in this channel",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        hideProgressDialog()
                                                        startActivity(
                                                            Intent(
                                                                requireContext(),
                                                                ManageMemberActivity::class.java
                                                            ).putExtra("channelUrl", data.url)
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        onBackClick = {
                                            requireActivity().supportFragmentManager.popBackStack()
                                        })
                                )
                                module
                            }

                            startActivity(ChannelActivity.newIntent(requireActivity(), data.url))
                        }
                    }

                    override fun onLongPressCLick(
                        data: GroupChannel,
                        position: Int
                    ) {

                        val channelData = Gson().fromJson<PropertyListResponse.Property>(
                            data.data,
                            object : TypeToken<PropertyListResponse.Property>() {}.type
                        )
                        if (channelData.starred) {
                            options.ivStarUnstar.setImageDrawable(
                                AppCompatResources.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_unstar
                                )
                            )
                        } else {
                            options.ivStarUnstar.setImageDrawable(
                                AppCompatResources.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_star
                                )
                            )
                        }

                        if (adapterChannelListAdapter.getLongPressSelectedChannel().isNotEmpty()) {
                            options.tvCount.text =
                                adapterChannelListAdapter.getLongPressSelectedChannel().size.toString()

                            options.root.visibility = View.VISIBLE
                            toolbar.root.visibility = View.GONE

                        } else {
                            options.root.visibility = View.GONE
                            toolbar.root.visibility = View.VISIBLE
                        }
                    }
                })

            rvChannelList.adapter = adapterChannelListAdapter

            adapterChannelFilterOptionsAdapter = ChannelFilterOptionsAdapter(
                mutableListOf(),
                object : ChannelFilterOptionsAdapter.OnItemClickListener {
                    override fun onItemCLick(
                        data: String,
                        position: Int
                    ) {
                        adapterChannelFilterOptionsAdapter.updateMenuBackground(position)
                        adapterChannelListAdapter.setLongPressActiveFlagFalse()
                        options.root.visibility = View.GONE
                        toolbar.root.visibility = View.VISIBLE

                        when (position) {
                            0 -> {
                                getChannelList()
                            }

                            1 -> {
                                adapterChannelListAdapter.setData(
                                    adapterChannelListAdapter.getData()
                                        .filter { it.unreadMessageCount > 0 }
                                )
                            }

                            2 -> {
                                adapterChannelListAdapter.setData(
                                    adapterChannelListAdapter.getData().filter {
                                        Gson().fromJson<PropertyListResponse.Property>(
                                            it.data,
                                            object :
                                                TypeToken<PropertyListResponse.Property>() {}.type
                                        ).starred == true
                                    }
                                )
                            }

                            3 -> {
                                adapterChannelListAdapter.setData(
                                    adapterChannelListAdapter.getData().filter {
                                        Gson().fromJson<PropertyListResponse.Property>(
                                            it.data,
                                            object :
                                                TypeToken<PropertyListResponse.Property>() {}.type
                                        ).archived == true
                                    }
                                )
                            }
                        }
                    }
                })

            rvFilterOptionsList.adapter = adapterChannelFilterOptionsAdapter
        }
    }


    class MessageUISampleFragment : ChannelFragment() {
        override fun onCreateModule(args: Bundle): ChannelModule {
            val params = ChannelModule.Params(requireContext(), R.style.AppTheme_Sendbird_Custom)
            return ChannelModule(requireContext(), params)
        }
    }

    /**
     * You can customize a message list using the [MessageListUIParams.Builder].
     * [MessageListUIParams] can be applied as an argument to the constructor of [MessageListAdapter].
     */
    val customMessageListUIParams = MessageListUIParams.Builder()
        .setUseMessageGroupUI(false)
        .build()

    /**
     * Through [MessageUIConfig], you can customize text properties
     * such as text color, font, and style and the background used in UIKit messages.
     */
    val customMessageUIConfig = MessageUIConfig().apply {
        val textUIConfig = TextUIConfig.Builder()
            .setTextColor(Color.RED)
            .build()
        this.otherNicknameTextUIConfig.apply(textUIConfig)


    }

    private fun getChannelList() {
        val query = GroupChannel.createMyGroupChannelListQuery(
            GroupChannelListQueryParams().apply {
                includeEmpty = false
                order = GroupChannelListQueryOrder.LATEST_LAST_MESSAGE
            }
        )

        query.next { channels, e ->
            if (e != null) {
                Log.e("Sendbird", "Failed to fetch channels: ${e.message}")
                hideProgressDialog()
                return@next
            }

            channels?.let {

                allChannelCount = it.size
                unreadChannelCount = it.map { it.unreadMessageCount > 0 }.count { it }

                starredChannelCount = it.map {
                    Gson().fromJson<PropertyListResponse.Property>(
                        it.data,
                        object : TypeToken<PropertyListResponse.Property>() {}.type
                    ).starred == true
                }.count { it }

                archivedChannelCount = it.map {
                    Gson().fromJson<PropertyListResponse.Property>(
                        it.data,
                        object : TypeToken<PropertyListResponse.Property>() {}.type
                    ).archived == true
                }.count { it }

                adapterChannelFilterOptionsAdapter.setData(
                    listOf(
                        "All (${allChannelCount})",
                        "Unread (${unreadChannelCount})",
                        "Starred (${starredChannelCount})",
                        "Archived (${archivedChannelCount})"
                    )
                )

                adapterChannelListAdapter.setData(it)
            }

            hideProgressDialog()
        }

        SendbirdChat.addChannelHandler("CHANNEL_LIST_HANDLER", object : GroupChannelHandler() {
            override fun onMessageReceived(channel: BaseChannel, message: BaseMessage) {
                if (channel is GroupChannel) {
                    getChannelList()
                }
            }



            override fun onChannelChanged(channel: BaseChannel) {
                super.onChannelChanged(channel)
                getChannelList()
            }

            override fun onChannelDeleted(channelUrl: String, channelType: ChannelType) {
                super.onChannelDeleted(channelUrl, channelType)
                getChannelList()
            }

            override fun onUserLeft(channel: GroupChannel, user: User) {
                super.onUserLeft(channel, user)
                getChannelList()
            }

            override fun onUserJoined(channel: GroupChannel, user: User) {
                super.onUserJoined(channel, user)
                getChannelList()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        SendbirdChat.removeChannelHandler("CHANNEL_LIST_HANDLER")
    }

    override fun onDestroy() {
        super.onDestroy()
        SendbirdChat.removeChannelHandler("CHANNEL_LIST_HANDLER")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        SendbirdChat.removeChannelHandler("CHANNEL_LIST_HANDLER")
    }

    class MessageUISampleAdapter(
        channel: GroupChannel?,
        uiParams: MessageListUIParams
    ) : MessageListAdapter(channel, uiParams) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (MessageType.from(viewType)) {
                MessageType.VIEW_TYPE_USER_MESSAGE_ME -> CustomMessageMeViewHolder(
                    ViewCustomMessageMeBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                )

                else -> super.onCreateViewHolder(parent, viewType)
            }
        }

        class CustomMessageMeViewHolder(
            val binding: ViewCustomMessageMeBinding
        ) : GroupChannelMessageViewHolder(binding.root) {
            override fun setEmojiReaction(
                reactionList: List<Reaction>,
                emojiReactionClickListener: OnItemClickListener<String>?,
                emojiReactionLongClickListener: OnItemLongClickListener<String>?,
                moreButtonClickListener: View.OnClickListener?
            ) {
            }

            override fun bind(
                channel: BaseChannel,
                message: BaseMessage,
                params: MessageListUIParams
            ) {
                val context = binding.getRoot().context
                val sendingState = message.sendingStatus == SendingStatus.SUCCEEDED

                binding.tvSentAt.visibility = if (sendingState) View.VISIBLE else View.GONE
                val sentAt =
                    DateUtils.formatDateTime(context, message.createdAt, DateUtils.FORMAT_SHOW_TIME)
                binding.tvSentAt.text = sentAt
                binding.tvMessage.text = message.message
            }

            override fun getClickableViewMap(): Map<String, View> =
                mapOf(ClickableViewIdentifier.Chat.name to binding.tvMessage)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!adapterChannelListAdapter.getLongPressActiveFlag() && binding.edtSearch.text?.isEmpty() == true) {
            showProgressDialog()
            getChannelList()
        }
    }
}