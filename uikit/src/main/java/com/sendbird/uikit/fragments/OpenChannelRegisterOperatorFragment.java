package com.sendbird.uikit.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.user.User;
import com.sendbird.uikit.test.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.activities.OpenChannelOperatorListActivity;
import com.sendbird.uikit.activities.adapter.OpenChannelRegisterOperatorListAdapter;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.OnUserSelectChangedListener;
import com.sendbird.uikit.interfaces.OnUserSelectionCompleteListener;
import com.sendbird.uikit.interfaces.PagedQueryHandler;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.modules.OpenChannelRegisterOperatorModule;
import com.sendbird.uikit.modules.components.OpenChannelRegisterOperatorListComponent;
import com.sendbird.uikit.modules.components.SelectUserHeaderComponent;
import com.sendbird.uikit.modules.components.StatusComponent;
import com.sendbird.uikit.providers.ModuleProviders;
import com.sendbird.uikit.providers.ViewModelProviders;
import com.sendbird.uikit.vm.OpenChannelRegisterOperatorViewModel;
import com.sendbird.uikit.widgets.StatusFrameView;

import java.util.List;

/**
 * Fragment displaying the participant list to be operator.
 *
 * since 3.1.0
 */
public class OpenChannelRegisterOperatorFragment extends BaseModuleFragment<OpenChannelRegisterOperatorModule, OpenChannelRegisterOperatorViewModel> {
    @Nullable
    private PagedQueryHandler<User> pagedQueryHandler;
    @Nullable
    private OpenChannelRegisterOperatorListAdapter adapter;
    @Nullable
    private View.OnClickListener headerLeftButtonClickListener;
    @Nullable
    private View.OnClickListener headerRightButtonClickListener;
    @Nullable
    private OnUserSelectChangedListener userSelectChangedListener;
    @Nullable
    private OnUserSelectionCompleteListener userSelectionCompleteListener;

    @NonNull
    @Override
    protected OpenChannelRegisterOperatorModule onCreateModule(@NonNull Bundle args) {
        return ModuleProviders.getOpenChannelRegisterOperator().provide(requireContext(), args);
    }

    @Override
    protected void onConfigureParams(@NonNull OpenChannelRegisterOperatorModule module, @NonNull Bundle args) {
    }

    @NonNull
    @Override
    protected OpenChannelRegisterOperatorViewModel onCreateViewModel() {
        return ViewModelProviders.getOpenChannelRegisterOperator().provide(this, getChannelUrl(), pagedQueryHandler);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getModule().getStatusComponent().notifyStatusChanged(StatusFrameView.Status.LOADING);
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull OpenChannelRegisterOperatorModule module, @NonNull OpenChannelRegisterOperatorViewModel viewModel) {
        Logger.d(">> OpenChannelRegisterOperatorFragment::onBeforeReady status=%s", status);
        module.getRegisterOperatorListComponent().setPagedDataLoader(viewModel);
        if (this.adapter != null) {
            module.getRegisterOperatorListComponent().setAdapter(adapter);
        }
        final OpenChannel channel = viewModel.getChannel();
        onBindHeaderComponent(module.getHeaderComponent(), viewModel, channel);
        onBindRegisterOperatorListComponent(module.getRegisterOperatorListComponent(), viewModel, channel);
        onBindStatusComponent(module.getStatusComponent(), viewModel, channel);
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull OpenChannelRegisterOperatorModule module, @NonNull OpenChannelRegisterOperatorViewModel viewModel) {
        Logger.d(">> OpenChannelRegisterOperatorFragment::onReady status=%s", status);
        final OpenChannel channel = viewModel.getChannel();
        if (status != ReadyStatus.READY || channel == null) {
            final StatusComponent statusComponent = module.getStatusComponent();
            statusComponent.notifyStatusChanged(StatusFrameView.Status.CONNECTION_ERROR);
            return;
        }
        viewModel.getChannelDeleted().observe(getViewLifecycleOwner(), isDeleted -> {
            if (isDeleted) shouldActivityFinish();
        });

        viewModel.loadInitial();
    }

