package com.sendbird.uikit.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.channel.query.GroupChannelListQuery;
import com.sendbird.uikit.test.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.activities.ChannelActivity;
import com.sendbird.uikit.activities.ChatNotificationChannelActivity;
import com.sendbird.uikit.activities.CreateChannelActivity;
import com.sendbird.uikit.activities.adapter.ChannelListAdapter;
import com.sendbird.uikit.consts.CreatableChannelType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.MessageDisplayDataProvider;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.internal.ui.widgets.SelectChannelTypeView;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.DialogListItem;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.model.configurations.ChannelListConfig;
import com.sendbird.uikit.modules.ChannelListModule;
import com.sendbird.uikit.modules.components.ChannelListComponent;
import com.sendbird.uikit.modules.components.HeaderComponent;
import com.sendbird.uikit.modules.components.StatusComponent;
import com.sendbird.uikit.providers.ModuleProviders;
import com.sendbird.uikit.providers.ViewModelProviders;
import com.sendbird.uikit.utils.Available;
import com.sendbird.uikit.utils.ChannelUtils;
import com.sendbird.uikit.utils.ContextUtils;
import com.sendbird.uikit.utils.DialogUtils;
import com.sendbird.uikit.vm.ChannelListViewModel;
import com.sendbird.uikit.widgets.StatusFrameView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Fragment displaying the list of channels.
 */
public class ChannelListFragment extends BaseModuleFragment<ChannelListModule, ChannelListViewModel> {

    @Nullable
    private View.OnClickListener headerLeftButtonClickListener;
    @Nullable
    private View.OnClickListener headerRightButtonClickListener;
    @Nullable
    private ChannelListAdapter adapter;
    @Nullable
    private OnItemClickListener<GroupChannel> itemClickListener;
    @Nullable
    private OnItemLongClickListener<GroupChannel> itemLongClickListener;
    @Nullable
    private GroupChannelListQuery query;

    @NonNull
    @Override
    protected ChannelListModule onCreateModule(@NonNull Bundle args) {
        return ModuleProviders.getChannelList().provide(requireContext(), args);
    }

    @Override
    protected void onConfigureParams(@NonNull ChannelListModule module, @NonNull Bundle args) {
    }

    @NonNull
    @Override
    protected ChannelListViewModel onCreateViewModel() {
        return ViewModelProviders.getChannelList().provide(this, query);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getModule().getStatusComponent().notifyStatusChanged(StatusFrameView.Status.LOADING);
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull ChannelListModule module, @NonNull ChannelListViewModel viewModel) {
        Logger.d(">> ChannelListFragment::initModule()");
        module.getChannelListComponent().setPagedDataLoader(viewModel);
        if (this.adapter != null) {
            module.getChannelListComponent().setAdapter(adapter);
        }
        onBindHeaderComponent(module.getHeaderComponent(), viewModel);
        onBindChannelListComponent(module.getChannelListComponent(), viewModel);
        onBindStatusComponent(module.getStatusComponent(), viewModel);
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull ChannelListModule module, @NonNull ChannelListViewModel viewModel) {
        Logger.d(">> ChannelListFragment::onReady status=%s", status);
        if (status != ReadyStatus.READY) {
            final StatusComponent statusComponent = module.getStatusComponent();
            statusComponent.notifyStatusChanged(StatusFrameView.Status.CONNECTION_ERROR);
            return;
        }
        viewModel.loadInitial();
    }

    /**
     * Called to bind events to the HeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, ChannelListModule, ChannelListViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * since 3.0.0
     */
    protected void onBindHeaderComponent(@NonNull HeaderComponent headerComponent, @NonNull ChannelListViewModel viewModel) {
        Logger.d(">> ChannelListFragment::setupHeaderComponent()");
        headerComponent.setOnLeftButtonClickListener(headerLeftButtonClickListener != null ? headerLeftButtonClickListener : v -> shouldActivityFinish());
        headerComponent.setOnRightButtonClickListener(headerRightButtonClickListener != null ? headerRightButtonClickListener : v -> showChannelTypeSelectDialog());
    }

    /**
     * Called to bind events to the ChannelListComponent. This is called from {@link #onBeforeReady(ReadyStatus, ChannelListModule, ChannelListViewModel)}.
     *
     * @param channelListComponent The component to which the event will be bound
     * @param viewModel            A view model that provides the data needed for the fragment
     * since 3.0.0
     */
    protected void onBindChannelListComponent(@NonNull ChannelListComponent channelListComponent, @NonNull ChannelListViewModel viewModel) {
        Logger.d(">> ChannelListFragment::setupChannelListComponent()");
        channelListComponent.setOnItemClickListener(this::onItemClicked);
        channelListComponent.setOnItemLongClickListener(this::onItemLongClicked);
        viewModel.getChannelList().observe(getViewLifecycleOwner(), channelListComponent::notifyDataSetChanged);
    }

