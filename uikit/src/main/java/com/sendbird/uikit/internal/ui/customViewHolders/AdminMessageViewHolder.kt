package com.sendbird.uikit.internal.ui.customViewHolders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.Emoji
import com.sendbird.android.message.Reaction
import com.sendbird.android.user.User
import com.sendbird.uikit.activities.viewholder.GroupChannelMessageViewHolder
import com.sendbird.uikit.consts.ClickableViewIdentifier
import com.sendbird.uikit.test.databinding.SbViewAdminMessageBinding
import com.sendbird.uikit.interfaces.OnItemClickListener
import com.sendbird.uikit.interfaces.OnItemLongClickListener
import com.sendbird.uikit.internal.interfaces.OnFeedbackRatingClickListener
import com.sendbird.uikit.internal.ui.customeMessages.AdminMessageView
import com.sendbird.uikit.model.MessageListUIParams
import com.sendbird.uikit.test.databinding.SbViewOtherMessageBinding

internal class AdminMessageViewHolder constructor(
    val binding: SbViewAdminMessageBinding,
    messageListUIParams: MessageListUIParams
) : GroupChannelMessageViewHolder(binding.root, messageListUIParams) {
    override fun bind(channel: BaseChannel, message: BaseMessage, messageListUIParams: MessageListUIParams) {
        binding.adminMessageView.messageUIConfig = messageUIConfig
        if (channel is GroupChannel) {
            binding.adminMessageView.drawMessage(channel, message, messageListUIParams)
        }
    }

    override fun setEmojiReaction(
        reactionList: List<Reaction>,
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: View.OnClickListener?
    ) {
        // not-used anymore
    }

    override fun setEmojiReaction(
        reactionList: List<Reaction>,
        totalEmojiList: List<Emoji>,
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: View.OnClickListener?
    ) {
        binding.adminMessageView.binding.rvEmojiReactionList.apply {
            setReactionList(reactionList, totalEmojiList)
            setClickListeners(emojiReactionClickListener, emojiReactionLongClickListener, moreButtonClickListener)
        }
    }

    override fun getClickableViewMap(): Map<String, View> {
        return mapOf(
            ClickableViewIdentifier.Chat.name to binding.adminMessageView.binding.contentPanel,
            ClickableViewIdentifier.Profile.name to binding.adminMessageView.binding.ivProfileView,
            ClickableViewIdentifier.QuoteReply.name to binding.adminMessageView.binding.quoteReplyPanel,
            ClickableViewIdentifier.ThreadInfo.name to binding.adminMessageView.binding.threadInfo
        )
    }

    fun setOnMentionClickListener(listener: OnItemClickListener<User>?) {
        binding.adminMessageView.mentionClickListener = listener
    }

    fun setOnFeedbackRatingClickListener(listener: OnFeedbackRatingClickListener?) {
        binding.adminMessageView.onFeedbackRatingClickListener = listener
    }

    fun setOnSuggestedRepliesClickListener(listener: OnItemClickListener<String>?) {
        binding.adminMessageView.onSuggestedRepliesClickListener = listener
    }
}