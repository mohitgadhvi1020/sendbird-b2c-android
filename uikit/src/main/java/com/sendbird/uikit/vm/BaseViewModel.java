package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.ViewModel;

import com.sendbird.android.handler.ConnectHandler;
import com.sendbird.uikit.interfaces.AuthenticateHandler;
import com.sendbird.uikit.internal.contracts.SendbirdUIKitImpl;
import com.sendbird.uikit.internal.contracts.SendbirdUIKitContract;
import com.sendbird.uikit.log.Logger;

/**
 * ViewModel preparing and managing data commonly used in UIKit's Activities or Fragments.
 *
 * since 3.0.0
 */
public abstract class BaseViewModel extends ViewModel {
    @NonNull
    protected final SendbirdUIKitContract sendbirdUIKit;

    protected BaseViewModel() {
        this(new SendbirdUIKitImpl());
    }

    @VisibleForTesting
    BaseViewModel(@NonNull SendbirdUIKitContract sendbirdUIKit) {
        this.sendbirdUIKit = sendbirdUIKit;
    }

    void connect(@NonNull ConnectHandler handler) {
        Logger.dev(">> BaseViewModel::connect()");
        sendbirdUIKit.connect(handler);
    }

    /**
     * Authenticates before using ViewModels data.
     *
     * @param handler Callback notifying the result of authentication
     * since 3.0.0
     */
    abstract public void authenticate(@NonNull AuthenticateHandler handler);
}