    /**
     * Called to bind events to the StatusComponent. This is called from {@link #onBeforeReady(ReadyStatus, ChannelListModule, ChannelListViewModel)}.
     *
     * @param statusComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * since 3.0.0
     */
    protected void onBindStatusComponent(@NonNull StatusComponent statusComponent, @NonNull ChannelListViewModel viewModel) {
        Logger.d(">> ChannelListFragment::setupStatusComponent()");
        statusComponent.setOnActionButtonClickListener(v -> {
            statusComponent.notifyStatusChanged(StatusFrameView.Status.LOADING);
            shouldAuthenticate();
        });

        viewModel.getChannelList().observe(getViewLifecycleOwner(), channels -> statusComponent.notifyStatusChanged(channels.isEmpty() ? StatusFrameView.Status.EMPTY : StatusFrameView.Status.NONE));
    }

    private void showChannelTypeSelectDialog() {
        if (getContext() == null) return;
        if (Available.isSupportSuper() || Available.isSupportBroadcast()) {
            final Context contextThemeWrapper = ContextUtils.extractModuleThemeContext(getContext(), getModule().getParams().getTheme(), R.attr.sb_component_header);
            final SelectChannelTypeView layout = new SelectChannelTypeView(contextThemeWrapper);
            layout.canCreateSuperGroupChannel(Available.isSupportSuper());
            layout.canCreateBroadcastGroupChannel(Available.isSupportBroadcast());
            final AlertDialog dialog = DialogUtils.showContentTopDialog(requireContext(), layout);
            layout.setOnItemClickListener((itemView, position, channelType) -> {
                dialog.dismiss();
                Logger.dev("++ channelType : " + channelType);
                if (isFragmentAlive()) {
                    onSelectedChannelType(channelType);
                }
            });
        } else {
            if (isFragmentAlive()) {
                onSelectedChannelType(CreatableChannelType.Normal);
            }
        }
    }

    private void startChannelActivity(@NonNull GroupChannel channel) {
        if (isFragmentAlive()) {
            if (channel.isChatNotification()) {
                startActivity(ChatNotificationChannelActivity.newIntent(requireContext(), channel.getUrl()));
            } else {
                startActivity(ChannelActivity.newIntent(requireContext(), channel.getUrl()));
            }
        }
    }

    /**
     * Returns the title of the context menu.
     *
     * @param channel a channel to make the context menu
     * @return the title of the context menu
     * since 3.17.0
     */
    @NonNull
    protected String getActionContextMenuTitle(@NonNull GroupChannel channel) {
        return ChannelUtils.makeTitleText(requireContext(), channel);
    }

    /**
     * Makes the context menu for the channel.
     *
     * @param channel a channel to make the context menu
     * @return a list of {@link DialogListItem} to show the context menu
     * since 3.17.0
     */
    @NonNull
    protected List<DialogListItem> makeChannelContextMenu(@NonNull GroupChannel channel) {
        if (channel.isChatNotification()) return Collections.emptyList();
        DialogListItem pushOnOff = new DialogListItem(ChannelUtils.isChannelPushOff(channel) ? R.string.sb_text_channel_list_push_on : R.string.sb_text_channel_list_push_off);
        DialogListItem leaveChannel = new DialogListItem(R.string.sb_text_channel_list_leave);
        return Arrays.asList(pushOnOff, leaveChannel);
    }

    /**
     * It will be called when the message context menu was clicked.
     *
     * @param channel  A clicked channel.
     * @param view     The view that was clicked.
     * @param position The position that was clicked.
     * @param item     {@link DialogListItem} that was clicked.
     * @return <code>true</code> if long click event was handled, <code>false</code> otherwise.
     * since 3.17.0
     */
    protected boolean onChannelContextMenuItemClicked(@NonNull GroupChannel channel, @NonNull View view, int position, @NonNull DialogListItem item) {
        final int key = item.getKey();
        if (key == R.string.sb_text_channel_list_leave) {
            Logger.dev("leave channel");
            leaveChannel(channel);
            return true;
        } else if (key == R.string.sb_text_channel_list_push_on || key == R.string.sb_text_channel_list_push_off) {
            Logger.dev("change push notifications");
            final boolean enable = ChannelUtils.isChannelPushOff(channel);
            getViewModel().setPushNotification(channel, ChannelUtils.isChannelPushOff(channel), e -> {
                if (e != null) {
                    int message = enable ? R.string.sb_text_error_push_notification_on : R.string.sb_text_error_push_notification_off;
                    toastError(message);
                }
            });
            return true;
        }
        return false;
    }

