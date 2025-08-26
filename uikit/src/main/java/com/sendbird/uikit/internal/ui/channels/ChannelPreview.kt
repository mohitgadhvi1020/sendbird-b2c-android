package com.sendbird.uikit.internal.ui.channels

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.AdminMessage
import com.sendbird.android.message.BaseFileMessage
import com.sendbird.android.message.UserMessage
import com.sendbird.uikit.test.R
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.internal.extensions.getDisplayMessage
import com.sendbird.uikit.internal.extensions.removeMarkdownFormatting
import com.sendbird.uikit.internal.extensions.setAppearance
import com.sendbird.uikit.internal.extensions.toDisplayText
import com.sendbird.uikit.model.configurations.UIKitConfig.groupChannelConfig
import com.sendbird.uikit.utils.ChannelUtils
import com.sendbird.uikit.utils.DateUtils
import com.sendbird.uikit.utils.DrawableUtils
import com.sendbird.uikit.utils.MessageUtils

internal class ChannelPreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_channel_preview
) : FrameLayout(context, attrs, defStyle) {
    private val coverView: ChannelCoverView
    private val tvTitle: TextView
    private val tvMemberCount: TextView
    private val tvUpdatedAt: TextView
    private val tvLastMessage: TextView
    private val tvUnreadMentionCount: TextView
    private val tvUnreadCount: TextView
    private val ivPushEnabled: ImageView
    private val ivBroadcast: ImageView
    private val ivFrozen: ImageView
    private val ivLastMessageStatus: ImageView

    val layout: View
    var useTypingIndicator: Boolean = false
    var useMessageReceiptStatus: Boolean = false
    var useUnreadMentionCount: Boolean = false

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ChannelPreview, defStyle, 0)
        try {
            layout = LayoutInflater.from(getContext()).inflate(R.layout.sb_view_channel_list_item, this, false)
            addView(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            coverView = layout.findViewById(R.id.ivMediaSelector)
            tvTitle = layout.findViewById(R.id.tvTitle)
            tvMemberCount = layout.findViewById(R.id.tvMemberCount)
            ivPushEnabled = layout.findViewById(R.id.ivPushEnabledIcon)
            tvUpdatedAt = layout.findViewById(R.id.tvUpdatedAt)
            tvLastMessage = layout.findViewById(R.id.tvLastMessage)
            tvUnreadMentionCount = layout.findViewById(R.id.tvUnreadMentionCount)
            tvUnreadCount = layout.findViewById(R.id.tvUnreadCount)
            ivBroadcast = layout.findViewById(R.id.ivBroadcastIcon)
            ivFrozen = layout.findViewById(R.id.ivFrozenIcon)
            ivLastMessageStatus = layout.findViewById(R.id.ivLastMessageStatus)
            val background = a.getResourceId(
                R.styleable.ChannelPreview_sb_channel_preview_background, R.drawable.selector_rectangle_light
            )
            val titleAppearance = a.getResourceId(
                R.styleable.ChannelPreview_sb_channel_preview_title_appearance, R.style.SendbirdSubtitle1OnLight01
            )
            val memberCountAppearance = a.getResourceId(
                R.styleable.ChannelPreview_sb_channel_preview_member_count_appearance, R.style.SendbirdCaption1OnLight02
            )
            val updatedAtAppearance = a.getResourceId(
                R.styleable.ChannelPreview_sb_channel_preview_updated_at_appearance, R.style.SendbirdCaption2OnLight02
            )
            val unReadCountAppearance = a.getResourceId(
                R.styleable.ChannelPreview_sb_channel_preview_unread_count_appearance, R.style.SendbirdCaption1OnDark01
            )
            val unReadMentionCountAppearance = a.getResourceId(
                R.styleable.ChannelPreview_sb_channel_preview_unread_mention_count_appearance,
                R.style.SendbirdH2Primary300
            )
            val lastMessageAppearance = a.getResourceId(
                R.styleable.ChannelPreview_sb_channel_preview_last_message_appearance, R.style.SendbirdBody3OnLight03
            )
            layout.findViewById<View>(R.id.root).setBackgroundResource(background)
            tvTitle.setAppearance(context, titleAppearance)
            tvMemberCount.setAppearance(context, memberCountAppearance)
            tvUpdatedAt.setAppearance(context, updatedAtAppearance)
            tvUnreadMentionCount.setAppearance(context, unReadMentionCountAppearance)
            tvUnreadCount.setAppearance(context, unReadCountAppearance)
            tvLastMessage.setAppearance(context, lastMessageAppearance)
        } finally {
            a.recycle()
        }
    }

    fun drawChannel(channel: GroupChannel) {
        val context = context
        val lastMessage = channel.lastMessage
        val unreadMessageCount = channel.unreadMessageCount
        val unreadMentionCount = channel.unreadMentionCount
        ivPushEnabled.visibility = if (ChannelUtils.isChannelPushOff(channel)) VISIBLE else GONE
        val pushEnabledTint = SendbirdUIKit.getDefaultThemeMode().monoTintResId
        ivPushEnabled.setImageDrawable(
            DrawableUtils.setTintList(
                context, R.drawable.icon_notifications_off_filled, pushEnabledTint
            )
        )
        tvTitle.text =
            if (channel.isChatNotification) channel.name.ifEmpty { context.getString(R.string.sb_text_channel_list_title_unknown) } else ChannelUtils.makeTitleText(
                context, channel
            )
        tvUnreadCount.text =
            if (unreadMessageCount > 99) context.getString(R.string.sb_text_channel_list_unread_count_max) else unreadMessageCount.toString()
        tvUnreadCount.visibility = if (unreadMessageCount > 0) VISIBLE else GONE
        tvUnreadCount.setBackgroundResource(if (SendbirdUIKit.isDarkMode()) R.drawable.sb_shape_unread_message_count_dark else R.drawable.sb_shape_unread_message_count)
        ivFrozen.visibility = if (channel.isFrozen) VISIBLE else GONE
        ivBroadcast.visibility = if (channel.isBroadcast) VISIBLE else GONE
        ChannelUtils.loadChannelCover(coverView, channel)
        if (channel.isBroadcast) {
            val broadcastTint = SendbirdUIKit.getDefaultThemeMode().getSecondaryTintColorStateList(context)
            ivBroadcast.setImageDrawable(DrawableUtils.setTintList(context, R.drawable.icon_broadcast, broadcastTint))
        }
        if (channel.isFrozen) {
            val frozenTint = SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(context)
            ivFrozen.setImageDrawable(DrawableUtils.setTintList(context, R.drawable.icon_freeze, frozenTint))
        }
        val memberCount = channel.memberCount
        tvMemberCount.visibility = if (memberCount > 2) VISIBLE else GONE
        tvMemberCount.text = ChannelUtils.makeMemberCountText(channel.memberCount)
        tvUpdatedAt.text = DateUtils.formatDateTime(
            context, lastMessage?.createdAt ?: channel.createdAt
        )
        setLastMessage(tvLastMessage, channel, useTypingIndicator)
        ivLastMessageStatus.visibility = if (useMessageReceiptStatus) VISIBLE else GONE
        if (useMessageReceiptStatus) {
            lastMessage?.let {
                if (MessageUtils.isMine(lastMessage) && !channel.isSuper && channel.isGroupChannel) {
                    ivLastMessageStatus.visibility = VISIBLE
                    val unreadMemberCount = channel.getUnreadMemberCount(lastMessage)
                    val unDeliveredMemberCount = channel.getUndeliveredMemberCount(lastMessage)
                    when {
                        unreadMemberCount == 0 -> {
                            ivLastMessageStatus.setImageDrawable(
                                DrawableUtils.setTintList(
                                    getContext(),
                                    R.drawable.icon_done_all,
                                    SendbirdUIKit.getDefaultThemeMode().secondaryTintResId
                                )
                            )
                        }
                        unDeliveredMemberCount == 0 -> {
                            ivLastMessageStatus.setImageDrawable(
                                DrawableUtils.setTintList(
                                    getContext(),
                                    R.drawable.icon_done_all,
                                    SendbirdUIKit.getDefaultThemeMode().monoTintResId
                                )
                            )
                        }
                        else -> {
                            ivLastMessageStatus.setImageDrawable(
                                DrawableUtils.setTintList(
                                    getContext(),
                                    R.drawable.icon_done,
                                    SendbirdUIKit.getDefaultThemeMode().monoTintResId
                                )
                            )
                        }
                    }
                } else {
                    ivLastMessageStatus.visibility = GONE
                }
            }
        }
        if (useUnreadMentionCount) {
            tvUnreadMentionCount.text = SendbirdUIKit.getUserMentionConfig().trigger
            tvUnreadMentionCount.visibility = if (unreadMentionCount > 0) VISIBLE else GONE
        } else {
            tvUnreadMentionCount.visibility = GONE
        }
    }

    companion object {
        private fun setLastMessage(textView: TextView, channel: GroupChannel, useTypingIndicator: Boolean) {
            var message: CharSequence = ""
            if (useTypingIndicator) {
                val typingUsers = channel.typingUsers
                if (typingUsers.isNotEmpty()) {
                    message = ChannelUtils.makeTypingText(textView.context, typingUsers)
                    textView.text = message
                    return
                }
            }

            channel.lastMessage?.let {
                when (it) {
                    is AdminMessage,
                    is UserMessage -> {
                        textView.maxLines = 2
                        textView.ellipsize = TextUtils.TruncateAt.END
                        message = it.getDisplayMessage().run {
                            if (!groupChannelConfig.enableMarkdownForUserMessage) this
                            else this.removeMarkdownFormatting()
                        }
                    }

                    is BaseFileMessage -> {
                        textView.maxLines = 1
                        textView.ellipsize = TextUtils.TruncateAt.MIDDLE
                        message = it.toDisplayText(textView.context)
                    }
                }
            }
            textView.text = message
        }
    }
}
