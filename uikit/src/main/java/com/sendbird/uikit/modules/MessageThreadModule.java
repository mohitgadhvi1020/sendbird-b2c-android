package com.sendbird.uikit.modules;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.view.ContextThemeWrapper;

import com.sendbird.android.message.BaseMessage;
import com.sendbird.uikit.test.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.modules.components.MessageInputComponent;
import com.sendbird.uikit.modules.components.MessageThreadHeaderComponent;
import com.sendbird.uikit.modules.components.MessageThreadInputComponent;
import com.sendbird.uikit.modules.components.StatusComponent;
import com.sendbird.uikit.modules.components.ThreadListComponent;

/**
 * A module for message thread. This module is composed of a header, list, input, and status.
 * All composed components are created when the module is created. After than those components can replace.
 *
 * <ul>
 *  <li>Header component is {@link MessageThreadHeaderComponent} and you can set custom header component by {@link #setHeaderComponent(MessageThreadHeaderComponent)}
 *  <li>List component is {@link ThreadListComponent} and you can set custom list component by {@link #setMessageListComponent(ThreadListComponent)}
 *  <li>Input component is {@link MessageThreadInputComponent} and you can set custom input component by {@link #setInputComponent(MessageInputComponent)}
 *  <li>Status component is {@link StatusComponent} and you can set custom status component by {@link #setStatusComponent(StatusComponent)}
 * </ul>
 *
 * since 3.3.0
 */
public class MessageThreadModule extends BaseMessageListModule<ThreadListComponent> {
    @NonNull
    private final Params params;
    @NonNull
    private MessageThreadHeaderComponent headerComponent;

    /**
     * Constructor
     *
     * @param context The {@code Context} this module is currently associated with
     * @param parentMessage The parent message to be shown in this module
     * since 3.3.0
     */
    public MessageThreadModule(@NonNull Context context, @NonNull BaseMessage parentMessage) {
        this(context, parentMessage, new Params(context));
    }

    /**
     * Constructor
     *
     * @param context The {@code Context} this module is currently associated with
     * @param parentMessage The parent message to be shown in this module
     * @param params  The Parameter object that can customize a default Fragment.
     * since 3.3.0
     */
    public MessageThreadModule(@NonNull Context context, @NonNull BaseMessage parentMessage, @NonNull Params params) {
        super(context, new ThreadListComponent());
        this.params = params;
        this.headerComponent = new MessageThreadHeaderComponent();
        this.headerComponent.getParams().setUseRightButton(false);
        setInputComponent(new MessageThreadInputComponent(parentMessage));
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @Nullable Bundle args) {
        if (args != null) params.apply(context, args);

        final Context moduleContext = new ContextThemeWrapper(context, params.getTheme());
        final LinearLayout parent = new LinearLayout(context);
        parent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        parent.setOrientation(LinearLayout.VERTICAL);

        final TypedValue values = new TypedValue();
        if (params.shouldUseHeader()) {
            moduleContext.getTheme().resolveAttribute(R.attr.sb_component_header, values, true);
            final Context headerThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
            final LayoutInflater headerInflater = inflater.cloneInContext(headerThemeContext);
            final View header = getHeaderComponent().onCreateView(headerThemeContext, headerInflater, parent, args);
            parent.addView(header);
        }

        final FrameLayout bodyContainer = new FrameLayout(context);
        bodyContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
        parent.addView(bodyContainer);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_list, values, true);
        final Context listThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater listInflater = inflater.cloneInContext(listThemeContext);
        final View messageListLayout = getMessageListComponent().onCreateView(listThemeContext, listInflater, bodyContainer, args);
        bodyContainer.addView(messageListLayout);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_status, values, true);
        final Context statusThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater statusInflater = inflater.cloneInContext(statusThemeContext);
        final View statusLayout = getStatusComponent().onCreateView(statusThemeContext, statusInflater, bodyContainer, args);
        bodyContainer.addView(statusLayout);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_channel_message_input, values, true);
        final Context inputThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater inputInflater = inflater.cloneInContext(inputThemeContext);
        final View inputLayout = getMessageInputComponent().onCreateView(inputThemeContext, inputInflater, parent, args);
        parent.addView(inputLayout);
        return parent;
    }

    /**
     * Sets a custom header component.
     *
     * @param component The header component to be used in this module
     * since 3.3.0
     */
    public <T extends MessageThreadHeaderComponent> void setHeaderComponent(@NonNull T component) {
        this.headerComponent = component;
    }

    /**
     * Returns the header component.
     *
     * @return The header component of this module
     * since 3.3.0
     */
    @NonNull
    public MessageThreadHeaderComponent getHeaderComponent() {
        return headerComponent;
    }

    @NonNull
    @Override
    public Params getParams() {
        return params;
    }

    public static class Params extends BaseModule.Params {
        /**
         * Constructor
         *
         * @param context The {@code Context} this module is currently associated with
         * since 3.3.0
         */
        public Params(@NonNull Context context) {
            this(context, SendbirdUIKit.getDefaultThemeMode());
        }

        /**
         * Constructor
         *
         * @param context   The {@code Context} this module is currently associated with
         * @param themeMode The theme of Sendbird UIKit to be applied to this module
         * since 3.3.0
         */
        public Params(@NonNull Context context, @NonNull SendbirdUIKit.ThemeMode themeMode) {
            super(context, themeMode, R.attr.sb_module_message_thread);
        }

        /**
         * Constructor
         *
         * @param context    The {@code Context} this module is currently associated with
         * @param themeResId The theme resource ID to be applied to this module
         * since 3.3.0
         */
        public Params(@NonNull Context context, @StyleRes int themeResId) {
            super(context, themeResId, R.attr.sb_module_message_thread);
        }
    }
}