    private void showListContextMenu(@NonNull GroupChannel channel) {
        final List<DialogListItem> items = makeChannelContextMenu(channel);
        final int size = items.size();

        if (isFragmentAlive() && size > 0) {
            DialogUtils.showListDialog(requireContext(),
                getActionContextMenuTitle(channel),
                items.toArray(new DialogListItem[size]), (v, p, item) -> onChannelContextMenuItemClicked(channel, v, p, item));
        }
    }

    /**
     * A callback that selected channel types.
     *
     * @param channelType selected channel type.
     * @see CreatableChannelType
     * since 1.2.0
     */
    protected void onSelectedChannelType(@NonNull CreatableChannelType channelType) {
        startActivity(CreateChannelActivity.newIntent(requireContext(), channelType));
    }

    /**
     * Leaves this channel.
     *
     * since 1.0.4
     */
    protected void leaveChannel(@NonNull GroupChannel channel) {
        getViewModel().leaveChannel(channel, e -> {
            if (e != null) toastError(R.string.sb_text_error_leave_channel);
        });
    }

    /**
     * Called when the item of the channel list is clicked.
     *
     * @param view     The View clicked.
     * @param position The position clicked.
     * @param channel  The channel that the clicked item displays
     * since 3.2.0
     */
    protected void onItemClicked(@NonNull View view, int position, @NonNull GroupChannel channel) {
        if (itemClickListener != null) {
            itemClickListener.onItemClick(view, position, channel);
            return;
        }
        startChannelActivity(channel);
    }

    /**
     * Called when the item of the channel list is long-clicked.
     *
     * @param view     The View long-clicked.
     * @param position The position long-clicked.
     * @param channel  The channel that the long-clicked item displays
     * since 3.2.0
     */
    protected void onItemLongClicked(@NonNull View view, int position, @NonNull GroupChannel channel) {
        if (itemLongClickListener != null) {
            itemLongClickListener.onItemLongClick(view, position, channel);
            return;
        }
        showListContextMenu(channel);
    }

    public static class Builder {
        @NonNull
        private final Bundle bundle;
        @Nullable
        private View.OnClickListener headerLeftButtonClickListener;
        @Nullable
        private View.OnClickListener headerRightButtonClickListener;
        @Nullable
        private ChannelListAdapter adapter;
        @Nullable
        private OnItemClickListener<GroupChannel> itemClickListener;
        @Nullable
        private OnItemLongClickListener<GroupChannel> itemLongClickListener;
        @Nullable
        private GroupChannelListQuery query;
        @Nullable
        private ChannelListFragment customFragment;

        /**
         * Constructor
         */
        public Builder() {
            this(0);
        }

        /**
         * Constructor
         *
         * @param themeMode {@link SendbirdUIKit.ThemeMode}
         */
        public Builder(@NonNull SendbirdUIKit.ThemeMode themeMode) {
            this(themeMode.getResId());
        }

        /**
         * Constructor
         *
         * @param customThemeResId the resource identifier for custom theme.
         */
        public Builder(@StyleRes int customThemeResId) {
            bundle = new Bundle();
            if (customThemeResId != 0) {
                bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            }
        }

