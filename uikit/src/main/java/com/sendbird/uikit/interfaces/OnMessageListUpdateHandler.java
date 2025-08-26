package com.sendbird.uikit.interfaces;

import androidx.annotation.NonNull;

import com.sendbird.android.message.BaseMessage;

import java.util.List;

/**
 * Callback interface called when the message list is updated.
 */
public interface OnMessageListUpdateHandler {
    /**
     * Called when the message list is updated.
     *
     * @param messages Updated {@code List<BaseMessage>}.
     */
    void onListUpdated(@NonNull List<BaseMessage> messages);
}
