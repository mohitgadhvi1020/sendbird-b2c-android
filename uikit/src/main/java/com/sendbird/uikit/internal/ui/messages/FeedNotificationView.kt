package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import com.sendbird.android.channel.FeedChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.test.R
import com.sendbird.uikit.test.databinding.SbViewFeedNotificationComponentBinding
import com.sendbird.uikit.interfaces.OnNotificationTemplateActionHandler
import com.sendbird.uikit.internal.extensions.addRipple
import com.sendbird.uikit.internal.extensions.setAppearance
import com.sendbird.uikit.internal.extensions.setTypeface
import com.sendbird.uikit.internal.model.notifications.NotificationConfig
import com.sendbird.uikit.internal.model.notifications.NotificationThemeMode
import com.sendbird.uikit.utils.DateUtils
import com.sendbird.uikit.utils.DrawableUtils
import com.sendbird.uikit.utils.MessageUtils

internal class FeedNotificationView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_feed_notification
) : BaseNotificationView(context, attrs, defStyle) {
    override val binding: SbViewFeedNotificationComponentBinding
    override val layout: View
        get() = binding.root

    var onNotificationTemplateActionHandler: OnNotificationTemplateActionHandler? = null

    init {
        val a =
            context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_FeedNotification, defStyle, 0)
        try {
            binding = SbViewFeedNotificationComponentBinding.inflate(
                LayoutInflater.from(context),
                this,
                true
            )
            val messageBackground = a.getResourceId(
                R.styleable.MessageView_FeedNotification_sb_feed_notification_background,
                R.color.background_100
            )
            val leftCaptionAppearance = a.getResourceId(
                R.styleable.MessageView_FeedNotification_sb_feed_notification_category_text_appearance,
                R.style.SendbirdCaption1OnLight02
            )
            val rightCaptionAppearance = a.getResourceId(
                R.styleable.MessageView_FeedNotification_sb_feed_notification_sent_at_text_appearance,
                R.style.SendbirdCaption4OnLight03
            )
            val bubbleRadius = a.getDimensionPixelSize(
                R.styleable.MessageView_FeedNotification_sb_feed_notification_radius,
                context.resources.getDimensionPixelSize(R.dimen.sb_size_8)
            )
            val unreadIndicatorColor = a.getResourceId(
                R.styleable.MessageView_FeedNotification_sb_feed_notification_unread_indicator_color,
                R.color.secondary_main
            )

            binding.contentPanel.setBackgroundResource(messageBackground)
            binding.tvLabel.setAppearance(context, leftCaptionAppearance)
            binding.tvSentAt.setAppearance(context, rightCaptionAppearance)
            binding.contentPanel.radius = bubbleRadius.toFloat()
            binding.ivUnreadIndicator.background = DrawableUtils.createOvalIcon(context, unreadIndicatorColor)
        } finally {
            a.recycle()
        }
    }

    @JvmOverloads
    fun drawMessage(message: BaseMessage, channel: FeedChannel, lastSeen: Long, config: NotificationConfig? = null) {
        binding.tvLabel.text = MessageUtils.getNotificationLabel(message)
        binding.tvLabel.visibility = if (channel.isTemplateLabelEnabled) View.VISIBLE else View.INVISIBLE
        binding.tvSentAt.text = DateUtils.formatDateTime(context, message.createdAt)
        binding.ivUnreadIndicator.visibility =
            if (message.createdAt > lastSeen) View.VISIBLE else View.GONE

        // UI padding is different when category filter is enabled
        if (channel.isCategoryFilterEnabled && channel.notificationCategories.isNotEmpty()) {
            binding.root.setPaddingRelative(
                binding.root.paddingStart,
                0,
                binding.root.paddingEnd,
                context.resources.getDimensionPixelSize(R.dimen.sb_size_16)
            )
        } else {
            binding.root.setPaddingRelative(
                binding.root.paddingStart,
                context.resources.getDimensionPixelSize(R.dimen.sb_size_8),
                binding.root.paddingEnd,
                context.resources.getDimensionPixelSize(R.dimen.sb_size_8)
            )
        }

        // apply config
        config?.let {
            it.theme.notificationTheme.apply {
                val themeMode = config.themeMode
                val fontStyle = label ?: category
                fontStyle.apply {
                    binding.tvLabel.setTextColor(textColor.getColor(themeMode))
                    binding.tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
                    binding.tvLabel.setTypeface(fontWeight.value)
                }
                sentAt.apply {
                    binding.tvSentAt.setTextColor(textColor.getColor(themeMode))
                    binding.tvSentAt.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
                    binding.tvSentAt.setTypeface(fontWeight.value)
                }
                unreadIndicatorColor.getColor(themeMode).apply {
                    val color = this
                    binding.ivUnreadIndicator.background = ShapeDrawable(OvalShape()).apply {
                        paint.color = color
                    }
                }

                val theme = this
                binding.contentPanel.apply {
                    isClickable = true
                    isFocusable = true
                    setRadiusIntSize(theme.radius)
                    setBackgroundColor(theme.backgroundColor.getColor(themeMode))
                    addRipple(theme.pressedColor.getColor(themeMode))
                }
            }
        }

        makeTemplateView(
            message,
            binding.contentPanel,
            config?.themeMode ?: NotificationThemeMode.Default,
            onNotificationTemplateActionHandler
        )
    }
}