        /**
         * Sets the custom fragment. It must inherit {@link ChannelListFragment}.
         *
         * @param fragment custom fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public <T extends ChannelListFragment> Builder setCustomFragment(T fragment) {
            this.customFragment = fragment;
            return this;
        }

        /**
         * Sets arguments to this fragment.
         *
         * @param args the arguments supplied when the fragment was instantiated.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder withArguments(@NonNull Bundle args) {
            this.bundle.putAll(args);
            return this;
        }

        /**
         * Sets the title of the header.
         *
         * @param title text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setHeaderTitle(@NonNull String title) {
            bundle.putString(StringSet.KEY_HEADER_TITLE, title);
            return this;
        }

        /**
         * Sets whether the header is used.
         *
         * @param useHeader <code>true</code> if the header is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setUseHeader(boolean useHeader) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER, useHeader);
            return this;
        }

        /**
         * Sets whether the right button of the header is used.
         *
         * @param useHeaderRightButton <code>true</code> if the right button of the header is used,
         *                             <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setUseHeaderRightButton(boolean useHeaderRightButton) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER_RIGHT_BUTTON, useHeaderRightButton);
            return this;
        }

        /**
         * Sets whether the left button of the header is used.
         *
         * @param useHeaderLeftButton <code>true</code> if the left button of the header is used,
         *                            <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setUseHeaderLeftButton(boolean useHeaderLeftButton) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON, useHeaderLeftButton);
            return this;
        }

        /**
         * Sets the icon on the left button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setHeaderLeftButtonIconResId(@DrawableRes int resId) {
            return setHeaderLeftButtonIcon(resId, null);
        }

        /**
         * Sets the icon on the left button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public Builder setHeaderLeftButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets the icon on the right button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setHeaderRightButtonIconResId(@DrawableRes int resId) {
            return setHeaderRightButtonIcon(resId, null);
        }

        /**
         * Sets the icon on the right button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public Builder setHeaderRightButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets the click listener on the left button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnHeaderLeftButtonClickListener(@NonNull View.OnClickListener listener) {
            this.headerLeftButtonClickListener = listener;
            return this;
        }

        /**
         * Sets the click listener on the right button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnHeaderRightButtonClickListener(@NonNull View.OnClickListener listener) {
            this.headerRightButtonClickListener = listener;
            return this;
        }

        /**
         * Sets the channel list adapter.
         *
         * @param adapter the adapter for the channel list.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setChannelListAdapter(@NonNull ChannelListAdapter adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * Sets the channel list adapter and the message display data provider.
         * The message display data provider is used to generate the data to display the last message in the channel.
         *
         * @param adapter the adapter for the channel list.
         * @param provider the provider for the message display data.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.5.7
         */
        @NonNull
        public Builder setChannelListAdapter(@Nullable ChannelListAdapter adapter, @Nullable MessageDisplayDataProvider provider) {
            this.adapter = adapter;
            if (this.adapter != null) this.adapter.setMessageDisplayDataProvider(provider);
            return this;
        }

        /**
         * Sets the click listener on the item of channel list.
         *
         * @param itemClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnItemClickListener(@NonNull OnItemClickListener<GroupChannel> itemClickListener) {
            this.itemClickListener = itemClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the item of channel list.
         *
         * @param itemLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnItemLongClickListener(@NonNull OnItemLongClickListener<GroupChannel> itemLongClickListener) {
            this.itemLongClickListener = itemLongClickListener;
            return this;
        }

        /**
         * Sets the query instance to get <code>GroupChannel</code>s the current <code>User</code> has joined.
         *
         * @param query The GroupChannelListQuery instance that you want to use.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 1.0.5
         */
        @NonNull
        public Builder setGroupChannelListQuery(@NonNull GroupChannelListQuery query) {
            this.query = query;
            return this;
        }

        /**
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.0.2
         */
        @NonNull
        public Builder setEmptyIcon(@DrawableRes int resId) {
            return setEmptyIcon(resId, null);
        }

        /**
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public Builder setEmptyIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_EMPTY_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_EMPTY_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets the text when the data is not exists
         *
         * @param resId the resource identifier of text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.0.2
         */
        @NonNull
        public Builder setEmptyText(@StringRes int resId) {
            bundle.putInt(StringSet.KEY_EMPTY_TEXT_RES_ID, resId);
            return this;
        }

        /**
         * Sets the text when error occurs
         *
         * @param resId the resource identifier of text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setErrorText(@StringRes int resId) {
            bundle.putInt(StringSet.KEY_ERROR_TEXT_RES_ID, resId);
            return this;
        }

        /**
         * Sets the channel list configuration for this fragment.
         * Use {@code UIKitConfig.groupChannelListConfig.clone()} for the default value.
         * Example usage:
         *
         * <pre>
         * val fragment = ChannelListFragment.Builder()
         *     .setChannelListConfig(
         *         UIKitConfig.groupChannelListConfig.clone().apply {
         *             this.enableTypingIndicator = true
         *         }
         *     )
         *     .build()
         * </pre>
         *
         * @param channelListConfig The channel list config.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.6.0
         */
        @NonNull
        public Builder setChannelListConfig(@NonNull ChannelListConfig channelListConfig) {
            this.bundle.putParcelable(StringSet.KEY_CHANNEL_LIST_CONFIG, channelListConfig);
            return this;
        }

        /**
         * Creates an {@link ChannelListFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link ChannelListFragment} applied to the {@link Bundle}.
         */
        @NonNull
        public ChannelListFragment build() {
            final ChannelListFragment fragment = customFragment != null ? customFragment : new ChannelListFragment();
            fragment.setArguments(bundle);
            fragment.headerLeftButtonClickListener = headerLeftButtonClickListener;
            fragment.headerRightButtonClickListener = headerRightButtonClickListener;
            fragment.adapter = adapter;
            fragment.itemClickListener = itemClickListener;
            fragment.itemLongClickListener = itemLongClickListener;
            fragment.query = query;
            return fragment;
        }
    }
}
