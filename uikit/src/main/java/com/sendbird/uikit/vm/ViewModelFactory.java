package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sendbird.android.channel.ChannelType;
import com.sendbird.android.channel.query.GroupChannelListQuery;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.query.MessageSearchQuery;
import com.sendbird.android.params.MessageListParams;
import com.sendbird.android.params.OpenChannelListQueryParams;
import com.sendbird.android.params.ThreadMessageListParams;
import com.sendbird.android.user.Member;
import com.sendbird.android.user.User;
import com.sendbird.uikit.interfaces.PagedQueryHandler;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit.model.configurations.ChannelConfig;
import com.sendbird.uikit.model.configurations.UIKitConfig;

import java.util.Objects;

public class ViewModelFactory implements ViewModelProvider.Factory {
    @Nullable
    private final Object[] params;

    public ViewModelFactory() {
        this.params = null;
    }

    public ViewModelFactory(@Nullable Object... params) {
        this.params = params;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChannelViewModel.class)) {
            return (T) new ChannelViewModel((String) Objects.requireNonNull(params)[0], params.length > 1 ? (MessageListParams) params[1] : null, params.length > 2 ? (ChannelConfig) params[2] : UIKitConfig.getGroupChannelConfig());
        } else if (modelClass.isAssignableFrom(ChannelListViewModel.class)) {
            return (T) new ChannelListViewModel(params != null && params.length > 0 ? (GroupChannelListQuery) params[0] : null);
        } else if (modelClass.isAssignableFrom(OpenChannelViewModel.class)) {
            return (T) new OpenChannelViewModel((String) Objects.requireNonNull(params)[0], params.length > 1 ? (MessageListParams) params[1] : null);
        } else if (modelClass.isAssignableFrom(OpenChannelSettingsViewModel.class)) {
            return (T) new OpenChannelSettingsViewModel((String) Objects.requireNonNull(params)[0]);
        } else if (modelClass.isAssignableFrom(MessageSearchViewModel.class)) {
            return (T) new MessageSearchViewModel((String) Objects.requireNonNull(params)[0], params.length > 1 ? (MessageSearchQuery) params[1] : null);
        } else if (modelClass.isAssignableFrom(ChannelSettingsViewModel.class)) {
            return (T) new ChannelSettingsViewModel((String) Objects.requireNonNull(params)[0]);
        } else if (modelClass.isAssignableFrom(ModerationViewModel.class)) {
            return (T) new ModerationViewModel((String) Objects.requireNonNull(params)[0]);
        } else if (modelClass.isAssignableFrom(ParticipantViewModel.class)) {
            return (T) new ParticipantViewModel((String) Objects.requireNonNull(params)[0], params.length > 1 ? (PagedQueryHandler<User>) params[1] : null);
        } else if (modelClass.isAssignableFrom(BannedUserListViewModel.class)) {
            return (T) new BannedUserListViewModel((String) Objects.requireNonNull(params)[0], params.length > 1 ? (ChannelType) params[1] : null);
        } else if (modelClass.isAssignableFrom(CreateChannelViewModel.class)) {
            return (T) new CreateChannelViewModel(params != null && params.length > 0 ? (PagedQueryHandler<UserInfo>) params[0] : null);
        } else if (modelClass.isAssignableFrom(InviteUserViewModel.class)) {
            return (T) new InviteUserViewModel((String) Objects.requireNonNull(params)[0], params.length > 1 ? (PagedQueryHandler<UserInfo>) params[1] : null);
        } else if (modelClass.isAssignableFrom(MemberListViewModel.class)) {
            return (T) new MemberListViewModel((String) Objects.requireNonNull(params)[0]);
        } else if (modelClass.isAssignableFrom(MutedMemberListViewModel.class)) {
            return (T) new MutedMemberListViewModel((String) Objects.requireNonNull(params)[0], params.length > 1 ? (PagedQueryHandler<Member>) params[1] : null);
        } else if (modelClass.isAssignableFrom(OperatorListViewModel.class)) {
            switch (Objects.requireNonNull(params).length) {
                case 2:
                    return (T) new OperatorListViewModel((String) params[0], (ChannelType) params[1], null);
                case 3:
                    return (T) new OperatorListViewModel((String) params[0], (ChannelType) params[1], (PagedQueryHandler<User>) params[2]);
                default:
                    return (T) new OperatorListViewModel((String) params[0], null, null);
            }
        } else if (modelClass.isAssignableFrom(RegisterOperatorViewModel.class)) {
            return (T) new RegisterOperatorViewModel((String) Objects.requireNonNull(params)[0], params.length > 1 ? (PagedQueryHandler<Member>) params[1] : null);
        } else if (modelClass.isAssignableFrom(ChannelPushSettingViewModel.class)) {
            return (T) new ChannelPushSettingViewModel((String) Objects.requireNonNull(params)[0]);
        } else if (modelClass.isAssignableFrom(OpenChannelModerationViewModel.class)) {
            return (T) new OpenChannelModerationViewModel((String) Objects.requireNonNull(params)[0]);
        } else if (modelClass.isAssignableFrom(OpenChannelRegisterOperatorViewModel.class)) {
            return (T) new OpenChannelRegisterOperatorViewModel((String) Objects.requireNonNull(params)[0], params.length > 1 ? (PagedQueryHandler<User>) params[1] : null);
        } else if (modelClass.isAssignableFrom(OpenChannelOperatorListViewModel.class)) {
            return (T) new OpenChannelOperatorListViewModel((String) Objects.requireNonNull(params)[0], params.length > 1 ? (PagedQueryHandler<User>) params[1] : null);
        } else if (modelClass.isAssignableFrom(OpenChannelMutedParticipantListViewModel.class)) {
            return (T) new OpenChannelMutedParticipantListViewModel((String) Objects.requireNonNull(params)[0], params.length > 1 ? (PagedQueryHandler<User>) params[1] : null);
        } else if (modelClass.isAssignableFrom(OpenChannelBannedUserListViewModel.class)) {
            return (T) new OpenChannelBannedUserListViewModel((String) Objects.requireNonNull(params)[0]);
        } else if (modelClass.isAssignableFrom(CreateOpenChannelViewModel.class)) {
            return (T) new CreateOpenChannelViewModel();
        } else if (modelClass.isAssignableFrom(OpenChannelListViewModel.class)) {
            return (T) new OpenChannelListViewModel(params != null && params.length > 0 ? (OpenChannelListQueryParams) params[0] : null);
        } else if (modelClass.isAssignableFrom(MessageThreadViewModel.class)) {
            return (T) new MessageThreadViewModel((String) Objects.requireNonNull(params)[0], (BaseMessage) Objects.requireNonNull(params)[1], params.length > 2 ? (ThreadMessageListParams) params[2] : null);
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
