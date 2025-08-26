package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.SendingStatus
import com.sendbird.uikit.test.R
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.test.databinding.SbViewMyMessageStatusBinding
import com.sendbird.uikit.utils.DrawableUtils

internal class MyMessageStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    private val binding: SbViewMyMessageStatusBinding
    fun drawError() {
        setProgress(false)
        val errorColor = SendbirdUIKit.getDefaultThemeMode().errorColorResId
        binding.ivStatus.setImageDrawable(
            DrawableUtils.setTintList(context, R.drawable.icon_error, errorColor)
        )
    }

    fun drawRead() {
        setProgress(false)
        val readColor = SendbirdUIKit.getDefaultThemeMode().secondaryTintResId
        binding.ivStatus.setImageDrawable(
            DrawableUtils.setTintList(context, R.drawable.icon_done_all, readColor)
        )
    }

    fun drawSent() {
        setProgress(false)
        binding.ivStatus.setImageDrawable(
            DrawableUtils.setTintList(
                context,
                R.drawable.icon_done,
                SendbirdUIKit.getDefaultThemeMode().monoTintResId
            )
        )
    }

    fun drawDelivered() {
        setProgress(false)
        binding.ivStatus.setImageDrawable(
            DrawableUtils.setTintList(
                context,
                R.drawable.icon_done_all,
                SendbirdUIKit.getDefaultThemeMode().monoTintResId
            )
        )
    }

    fun drawProgress() {
        setProgress(true)
    }

    private fun setProgress(isProgress: Boolean) {
        this.visibility = VISIBLE
        if (isProgress) {
            binding.ivStatus.visibility = GONE
            binding.mpvProgressStatus.visibility = VISIBLE
        } else {
            binding.mpvProgressStatus.visibility = GONE
            binding.ivStatus.visibility = VISIBLE
        }
    }

    fun drawStatus(message: BaseMessage, channel: BaseChannel, useMessageReceipt: Boolean) {
        when (message.sendingStatus) {
            SendingStatus.CANCELED, SendingStatus.FAILED -> {
                visibility = VISIBLE
                drawError()
            }
            SendingStatus.SUCCEEDED -> {
                if (channel.isGroupChannel && channel is GroupChannel) {
                    if (!useMessageReceipt || channel.isSuper || channel.isBroadcast) {
                        visibility = GONE
                        return
                    }
                    visibility = VISIBLE
                    val unreadMemberCount = channel.getUnreadMemberCount(message)
                    val unDeliveredMemberCount = channel.getUndeliveredMemberCount(message)
                    if (unreadMemberCount == 0) {
                        drawRead()
                    } else if (unDeliveredMemberCount == 0) {
                        drawDelivered()
                    } else {
                        drawSent()
                    }
                } else {
                    visibility = GONE
                }
            }
            SendingStatus.PENDING -> drawProgress()
            else -> {}
        }
    }

    init {
        binding = SbViewMyMessageStatusBinding.inflate(LayoutInflater.from(context), this, true)
    }
}
