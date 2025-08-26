package com.sendbird.uikit.modules.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.uikit.test.R;
import com.sendbird.uikit.consts.KeyboardDisplayType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.OnInputModeChangedListener;
import com.sendbird.uikit.interfaces.OnInputTextChangedListener;
import com.sendbird.uikit.internal.ui.widgets.MessageInputDialogWrapper;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.TextUIConfig;
import com.sendbird.uikit.widgets.MessageInputView;

/**
 * This class creates and performs a view corresponding the message input area for {@code OpenChannel} in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class OpenChannelMessageInputComponent {
    @NonNull
    private final Params params;
    @Nullable
    private MessageInputView messageInputView;

    @Nullable
    private View.OnClickListener inputRightButtonClickListener;
    @Nullable
    private View.OnClickListener inputLeftButtonClickListener;
    @Nullable
    private View.OnClickListener editModeCancelButtonClickListener;
    @Nullable
    private View.OnClickListener editModeSaveButtonClickListener;
    @Nullable
    private OnInputTextChangedListener inputTextChangedListener;
    @Nullable
    private OnInputTextChangedListener editModeTextChangedListener;
    @Nullable
    private OnInputModeChangedListener inputModeChangedListener;
    private boolean isMuted;

    /**
     * Constructor
     *
     * since 3.0.0
     */
    public OpenChannelMessageInputComponent() {
        this.params = new Params();
    }

    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return {@code Params} applied to this component
     * since 3.0.0
     */
    @NonNull
    public Params getParams() {
        return params;
    }

    /**
     * Returns the view created by {@link #onCreateView(Context, LayoutInflater, ViewGroup, Bundle)}.
     *
     * @return the topmost view containing this view
     * since 3.0.0
     */
    @Nullable
    public View getRootView() {
        return this.messageInputView;
    }

    /**
     * Returns the edit text view used in the input component bt default.
     *
     * @return {@link EditText} used in this component
     * since 3.0.0
     */
    @Nullable
    public EditText getEditTextView() {
        if (messageInputView == null) return null;
        return messageInputView.getInputEditText();
    }

    /**
     * Called after the component was created to make views.
     * <p><b>If this function is used override, {@link #getRootView()} must also be override.</b></p>
     *
     * @param context  The {@code Context} this component is currently associated with
     * @param inflater The LayoutInflater object that can be used to inflate any views in the component
     * @param parent   The ViewGroup into which the new View will be added
     * @param args     The arguments supplied when the component was instantiated, if any
     * @return Return the View for the UI.
     * since 3.0.0
     */
    @NonNull
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle args) {
        if (args != null) params.apply(context, args);
        final MessageInputView messageInputView = new MessageInputView(context, null, R.attr.sb_component_open_channel_message_input);
        if (params.leftButtonIcon != null) {
            messageInputView.setAddImageDrawable(params.leftButtonIcon);
        }
        if (params.leftButtonIconTint != null) {
            messageInputView.setAddImageButtonTint(params.leftButtonIconTint);
        }
        if (params.rightButtonIcon != null) {
            messageInputView.setSendImageDrawable(params.rightButtonIcon);
        }
        if (params.rightButtonIconTint != null) {
            messageInputView.setSendImageButtonTint(params.rightButtonIconTint);
        }
        if (params.hintText != null) {
            messageInputView.setInputTextHint(params.hintText);
        }
        params.setInputHint(messageInputView.getInputTextHint());
        if (params.inputText != null) {
            messageInputView.setInputText(params.inputText);
        }
        if (params.textUIConfig != null) {
            messageInputView.applyTextUIConfig(params.textUIConfig);
        }
        messageInputView.setAddButtonVisibility(params.useLeftButton ? View.VISIBLE : View.GONE);

        if (params.alwaysShowRightButton) messageInputView.setSendButtonVisibility(View.VISIBLE);
        messageInputView.setShowSendButtonAlways(params.alwaysShowRightButton);
        messageInputView.setOnSendClickListener(this::onInputRightButtonClicked);
        messageInputView.setOnAddClickListener(this::onInputLeftButtonClicked);
        messageInputView.setOnEditCancelClickListener(this::onEditModeCancelButtonClicked);
        messageInputView.setOnEditSaveClickListener(this::onEditModeSaveButtonClicked);
        messageInputView.setOnInputTextChangedListener(this::onInputTextChanged);
        messageInputView.setOnEditModeTextChangedListener(this::onEditModeTextChanged);
        messageInputView.setOnInputModeChangedListener(this::onInputModeChanged);

        this.messageInputView = messageInputView;
        if (params.keyboardDisplayType == KeyboardDisplayType.Dialog) {
            final MessageInputDialogWrapper messageInputDialogWrapper = new MessageInputDialogWrapper(context);
            messageInputDialogWrapper.initInputView(messageInputView);
            return messageInputDialogWrapper;
        } else {
            return messageInputView;
        }
    }

    /**
     * Register a callback to be invoked when the right button of the input is clicked.
     *
     * @param inputRightButtonClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnInputRightButtonClickListener(@Nullable View.OnClickListener inputRightButtonClickListener) {
        this.inputRightButtonClickListener = inputRightButtonClickListener;
    }

    /**
     * Register a callback to be invoked when the left button of the input is clicked.
     *
     * @param inputLeftButtonClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnInputLeftButtonClickListener(@Nullable View.OnClickListener inputLeftButtonClickListener) {
        this.inputLeftButtonClickListener = inputLeftButtonClickListener;
    }

    /**
     * Register a callback to be invoked when the cancel button is clicked, when the input is the edited mode.
     *
     * @param editModeCancelButtonClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnEditModeCancelButtonClickListener(@Nullable View.OnClickListener editModeCancelButtonClickListener) {
        this.editModeCancelButtonClickListener = editModeCancelButtonClickListener;
    }

    /**
     * Register a callback to be invoked when the save button is clicked, when the input is the edited mode.
     *
     * @param editModeSaveButtonClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnEditModeSaveButtonClickListener(@Nullable View.OnClickListener editModeSaveButtonClickListener) {
        this.editModeSaveButtonClickListener = editModeSaveButtonClickListener;
    }

    /**
     * Register a callback to be invoked when the input text is changed, when the input is the edited mode.
     *
     * @param editModeTextChangedListener The callback that will run
     * since 3.0.0
     */
    public void setOnEditModeTextChangedListener(@Nullable OnInputTextChangedListener editModeTextChangedListener) {
        this.editModeTextChangedListener = editModeTextChangedListener;
    }

    /**
     * Register a callback to be invoked when the input text is changed.
     *
     * @param inputTextChangedListener The callback that will run
     * since 3.0.0
     */
    public void setOnInputTextChangedListener(@Nullable OnInputTextChangedListener inputTextChangedListener) {
        this.inputTextChangedListener = inputTextChangedListener;
    }

    /**
     * Register a callback to be invoked when the input mode is changed.
     *
     * @param inputModeChangedListener The callback that will run
     * since 3.0.0
     */
    public void setOnInputModeChangedListener(@Nullable OnInputModeChangedListener inputModeChangedListener) {
        this.inputModeChangedListener = inputModeChangedListener;
    }

    /**
     * Called when the left button of the input is clicked.
     *
     * @param view The View clicked
     * since 3.0.0
     */
    protected void onInputLeftButtonClicked(@NonNull View view) {
        if (inputLeftButtonClickListener != null) inputLeftButtonClickListener.onClick(view);
    }

    /**
     * Called when the right button of the input is clicked.
     *
     * @param view The View clicked
     * since 3.0.0
     */
    protected void onInputRightButtonClicked(@NonNull View view) {
        if (inputRightButtonClickListener != null) inputRightButtonClickListener.onClick(view);
    }

    /**
     * Called when the input text is changed, when the input is the edited mode.
     * <p>
     * This method is called to notify you that, within <code>s</code>,
     * the <code>count</code> characters beginning at <code>start</code>
     * have just replaced old text that had length <code>before</code>.
     * It is an error to attempt to make changes to <code>s</code> from
     * this callback.
     * </p>
     *
     * since 3.0.0
     */
    protected void onEditModeTextChanged(@NonNull CharSequence s, int start, int before, int count) {
        if (editModeTextChangedListener != null)
            editModeTextChangedListener.onInputTextChanged(s, start, before, count);
    }

    /**
     * Called when the input text is changed.
     * <p>
     * This method is called to notify you that, within <code>s</code>,
     * the <code>count</code> characters beginning at <code>start</code>
     * have just replaced old text that had length <code>before</code>.
     * It is an error to attempt to make changes to <code>s</code> from
     * this callback.
     * </p>
     *
     * since 3.0.0
     */
    protected void onInputTextChanged(@NonNull CharSequence s, int start, int before, int count) {
        if (inputTextChangedListener != null)
            inputTextChangedListener.onInputTextChanged(s, start, before, count);
    }

    /**
     * Called when the input mode is changed.
     *
     * @param before  Input mode before change
     * @param current The latest input mode
     * since 3.0.0
     */
    protected void onInputModeChanged(@NonNull MessageInputView.Mode before, @NonNull MessageInputView.Mode current) {
        if (inputModeChangedListener != null)
            inputModeChangedListener.onInputModeChanged(before, current);
    }


    /**
     * Called when the cancel button is clicked, when the input is the edited mode.
     *
     * @param view The View clicked
     * since 3.0.0
     */
    protected void onEditModeCancelButtonClicked(@NonNull View view) {
        if (editModeCancelButtonClickListener != null)
            editModeCancelButtonClickListener.onClick(view);
    }

    /**
     * Called when the save button is clicked, when the input is the edited mode.
     *
     * @param view The View clicked
     * since 3.0.0
     */
    protected void onEditModeSaveButtonClicked(@NonNull View view) {
        if (editModeSaveButtonClickListener != null) editModeSaveButtonClickListener.onClick(view);
    }

    /**
     * Notifies this component that the channel data has changed.
     *
     * @param channel The latest open channel
     * since 3.0.0
     */
    public void notifyChannelChanged(@NonNull OpenChannel channel) {
        if (messageInputView == null) return;
        final MessageInputView inputView = this.messageInputView;
        setHintMessageText(inputView, channel);
    }

    /**
     * Notifies this component that the data needed to draw the input has changed.
     *
     * @param message Message required for current input information
     * @param channel The latest open channel
     * since 3.0.0
     */
    public void notifyDataChanged(@Nullable BaseMessage message, @NonNull OpenChannel channel) {
        notifyDataChanged(message, channel, "");
    }

    /**
     * Notifies this component that the data needed to draw the input has changed.
     *
     * @param message     Message required for current input information
     * @param channel     The latest open channel
     * @param defaultText Text set as initial value for input
     * since 3.0.0
     */
    public void notifyDataChanged(@Nullable BaseMessage message, @NonNull OpenChannel channel, @NonNull String defaultText) {
        if (messageInputView == null) return;
        final MessageInputView inputView = this.messageInputView;

        final MessageInputView.Mode mode = inputView.getInputMode();
        if (MessageInputView.Mode.EDIT == mode) {
            if (message != null) inputView.setInputText(message.getMessage());
            inputView.showKeyboard();
        } else {
            inputView.setInputText(defaultText);
            final CharSequence text = inputView.getInputText();
            if (text != null) {
                inputView.getInputEditText().setSelection(text.length());
            }
        }

        setHintMessageText(inputView, channel);
    }

    /**
     * Requests to set the input mode.
     *
     * @param mode Input mode to be set to this component
     * @see MessageInputView.Mode
     * since 3.0.0
     */
    public void requestInputMode(@NonNull MessageInputView.Mode mode) {
        if (messageInputView == null) return;
        this.messageInputView.setInputMode(mode);
    }

    /**
     * Notifies this component that the muted state of the current user has changed.
     *
     * @param channel The latest open channel
     * @param isMuted Whether the current user is muted or not
     * since 3.1.0
     */
    public void notifyMyMutedStateChanged(@NonNull OpenChannel channel, boolean isMuted) {
        this.isMuted = isMuted;
        if (messageInputView == null) return;
        final MessageInputView inputView = this.messageInputView;
        setHintMessageText(inputView, channel);
    }

    /**
     * Enables or disables the input view with hint text.
     * If the user's status is not allowed to send messages, the enable value cannot be changed.
     *
     * @param channel  The open channel to be checked for the user's status
     * @param enabled <code>true</code> if the input view is enabled, <code>false</code> otherwise
     * @param hintText The hint text to be displayed
     * @return <code>true</code> if the enable setting is reflected, <code>false</code> otherwise
     * since 3.17.0
     */
    public boolean tryToChangeEnableStatus(@NonNull OpenChannel channel, boolean enabled, @NonNull String hintText) {
        if (messageInputView == null) return false;

        params.isViewEnabled = enabled;
        params.hintText = hintText;
        boolean shouldEnableInput = calculateViewEnableStatus(channel);
        messageInputView.setEnabled(shouldEnableInput);

        boolean isMessageSendable = isMessageSendable(channel);
        boolean isEnableStateChanged = isMessageSendable && shouldEnableInput == enabled;
        if (isEnableStateChanged) {
            messageInputView.setInputTextHint(hintText);
        }
        return isEnableStateChanged;
    }

    private boolean isMessageSendable(@NonNull OpenChannel channel) {
        boolean isOperator = channel.isOperator(SendbirdChat.getCurrentUser());
        boolean isFrozen = channel.isFrozen() && !isOperator;
        return !isMuted && !isFrozen;
    }

    private boolean calculateViewEnableStatus(@NonNull OpenChannel channel) {
        boolean messageSenable = isMessageSendable(channel);
        if (messageSenable) {
            return params.isViewEnabled;
        }
        return false;
    }

    private void setHintMessageText(@NonNull MessageInputView inputView, @NonNull OpenChannel channel) {
        boolean isOperator = channel.isOperator(SendbirdChat.getCurrentUser());
        boolean isFrozen = channel.isFrozen() && !isOperator;
        boolean shouldEnableInput = calculateViewEnableStatus(channel);
        inputView.setEnabled(shouldEnableInput);

        // set hint
        final Context context = inputView.getContext();
        String hintText = params.hintText != null ? params.hintText : null;
        if (isMuted) {
            hintText = context.getString(R.string.sb_text_channel_input_text_hint_muted);
        } else if (isFrozen) {
            hintText = context.getString(R.string.sb_text_channel_input_text_hint_frozen);
        }
        Logger.dev("++ hint text : " + hintText);
        inputView.setInputTextHint(hintText);
    }

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     * <p>Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.</p>
     *
     * @see #getParams()
     * since 3.0.0
     */
    public static class Params {
        private boolean useLeftButton = true;
        private boolean alwaysShowRightButton = false;
        @Nullable
        private Drawable leftButtonIcon;
        @Nullable
        private Drawable rightButtonIcon;
        @Nullable
        private ColorStateList leftButtonIconTint;
        @Nullable
        private ColorStateList rightButtonIconTint;
        @Nullable
        private String hintText;
        @Nullable
        private String inputText;
        private boolean isViewEnabled = true;
        @NonNull
        private KeyboardDisplayType keyboardDisplayType = KeyboardDisplayType.Plane;
        @Nullable
        private TextUIConfig textUIConfig;

        /**
         * Constructor
         *
         * since 3.0.0
         */
        protected Params() {
        }

        /**
         * Sets the icon on the left button of the input view.
         *
         * @param leftButtonIcon The Drawable to be displayed on the left button of the input view
         * since 3.0.0
         */
        public void setLeftButtonIcon(@Nullable Drawable leftButtonIcon) {
            this.leftButtonIcon = leftButtonIcon;
        }

        /**
         * Sets the icon on the right button of the input view.
         *
         * @param rightButtonIcon The Drawable to be displayed on the right button of the input view
         * since 3.0.0
         */
        public void setRightButtonIcon(@Nullable Drawable rightButtonIcon) {
            this.rightButtonIcon = rightButtonIcon;
        }

        /**
         * Sets whether the left button of the input view is used.
         *
         * @param useLeftButton <code>true</code> if the left button of the input view is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public void setUseLeftButton(boolean useLeftButton) {
            this.useLeftButton = useLeftButton;
        }

        /**
         * Sets the color of the icon on the left button of the input view.
         *
         * @param leftButtonIconTint Color state list to be applied to the left button icon
         * since 3.0.0
         */
        public void setLeftButtonIconTint(@Nullable ColorStateList leftButtonIconTint) {
            this.leftButtonIconTint = leftButtonIconTint;
        }

        /**
         * Sets the color of the icon on the right button of the input view.
         *
         * @param rightButtonIconTint Color state list to be applied to the right button icon
         * since 3.0.0
         */
        public void setRightButtonIconTint(@Nullable ColorStateList rightButtonIconTint) {
            this.rightButtonIconTint = rightButtonIconTint;
        }

        /**
         * Shows always the right button of the input view.
         *
         * since 3.0.0
         */
        public void showInputRightButtonAlways() {
            this.alwaysShowRightButton = true;
        }

        /**
         * Sets the hint of the input view.
         *
         * @param hint The String displayed as a hint message
         * since 3.0.0
         */
        public void setInputHint(@Nullable String hint) {
            this.hintText = hint;
        }

        /**
         * Sets the input text.
         *
         * @param inputText The String to be set on the input view
         * since 3.0.0
         */
        public void setInputText(@Nullable String inputText) {
            this.inputText = inputText;
        }

        /**
         * Sets the keyboard display type. (Refer to {@link KeyboardDisplayType})
         *
         * @param type Keyboard display type to be used in this component
         * @see KeyboardDisplayType
         * since 3.0.0
         */
        public void setKeyboardDisplayType(@NonNull KeyboardDisplayType type) {
            this.keyboardDisplayType = type;
        }

        /**
         * Returns the keyboard display type. (Refer to {@link KeyboardDisplayType})
         *
         * @return Keyboard display type used in this component
         * since 3.0.0
         */
        @NonNull
        public KeyboardDisplayType getKeyboardDisplayType() {
            return keyboardDisplayType;
        }

        /**
         * Returns whether the left button of the input view is used.
         *
         * @return <code>true</code> if the left button of the input view is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public boolean shouldUseLeftButton() {
            return useLeftButton;
        }

        /**
         * Returns whether the right button of the input view is shown always.
         *
         * @return <code>true</code> if the right button of the input view is shown always, <code>false</code> otherwise
         * since 3.0.0
         */
        public boolean isAlwaysShowRightButton() {
            return alwaysShowRightButton;
        }

        /**
         * Returns the icon on the left button of the input view.
         *
         * @return The Drawable to be displayed on the left button of the input view
         * since 3.0.0
         */
        @Nullable
        public Drawable getLeftButtonIcon() {
            return leftButtonIcon;
        }

        /**
         * Returns the icon on the right button of the input view.
         *
         * @return The Drawable to be displayed on the right button of the input view
         * since 3.0.0
         */
        @Nullable
        public Drawable getRightButtonIcon() {
            return rightButtonIcon;
        }

        /**
         * Returns the color of the icon on the left button of the input view.
         *
         * @return Color state list to be applied to the left button icon
         * since 3.0.0
         */
        @Nullable
        public ColorStateList getLeftButtonIconTint() {
            return leftButtonIconTint;
        }

        /**
         * Returns the color of the icon on the right button of the input view.
         *
         * @return Color state list to be applied to the right button icon
         * since 3.0.0
         */
        @Nullable
        public ColorStateList getRightButtonIconTint() {
            return rightButtonIconTint;
        }

        /**
         * Returns the hint of the input view.
         *
         * @return The String displayed as a hint message
         * since 3.0.0
         */
        @Nullable
        public String getHintText() {
            return hintText;
        }

        /**
         * Returns the input text.
         *
         * @return The String to be set on the input view
         * since 3.0.0
         */
        @Nullable
        public String getInputText() {
            return inputText;
        }

        /**
         * Sets the UI configuration of message input text.
         *
         * @param textUIConfig the UI configuration of the message input text.
         * since 3.2.1
         */
        public void setMessageInputTextUIConfig(@Nullable TextUIConfig textUIConfig) {
            this.textUIConfig = textUIConfig;
        }

        /**
         * Returns the UI configuration of message input text.
         *
         * @return the UI configuration of message input text.
         * since 3.2.1
         */
        @Nullable
        public TextUIConfig getMessageInputTextUIConfig() {
            return textUIConfig;
        }

        /**
         * Returns whether the input view is enabled.
         *
         * @return <code>true</code> if the input view is enabled, <code>false</code> otherwise
         * since 3.17.0
         */
        public boolean isViewEnabled() {
            return isViewEnabled;
        }

        /**
         * Sets whether the input view is enabled.
         *
         * @param enabled <code>true</code> if the input view is enabled, <code>false</code> otherwise
         * since 3.17.0
         */
        public void setViewEnabled(boolean enabled) {
            this.isViewEnabled = enabled;
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         * {@code KEY_INPUT_LEFT_BUTTON_ICON_RES_ID} is mapped to {@link #setLeftButtonIcon(Drawable)}
         * {@code KEY_INPUT_LEFT_BUTTON_ICON_TINT} is mapped to {@link #setLeftButtonIconTint(ColorStateList)}
         * {@code KEY_INPUT_RIGHT_BUTTON_ICON_RES_ID} is mapped to {@link #setRightButtonIconTint(ColorStateList)}
         * {@code KEY_INPUT_RIGHT_BUTTON_ICON_TINT} is mapped to {@link #setRightButtonIconTint(ColorStateList)}
         * {@code KEY_INPUT_HINT} is mapped to {@link #setInputHint(String)}
         * {@code KEY_INPUT_TEXT} is mapped to {@link #setInputText(String)}
         * {@code KEY_USE_INPUT_LEFT_BUTTON} is mapped to {@link #setUseLeftButton(boolean)}
         * {@code KEY_INPUT_RIGHT_BUTTON_SHOW_ALWAYS} is mapped to {@link #showInputRightButtonAlways()}
         * {@code KEY_KEYBOARD_DISPLAY_TYPE} is mapped to {@link #setKeyboardDisplayType(KeyboardDisplayType)}
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.0.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            if (args.containsKey(StringSet.KEY_INPUT_LEFT_BUTTON_ICON_RES_ID)) {
                setLeftButtonIcon(ContextCompat.getDrawable(context, args.getInt(StringSet.KEY_INPUT_LEFT_BUTTON_ICON_RES_ID)));
            }
            if (args.containsKey(StringSet.KEY_INPUT_LEFT_BUTTON_ICON_TINT)) {
                setLeftButtonIconTint(args.getParcelable(StringSet.KEY_INPUT_LEFT_BUTTON_ICON_TINT));
            }
            if (args.containsKey(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_RES_ID)) {
                setRightButtonIcon(ContextCompat.getDrawable(context, args.getInt(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_RES_ID)));
            }
            if (args.containsKey(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_TINT)) {
                setRightButtonIconTint(args.getParcelable(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_TINT));
            }
            if (args.containsKey(StringSet.KEY_INPUT_HINT)) {
                setInputHint(args.getString(StringSet.KEY_INPUT_HINT));
            }
            if (args.containsKey(StringSet.KEY_INPUT_TEXT)) {
                setInputText(args.getString(StringSet.KEY_INPUT_TEXT, ""));
            }
            if (args.containsKey(StringSet.KEY_USE_INPUT_LEFT_BUTTON)) {
                setUseLeftButton(args.getBoolean(StringSet.KEY_USE_INPUT_LEFT_BUTTON));
            }
            if (args.containsKey(StringSet.KEY_INPUT_RIGHT_BUTTON_SHOW_ALWAYS)) {
                if (args.getBoolean(StringSet.KEY_INPUT_RIGHT_BUTTON_SHOW_ALWAYS)) {
                    showInputRightButtonAlways();
                }
            }
            if (args.containsKey(StringSet.KEY_KEYBOARD_DISPLAY_TYPE)) {
                final KeyboardDisplayType displayType = (KeyboardDisplayType) args.getSerializable(StringSet.KEY_KEYBOARD_DISPLAY_TYPE);
                if (displayType != null) {
                    setKeyboardDisplayType(displayType);
                }
            }
            if (args.containsKey(StringSet.KEY_MESSAGE_INPUT_TEXT_UI_CONFIG)) {
                setMessageInputTextUIConfig(args.getParcelable(StringSet.KEY_MESSAGE_INPUT_TEXT_UI_CONFIG));
            }
            return this;
        }
    }
}