    /**
     * Called to bind events to the HeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, OpenChannelRegisterOperatorModule, OpenChannelRegisterOperatorViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel A view model that provides the data needed for the fragment
     * @param openChannel The {@code OpenChannel} that contains the data needed for this fragment
     * since 3.1.0
     */
    protected void onBindHeaderComponent(@NonNull SelectUserHeaderComponent headerComponent, @NonNull OpenChannelRegisterOperatorViewModel viewModel, @Nullable OpenChannel openChannel) {
        Logger.d(">> OpenChannelRegisterOperatorFragment::onBindHeaderComponent()");
        headerComponent.setOnLeftButtonClickListener(headerLeftButtonClickListener != null ? headerLeftButtonClickListener : v -> shouldActivityFinish());
        headerComponent.setOnRightButtonClickListener(headerRightButtonClickListener != null ? headerRightButtonClickListener : v -> getModule().getRegisterOperatorListComponent().notifySelectionComplete());
    }

    /**
     * Called to bind events to the OpenChannelRegisterOperatorListComponent. This is called from {@link #onBeforeReady(ReadyStatus, OpenChannelRegisterOperatorModule, OpenChannelRegisterOperatorViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param listComponent The component to which the event will be bound
     * @param viewModel A view model that provides the data needed for the fragment
     * @param openChannel The {@code OpenChannel} that contains the data needed for this fragment
     * since 3.1.0
     */
    protected void onBindRegisterOperatorListComponent(@NonNull OpenChannelRegisterOperatorListComponent listComponent, @NonNull OpenChannelRegisterOperatorViewModel viewModel, @Nullable OpenChannel openChannel) {
        Logger.d(">> OpenChannelRegisterOperatorFragment::onBindRegisterOperatorListComponent()");
        if (openChannel != null) {
            listComponent.notifyChannelChanged(openChannel);
        }
        listComponent.setOnUserSelectChangedListener(userSelectChangedListener != null ? userSelectChangedListener : (selectedUserIds, isSelected) -> getModule().getHeaderComponent().notifySelectedUserChanged(selectedUserIds.size()));
        listComponent.setOnUserSelectionCompleteListener(userSelectionCompleteListener != null ? userSelectionCompleteListener : OpenChannelRegisterOperatorFragment.this::onUserSelectionCompleted);
        viewModel.getUserList().observe(getViewLifecycleOwner(), listComponent::notifyDataSetChanged);
    }

    /**
     * Called to bind events to the StatusComponent. This is called from {@link #onBeforeReady(ReadyStatus, OpenChannelRegisterOperatorModule, OpenChannelRegisterOperatorViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param statusComponent The component to which the event will be bound
     * @param viewModel A view model that provides the data needed for the fragment
     * @param openChannel The {@code OpenChannel} that contains the data needed for this fragment
     * since 3.1.0
     */
    protected void onBindStatusComponent(@NonNull StatusComponent statusComponent, @NonNull OpenChannelRegisterOperatorViewModel viewModel, @Nullable OpenChannel openChannel) {
        Logger.d(">> OpenChannelRegisterOperatorFragment::onBindStatusComponent()");
        statusComponent.setOnActionButtonClickListener(v -> {
            statusComponent.notifyStatusChanged(StatusFrameView.Status.LOADING);
            shouldAuthenticate();
        });

        viewModel.getStatusFrame().observe(getViewLifecycleOwner(), statusComponent::notifyStatusChanged);
    }

