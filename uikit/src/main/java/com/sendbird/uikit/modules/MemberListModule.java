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

import com.sendbird.uikit.test.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.interfaces.LoadingDialogHandler;
import com.sendbird.uikit.internal.ui.widgets.WaitingDialog;
import com.sendbird.uikit.modules.components.HeaderComponent;
import com.sendbird.uikit.modules.components.MemberListComponent;
import com.sendbird.uikit.modules.components.SelectUserHeaderComponent;
import com.sendbird.uikit.modules.components.StatusComponent;

/**
 * A module for member list. This module is composed of a header, list, and status.
 * All composed components are created when the module is created. After than those components can replace.
 *
 * <ul>
 *  <li>Header component is {@link SelectUserHeaderComponent} and you can set custom header component by {@link #setHeaderComponent(HeaderComponent)}
 *  <li>List component is {@link MemberListComponent} and you can set custom list component by {@link #setMemberListComponent(MemberListComponent)}
 *  <li>Status component is {@link StatusComponent} and you can set custom status component by {@link #setStatusComponent(StatusComponent)}
 * </ul>
 *
 * since 3.0.0
 */
public class MemberListModule extends BaseModule {
    @NonNull
    private final Params params;
    @NonNull
    private HeaderComponent headerComponent;
    @NonNull
    private MemberListComponent memberListComponent;
    @NonNull
    private StatusComponent statusComponent;

    @Nullable
    private LoadingDialogHandler loadingDialogHandler;

    /**
     * Constructor
     *
     * @param context The {@code Context} this module is currently associated with
     * since 3.0.0
     */
    public MemberListModule(@NonNull Context context) {
        this(context, new Params(context));
    }

    /**
     * Constructor
     *
     * @param context The {@code Context} this module is currently associated with
     * @param params The Parameter object that can customize a default Fragment.
     * since 3.0.0
     */
    public MemberListModule(@NonNull Context context, @NonNull Params params) {
        this.params = params;
        this.headerComponent = new HeaderComponent();
        this.memberListComponent = new MemberListComponent();
        this.statusComponent = new StatusComponent();
    }

    @Override
    @NonNull
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
            final View header = this.headerComponent.onCreateView(headerThemeContext, headerInflater, parent, args);
            parent.addView(header);
        }

        final FrameLayout innerContainer = new FrameLayout(context);
        innerContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        parent.addView(innerContainer);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_list, values, true);
        final Context listThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater listInflater = inflater.cloneInContext(listThemeContext);
        final View memberListLayout = this.memberListComponent.onCreateView(listThemeContext, listInflater, innerContainer, args);
        innerContainer.addView(memberListLayout);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_status, values, true);
        final Context statusThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater statusInflater = inflater.cloneInContext(statusThemeContext);
        final View statusLayout = statusComponent.onCreateView(statusThemeContext, statusInflater, innerContainer, args);
        innerContainer.addView(statusLayout);
        return parent;
    }

    /**
     * Sets the handler for the loading dialog.
     *
     * @param loadingDialogHandler Loading dialog handler to be used in this module
     * since 3.0.0
     */
    public void setOnLoadingDialogHandler(@Nullable LoadingDialogHandler loadingDialogHandler) {
        this.loadingDialogHandler = loadingDialogHandler;
    }

    /**
     * Returns the handler for loading dialog.
     *
     * @return Loading dialog handler to be used in this module
     * since 3.0.0
     */
    @Nullable
    public LoadingDialogHandler getLoadingDialogHandler() {
        return loadingDialogHandler;
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * since 3.0.0
     */
    public boolean shouldShowLoadingDialog(@NonNull Context context) {
        if (loadingDialogHandler != null && loadingDialogHandler.shouldShowLoadingDialog()) {
            return true;
        }

        WaitingDialog.show(context);
        return true;
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * since 3.0.0
     */
    public void shouldDismissLoadingDialog() {
        if (loadingDialogHandler != null) {
            loadingDialogHandler.shouldDismissLoadingDialog();
            return;
        }
        WaitingDialog.dismiss();
    }

    /**
     * Sets a custom header component.
     *
     * @param component The header component to be used in this module
     * since 3.0.0
     */
    public <T extends HeaderComponent> void setHeaderComponent(@NonNull T component) {
        this.headerComponent = component;
    }

    /**
     * Sets a custom list component.
     *
     * @param component The list component to be used in this module
     * since 3.0.0
     */
    public <T extends MemberListComponent> void setMemberListComponent(@NonNull T component) {
        this.memberListComponent = component;
    }

    /**
     * Sets a custom status component.
     *
     * @param component The status component to be used in this module
     * since 3.0.0
     */
    public <T extends StatusComponent> void setStatusComponent(@NonNull T component) {
        this.statusComponent = component;
    }

    /**
     * Returns the header component.
     *
     * @return The header component of this module
     * since 3.0.0
     */
    @NonNull
    public HeaderComponent getHeaderComponent() {
        return headerComponent;
    }

    /**
     * Returns the list component.
     *
     * @return The list component of this module
     * since 3.0.0
     */
    @NonNull
    public MemberListComponent getMemberListComponent() {
        return memberListComponent;
    }

    /**
     * Returns the status component.
     *
     * @return The status component of this module
     * since 3.0.0
     */
    @NonNull
    public StatusComponent getStatusComponent() {
        return statusComponent;
    }

    /**
     * Returns a collection of parameters applied to this module.
     *
     * @return {@link Params} applied to this module.
     * since 3.0.0
     */
    @NonNull
    public Params getParams() {
        return params;
    }

    public static class Params extends BaseModule.Params {
        /**
         * Constructor
         *
         * @param context The {@code Context} this module is currently associated with
         * since 3.0.0
         */
        public Params(@NonNull Context context) {
            this(context, SendbirdUIKit.getDefaultThemeMode());
        }

        /**
         * Constructor
         *
         * @param context The {@code Context} this module is currently associated with
         * @param themeMode The theme of Sendbird UIKit to be applied to this module
         * since 3.0.0
         */
        public Params(@NonNull Context context, @NonNull SendbirdUIKit.ThemeMode themeMode) {
            super(context, themeMode, R.attr.sb_module_member_list);
        }

        /**
         * Constructor
         *
         * @param context The {@code Context} this module is currently associated with
         * @param themeResId The theme resource ID to be applied to this module
         * since 3.0.0
         */
        public Params(@NonNull Context context, @StyleRes int themeResId) {
            super(context, themeResId, R.attr.sb_module_member_list);
        }
    }
}
