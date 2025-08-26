package com.sendbird.uikit.modules.components;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.user.User;
import com.sendbird.uikit.activities.adapter.OpenChannelOperatorListAdapter;

import java.util.List;

/**
 * This class creates and performs a view corresponding the operator list area in Sendbird UIKit.
 *
 * since 3.1.0
 */
public class OpenChannelOperatorListComponent extends UserTypeListComponent<User> {
    @NonNull
    private OpenChannelOperatorListAdapter adapter = new OpenChannelOperatorListAdapter();

    /**
     * Returns the operator list adapter.
     *
     * @return The adapter applied to this list component
     * since 3.1.0
     */
    @NonNull
    @Override
    protected OpenChannelOperatorListAdapter getAdapter() {
        return adapter;
    }

    /**
     * Sets the operator list  adapter to provide child views on demand. The default is {@code new OpenChannelOperatorListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * since 3.1.0
     */
    public <T extends OpenChannelOperatorListAdapter> void setAdapter(@NonNull T adapter) {
        this.adapter = adapter;
        super.setAdapter(this.adapter);
    }

    /**
     * Notifies this component that the list of users is changed.
     *
     * @param userList The list of users to be displayed on this component
     * @param openChannel The latest open channel
     * since 3.1.0
     */
    public void notifyDataSetChanged(@NonNull List<User> userList, @NonNull OpenChannel openChannel) {
        this.adapter.setItems(userList, openChannel);
    }
}
