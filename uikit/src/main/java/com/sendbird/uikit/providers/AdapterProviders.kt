package com.sendbird.uikit.providers

import com.sendbird.uikit.activities.adapter.*
import com.sendbird.uikit.interfaces.providers.*

/**
 * A set of Providers that provide a RecyclerView.Adapter that binds to a RecyclerView among the screens used in UIKit.
 *
 * @since 3.9.0
 */
object AdapterProviders {
    /**
     * Returns the MessageListAdapter provider.
     *
     * @return The [MessageListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var messageList: MessageListAdapterProvider

    /**
     * Returns the BannedUserListAdapter provider.
     *
     * @return The [BannedUserListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var bannedUserList: BannedUserListAdapterProvider

    /**
     * Returns the ChannelListAdapter provider.
     *
     * @return The [ChannelListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channelList: ChannelListAdapterProvider

    /**
     * Returns the CreateChannelUserListAdapter provider.
     *
     * @return The [CreateChannelUserListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var createChannelUserList: CreateChannelUserListAdapterProvider

    /**
     * Returns the InviteChannelFragment provider.
     *
     * @return The [InviteUserListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var inviteUserList: InviteUserListAdapterProvider

    /**
     * Returns the MemberListAdapter provider.
     *
     * @return The [MemberListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var memberList: MemberListAdapterProvider

    /**
     * Returns the MessageSearchAdapter provider.
     *
     * @return The [MessageSearchAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var messageSearch: MessageSearchAdapterProvider

    /**
     * Returns the MutedMemberListAdapter provider.
     *
     * @return The [MutedMemberListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var mutedMemberList: MutedMemberListAdapterProvider

    /**
     * Returns the OpenChannelBannedUserListAdapter provider.
     *
     * @return The [OpenChannelBannedUserListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelBannedUserList: OpenChannelBannedUserListAdapterProvider

    /**
     * Returns the OpenChannelListAdapter provider.
     *
     * @return The [OpenChannelListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelList: OpenChannelListAdapterProvider

    /**
     * Returns the OpenChannelMessageListAdapter provider.
     *
     * @return The [OpenChannelMessageListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelMessageList: OpenChannelMessageListAdapterProvider

    /**
     * Returns the OpenChannelMutedParticipantListAdapter provider.
     *
     * @return The [OpenChannelMutedParticipantListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelMutedParticipantList: OpenChannelMutedParticipantListAdapterProvider

    /**
     * Returns the OpenChannelRegisterOperatorListAdapter provider.
     *
     * @return The [OpenChannelRegisterOperatorListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelRegisterOperatorList: OpenChannelRegisterOperatorListAdapterProvider

    /**
     * Returns the OperatorListAdapter provider.
     *
     * @return The [OperatorListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var operatorList: OperatorListAdapterProvider

    /**
     * Returns the ParticipantListAdapter provider.
     *
     * @return The [ParticipantListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var participantList: ParticipantListAdapterProvider

    /**
     * Returns the RegisterOperatorListAdapter provider.
     *
     * @return The [RegisterOperatorListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var registerOperatorList: RegisterOperatorListAdapterProvider

    /**
     * Returns the ThreadListAdapter provider.
     *
     * @return The [ThreadListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var threadList: ThreadListAdapterProvider

    /**
     * Reset all providers to default provider.
     *
     * @since 3.10.1
     */
    @JvmStatic
    fun resetToDefault() {
        this.messageList = MessageListAdapterProvider { channel, messageListUIParams ->
            MessageListAdapter(channel, messageListUIParams)
        }
        this.bannedUserList = BannedUserListAdapterProvider { BannedUserListAdapter() }

        this.channelList = ChannelListAdapterProvider { uiParams ->
            ChannelListAdapter(null, uiParams)
        }

        this.createChannelUserList = CreateChannelUserListAdapterProvider { CreateChannelUserListAdapter() }

        this.inviteUserList = InviteUserListAdapterProvider { InviteUserListAdapter() }

        this.memberList = MemberListAdapterProvider { MemberListAdapter() }

        this.messageSearch = MessageSearchAdapterProvider { MessageSearchAdapter() }

        this.mutedMemberList = MutedMemberListAdapterProvider { MutedMemberListAdapter() }

        this.openChannelBannedUserList = OpenChannelBannedUserListAdapterProvider {
            OpenChannelBannedUserListAdapter()
        }

        this.openChannelList = OpenChannelListAdapterProvider { OpenChannelListAdapter() }

        this.openChannelMessageList = OpenChannelMessageListAdapterProvider { messageListUIParams ->
            OpenChannelMessageListAdapter(null, messageListUIParams)
        }

        this.openChannelMutedParticipantList = OpenChannelMutedParticipantListAdapterProvider { OpenChannelMutedParticipantListAdapter() }

        this.openChannelRegisterOperatorList = OpenChannelRegisterOperatorListAdapterProvider { channel ->
            OpenChannelRegisterOperatorListAdapter(channel)
        }

        this.operatorList = OperatorListAdapterProvider { OperatorListAdapter() }

        this.participantList = ParticipantListAdapterProvider { ParticipantListAdapter() }

        this.registerOperatorList = RegisterOperatorListAdapterProvider { RegisterOperatorListAdapter() }

        this.threadList = ThreadListAdapterProvider { messageListUIParams ->
            ThreadListAdapter(null, messageListUIParams)
        }
    }

    init {
        resetToDefault()
    }
}
