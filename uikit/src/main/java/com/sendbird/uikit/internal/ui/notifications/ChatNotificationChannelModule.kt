package com.sendbird.uikit.internal.ui.notifications

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import com.sendbird.uikit.test.R
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.SendbirdUIKit.ThemeMode
import com.sendbird.uikit.interfaces.LoadingDialogHandler
import com.sendbird.uikit.internal.model.notifications.NotificationConfig
import com.sendbird.uikit.modules.BaseModule

/**
 * A module for notification channel.
 * All composed components are created when the module is created. After than those components can replace.
 *
 * since 3.5.0
 */
@JvmSuppressWildcards
internal class ChatNotificationChannelModule @JvmOverloads constructor(
    context: Context,
    uiConfig: NotificationConfig?,
    private val params: Params = Params(context)
) : BaseModule() {

    /**
     * Returns the notification channel header component.
     *
     * @return The channel header component of this module
     * @since 3.5.0
     */
    var headerComponent: ChatNotificationHeaderComponent
        private set

    /**
     * Sets a custom notification list component.
     *
     * @param component The notification list component to be used in this module
     * @since 3.5.0
     */
    var notificationListComponent: ChatNotificationListComponent
        private set

    /**
     * Returns the status component.
     *
     * @return The status component of this module
     * @since 3.5.0
     */
    var statusComponent: NotificationStatusComponent
        private set

    /**
     * Returns the handler for loading dialog.
     *
     * @return Loading dialog handler to be used in this module
     * @since 3.5.0
     */
    var loadingDialogHandler: LoadingDialogHandler? = null
        private set

    /**
     * Constructor
     *
     * @param context The `Context` this module is currently associated with
     * @since 3.5.0
     */
    init {
        headerComponent = ChatNotificationHeaderComponent(uiConfig).apply {
            params.setUseRightButton(false)
        }
        notificationListComponent = ChatNotificationListComponent(uiConfig = uiConfig)
        statusComponent = NotificationStatusComponent(uiConfig)
    }

    override fun onCreateView(context: Context, inflater: LayoutInflater, args: Bundle?): View {
        args?.let { params.applyArgs(context, it) }
        val moduleContext: Context = ContextThemeWrapper(context, params.theme)
        val parent = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        val values = TypedValue()
        if (params.shouldUseHeader()) {
            moduleContext.theme.resolveAttribute(R.attr.sb_component_header, values, true)
            val headerThemeContext: Context = ContextThemeWrapper(moduleContext, values.resourceId)
            val headerInflater = inflater.cloneInContext(headerThemeContext)
            val header = headerComponent.onCreateView(headerThemeContext, headerInflater, parent, args)
            parent.addView(header)
        }

        val innerContainer = FrameLayout(context).apply {
            layoutParams =
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        parent.addView(innerContainer)

        moduleContext.theme.resolveAttribute(R.attr.sb_component_list, values, true)
        val listThemeContext: Context = ContextThemeWrapper(moduleContext, values.resourceId)
        val listInflater = inflater.cloneInContext(listThemeContext)
        val channelListLayout =
            notificationListComponent.onCreateView(listThemeContext, listInflater, innerContainer, args)
        innerContainer.addView(channelListLayout)
        moduleContext.theme.resolveAttribute(R.attr.sb_component_status, values, true)

        val statusThemeContext: Context = ContextThemeWrapper(moduleContext, values.resourceId)
        val statusInflater = inflater.cloneInContext(statusThemeContext)
        val statusLayout = statusComponent.onCreateView(statusThemeContext, statusInflater, innerContainer, args)
        innerContainer.addView(statusLayout)
        return parent
    }

    /**
     * Sets the handler for the loading dialog.
     *
     * @param loadingDialogHandler Loading dialog handler to be used in this module
     * @since 3.5.0
     */
    fun setOnLoadingDialogHandler(loadingDialogHandler: LoadingDialogHandler?) {
        this.loadingDialogHandler = loadingDialogHandler
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * @since 3.5.0
     */
    fun shouldShowLoadingDialog(): Boolean {
        return loadingDialogHandler?.shouldShowLoadingDialog() ?: false
        // Do nothing on the channel.
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * @since 3.5.0
     */
    fun shouldDismissLoadingDialog() {
        loadingDialogHandler?.shouldDismissLoadingDialog()
    }

    class Params : BaseModule.Params {
        @JvmOverloads
        constructor(context: Context, themeMode: ThemeMode = SendbirdUIKit.getDefaultThemeMode()) : super(
            context,
            themeMode,
            R.attr.sb_module_chat_notification_channel
        )

        /**
         * Constructor
         *
         * @param context    The `Context` this module is currently associated with
         * @param themeResId The theme resource ID to be applied to this module
         * @since 3.5.0
         */
        constructor(context: Context, @StyleRes themeResId: Int) : super(
            context,
            themeResId,
            R.attr.sb_module_chat_notification_channel
        )

        fun applyArgs(context: Context, args: Bundle): Params {
            return super.apply(context, args) as Params
        }
    }
}