    /**
     * Called when the user selection completed.
     *
     * @param selectedUsers selected user's ids.
     * since 3.1.0
     */
    protected void onUserSelectionCompleted(@NonNull List<String> selectedUsers) {
        Logger.d(">> RegisterOperators::onUserSelectComplete()");
        getViewModel().addOperators(selectedUsers, e -> {
            if (e != null) {
                toastError(R.string.sb_text_error_register_operator);
                Logger.e(e);
                return;
            }
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), OpenChannelOperatorListActivity.class);
                getActivity().setResult(RESULT_OK, intent);
            }
            shouldActivityFinish();
        });
    }

    /**
     * Returns the URL of the channel with the required data to use this fragment.
     *
     * @return The URL of a channel this fragment is currently associated with
     * since 3.1.0
     */
    @NonNull
    protected String getChannelUrl() {
        final Bundle args = getArguments() == null ? new Bundle() : getArguments();
        return args.getString(StringSet.KEY_CHANNEL_URL, "");
    }

    public static class Builder {
        @NonNull
        private final Bundle bundle;
        @Nullable
        private PagedQueryHandler<User> pagedQueryHandler;
        @Nullable
        private OpenChannelRegisterOperatorListAdapter adapter;
        @Nullable
        private View.OnClickListener headerLeftButtonClickListener;
        @Nullable
        private View.OnClickListener headerRightButtonClickListener;
        @Nullable
        private OnUserSelectChangedListener userSelectChangedListener;
        @Nullable
        private OnUserSelectionCompleteListener userSelectionCompleteListener;
        @Nullable
        private OpenChannelRegisterOperatorFragment customFragment;

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * since 3.1.0
         */
        public Builder(@NonNull String channelUrl) {
            this(channelUrl, 0);
        }

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @param themeMode  {@link SendbirdUIKit.ThemeMode}
         * since 3.1.0
         */
        public Builder(@NonNull String channelUrl, @NonNull SendbirdUIKit.ThemeMode themeMode) {
            this(channelUrl, themeMode.getResId());
        }

        /**
         * Constructor
         *
         * @param channelUrl       the url of the channel will be implemented.
         * @param customThemeResId the resource identifier for custom theme.
         * since 3.1.0
         */
        public Builder(@NonNull String channelUrl, @StyleRes int customThemeResId) {
            bundle = new Bundle();
            if (customThemeResId != 0) {
                bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            }
            bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
        }

        /**
         * Sets the custom fragment. It must inherit {@link OpenChannelRegisterOperatorFragment}.
         *
         * @param fragment custom fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public <T extends OpenChannelRegisterOperatorFragment> Builder setCustomFragment(T fragment) {
            this.customFragment = fragment;
            return this;
        }

        /**
         * Sets arguments to this fragment.
         *
         * @param args the arguments supplied when the fragment was instantiated.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.0
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
         * since 3.1.0
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
         * since 3.1.0
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
         * since 3.1.0
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
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.0
         */
        @NonNull
        public Builder setHeaderLeftButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets the right button text of the header.
         *
         * @param rightButtonText text to be displayed to the right button.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setRightButtonText(@NonNull String rightButtonText) {
            bundle.putString(StringSet.KEY_HEADER_RIGHT_BUTTON_TEXT, rightButtonText);
            return this;
        }

        /**
         * Sets the handler that loads the list of user.
         *
         * @param handler The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.0
         */
        @NonNull
        public Builder setCustomPagedQueryHandler(@NonNull PagedQueryHandler<User> handler) {
            this.pagedQueryHandler = handler;
            return this;
        }

        /**
         * Sets the user list adapter.
         *
         * @param adapter the adapter for the user list.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.0
         */
        @NonNull
        public Builder setRegisterOperatorListAdapter(@NonNull OpenChannelRegisterOperatorListAdapter adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * Sets the click listener on the left button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.0
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
         * since 3.1.0
         */
        @NonNull
        public Builder setOnHeaderRightButtonClickListener(@NonNull View.OnClickListener listener) {
            this.headerRightButtonClickListener = listener;
            return this;
        }

        /**
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.0
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
         * since 3.1.0
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
         * since 3.1.0
         */
        @NonNull
        public Builder setErrorText(@StringRes int resId) {
            bundle.putInt(StringSet.KEY_ERROR_TEXT_RES_ID, resId);
            return this;
        }

        /**
         * Register a callback to be invoked when the user is selected.
         *
         * @param userSelectChangedListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.0
         */
        @NonNull
        public Builder setOnUserSelectChangedListener(@Nullable OnUserSelectChangedListener userSelectChangedListener) {
            this.userSelectChangedListener = userSelectChangedListener;
            return this;
        }

        /**
         * Register a callback to be invoked when selecting users is completed.
         *
         * @param userSelectionCompleteListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.0
         */
        @NonNull
        public Builder setOnUserSelectionCompleteListener(@Nullable OnUserSelectionCompleteListener userSelectionCompleteListener) {
            this.userSelectionCompleteListener = userSelectionCompleteListener;
            return this;
        }

        /**
         * Creates an {@link OpenChannelRegisterOperatorFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link OpenChannelRegisterOperatorFragment} applied to the {@link Bundle}.
         * since 3.1.0
         */
        @NonNull
        public OpenChannelRegisterOperatorFragment build() {
            final OpenChannelRegisterOperatorFragment fragment = customFragment != null ? customFragment : new OpenChannelRegisterOperatorFragment();
            fragment.setArguments(bundle);
            fragment.pagedQueryHandler = pagedQueryHandler;
            fragment.adapter = adapter;
            fragment.headerLeftButtonClickListener = headerLeftButtonClickListener;
            fragment.headerRightButtonClickListener = headerRightButtonClickListener;
            fragment.userSelectChangedListener = userSelectChangedListener;
            fragment.userSelectionCompleteListener = userSelectionCompleteListener;
            return fragment;
        }
    }
}
