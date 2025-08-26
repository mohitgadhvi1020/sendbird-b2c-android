package com.sendbird.uikit.modules.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.sendbird.uikit.test.R;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.internal.ui.components.StateHeaderView;

/**
 * This class creates and performs a view corresponding the stateful header area in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class StateHeaderComponent {
    @NonNull
    private final Params params;
    @Nullable
    private StateHeaderView headerView;

    @Nullable
    private View.OnClickListener leftButtonClickListener;
    @Nullable
    private View.OnClickListener rightButtonClickListener;

    /**
     * Constructor
     *
     * since 3.0.0
     */
    public StateHeaderComponent() {
        this.params = new Params();
    }

    /**
     * Returns the view created by {@link #onCreateView(Context, LayoutInflater, ViewGroup, Bundle)}.
     *
     * @return the topmost view containing this view
     * since 3.0.0
     */
    @Nullable
    public View getRootView() {
        return this.headerView;
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
        final StateHeaderView headerView = new StateHeaderView(context, null, R.attr.sb_component_state_header);

        headerView.setUseLeftButton(params.useLeftButton);
        headerView.setUseRightButton(params.useRightButton);

        if (params.title != null) {
            headerView.getTitleTextView().setText(params.title);
        }
        if (params.leftButtonIcon != null) {
            headerView.setLeftButtonImageDrawable(params.leftButtonIcon);
        }
        if (params.leftButtonIconTint != null) {
            headerView.setLeftButtonTint(params.leftButtonIconTint);
        }
        if (params.rightButtonText != null) {
            headerView.setRightButtonText(params.rightButtonText);
        }

        headerView.setOnLeftButtonClickListener(this::onLeftButtonClicked);
        headerView.setOnRightButtonClickListener(this::onRightButtonClicked);
        this.headerView = headerView;
        return headerView;
    }

    /**
     * Register a callback to be invoked when the left button of the header is clicked.
     *
     * @param leftButtonClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnLeftButtonClickListener(@Nullable View.OnClickListener leftButtonClickListener) {
        this.leftButtonClickListener = leftButtonClickListener;
    }

    /**
     * Register a callback to be invoked when the right button of the header is clicked.
     *
     * @param rightButtonClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnRightButtonClickListener(@Nullable View.OnClickListener rightButtonClickListener) {
        this.rightButtonClickListener = rightButtonClickListener;
    }

    /**
     * Called when the left button of the header is clicked.
     *
     * @param view The view that was clicked.
     * since 3.0.0
     */
    protected void onLeftButtonClicked(@NonNull View view) {
        if (this.leftButtonClickListener != null) this.leftButtonClickListener.onClick(view);
    }

    /**
     * Called when the right button of the header is clicked.
     *
     * @param view The view that was clicked.
     * since 3.0.0
     */
    protected void onRightButtonClicked(@NonNull View view) {
        if (this.rightButtonClickListener != null) this.rightButtonClickListener.onClick(view);
    }

    /**
     * Notifies the events to the view when the right button enable state changed.
     *
     * @param enabled the enable state of the view
     * since 3.2.0
     */
    public void notifyRightButtonEnableStateChanged(boolean enabled) {
        if (headerView != null) {
            headerView.setEnabledRightButton(enabled);
        }
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
        private boolean useRightButton = true;
        private boolean useLeftButton = true;
        @Nullable
        private String title;
        @Nullable
        private Drawable leftButtonIcon;
        @Nullable
        private String rightButtonText;
        @Nullable
        private ColorStateList leftButtonIconTint;

        /**
         * Constructor
         *
         * since 3.0.0
         */
        protected Params() {
        }

        /**
         * Sets the title of the header.
         *
         * @param title The String to be displayed in the title of header
         * since 3.0.0
         */
        public void setTitle(@Nullable String title) {
            this.title = title;
        }

        /**
         * Sets the icon on the left button of the header.
         *
         * @param leftButtonIcon The Drawable to be displayed on the left button of the header
         * since 3.0.0
         */
        public void setLeftButtonIcon(@Nullable Drawable leftButtonIcon) {
            this.leftButtonIcon = leftButtonIcon;
        }

        /**
         * Sets whether the right button of the header is used.
         *
         * @param useRightButton <code>true</code> if the right button of the header is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public void setUseRightButton(boolean useRightButton) {
            this.useRightButton = useRightButton;
        }

        /**
         * Sets whether the left button of the header is used.
         *
         * @param useLeftButton <code>true</code> if the left button of the header is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public void setUseLeftButton(boolean useLeftButton) {
            this.useLeftButton = useLeftButton;
        }

        /**
         * Sets the color of the icon on the left button of the header.
         *
         * @param leftButtonIconTint Color state list to be applied to the left button icon
         * since 3.0.0
         */
        public void setLeftButtonIconTint(@Nullable ColorStateList leftButtonIconTint) {
            this.leftButtonIconTint = leftButtonIconTint;
        }

        /**
         * Sets the text on the right button of the header.
         *
         * @param rightButtonText The String to be displayed on the right button
         * since 3.0.0
         */
        public void setRightButtonText(@Nullable String rightButtonText) {
            this.rightButtonText = rightButtonText;
        }

        /**
         * Returns the title of the header.
         *
         * @return The String displayed in the title of header
         * since 3.0.0
         */
        @Nullable
        public String getTitle() {
            return title;
        }

        /**
         * Returns the icon on the left button of the header.
         *
         * @return The Drawable displayed on the left button of the header
         * since 3.0.0
         */
        @Nullable
        public Drawable getLeftButtonIcon() {
            return leftButtonIcon;
        }

        /**
         * Returns the text on the right button of the header.
         *
         * @return The String displayed on the right button
         * since 3.0.0
         */
        @Nullable
        public String getRightButtonText() {
            return rightButtonText;
        }

        /**
         * Returns whether the right button of the header is used.
         *
         * @return <code>true</code> if the right button of the header is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public boolean shouldUseRightButton() {
            return useRightButton;
        }

        /**
         * Returns whether the left button of the header is used.
         *
         * @return <code>true</code> if the left button of the header is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public boolean shouldUseLeftButton() {
            return useLeftButton;
        }

        /**
         * Returns the color of the icon on the left button of the header.
         *
         * @return Color state list applied to the left button icon
         * since 3.0.0
         */
        @Nullable
        public ColorStateList getLeftButtonIconTint() {
            return leftButtonIconTint;
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         *
         * <li>{@code KEY_USE_HEADER_LEFT_BUTTON} is mapped to {@link #setUseLeftButton(boolean)}</li>
         * <li>{@code KEY_USE_HEADER_RIGHT_BUTTON} is mapped to {@link #setUseRightButton(boolean)}</li>
         * <li>{@code KEY_HEADER_LEFT_BUTTON_ICON_RES_ID} is mapped to {@link #setLeftButtonIcon(Drawable)}</li>
         * <li>{@code KEY_HEADER_LEFT_BUTTON_ICON_TINT} is mapped to {@link #setLeftButtonIconTint(ColorStateList)}</li>
         * <li>{@code KEY_HEADER_RIGHT_BUTTON_TEXT} is mapped to {@link #setRightButtonText(String)}</li>
         * <li>{@code KEY_HEADER_TITLE} is mapped to {@link #setTitle(String)}</li>
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.0.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            if (args.containsKey(StringSet.KEY_USE_HEADER_LEFT_BUTTON)) {
                setUseLeftButton(args.getBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON));
            }
            if (args.containsKey(StringSet.KEY_USE_HEADER_RIGHT_BUTTON)) {
                setUseRightButton(args.getBoolean(StringSet.KEY_USE_HEADER_RIGHT_BUTTON));
            }
            if (args.containsKey(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID)) {
                setLeftButtonIcon(ContextCompat.getDrawable(context, args.getInt(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID)));
            }
            if (args.containsKey(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_TINT)) {
                setLeftButtonIconTint(args.getParcelable(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_TINT));
            }
            if (args.containsKey(StringSet.KEY_HEADER_RIGHT_BUTTON_TEXT)) {
                setRightButtonText(args.getString(StringSet.KEY_HEADER_RIGHT_BUTTON_TEXT));
            }
            if (args.containsKey(StringSet.KEY_HEADER_TITLE)) {
                setTitle(args.getString(StringSet.KEY_HEADER_TITLE));
            }
            return this;
        }
    }
}
