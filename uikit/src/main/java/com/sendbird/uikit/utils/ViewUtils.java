package com.sendbird.uikit.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.CustomizableMessage;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.OGMetaData;
import com.sendbird.android.message.Thumbnail;
import com.sendbird.android.user.Sender;
import com.sendbird.android.user.User;
import com.sendbird.uikit.test.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.consts.ReplyType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.internal.extensions.MarkdownExtensionsKt;
import com.sendbird.uikit.internal.extensions.MarkdownType;
import com.sendbird.uikit.internal.model.GlideCachedUrlLoader;
import com.sendbird.uikit.internal.singleton.MessageDisplayDataManager;
import com.sendbird.uikit.internal.ui.messages.BaseQuotedMessageView;
import com.sendbird.uikit.internal.ui.messages.OgtagView;
import com.sendbird.uikit.internal.ui.messages.ThreadInfoView;
import com.sendbird.uikit.internal.ui.messages.VoiceMessageView;
import com.sendbird.uikit.internal.ui.reactions.EmojiReactionListView;
import com.sendbird.uikit.internal.ui.widgets.RoundCornerView;
import com.sendbird.uikit.internal.ui.widgets.VoiceProgressView;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.FileInfo;
import com.sendbird.uikit.model.MentionSpan;
import com.sendbird.uikit.model.MessageDisplayData;
import com.sendbird.uikit.model.MessageListUIParams;
import com.sendbird.uikit.model.MessageUIConfig;
import com.sendbird.uikit.model.TextUIConfig;
import com.sendbird.uikit.model.UserMessageDisplayData;
import com.sendbird.uikit.model.configurations.ChannelConfig;
import com.sendbird.uikit.vm.PendingMessageRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.Unit;

/**
 * The helper class for the drawing views in the UIKit.
 * It is used to draw common UI from each custom component.
 */
public class ViewUtils {
    private final static int MINIMUM_THUMBNAIL_WIDTH = 100;
    private final static int MINIMUM_THUMBNAIL_HEIGHT = 100;
    public static final Pattern MENTION = Pattern.compile("[" + SendbirdUIKit.getUserMentionConfig().getTrigger() + "][{](.*?)([}])");

    public static void drawUnknownMessage(@NonNull TextView view, boolean isMine) {
        int unknownHintAppearance;
        if (isMine) {
            unknownHintAppearance = SendbirdUIKit.isDarkMode() ? R.style.SendbirdBody3OnLight02 : R.style.SendbirdBody3OnDark02;
        } else {
            unknownHintAppearance = SendbirdUIKit.isDarkMode() ? R.style.SendbirdBody3OnDark03 : R.style.SendbirdBody3OnLight02;
        }

        final int sizeOfFirstLine = 23;
        String unknownHintText = view.getContext().getResources().getString(R.string.sb_text_channel_unknown_type_text);
        final Spannable spannable = new SpannableString(unknownHintText);
        spannable.setSpan(new TextAppearanceSpan(view.getContext(), unknownHintAppearance), sizeOfFirstLine, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        view.setText(spannable);
    }

    public static void drawTextMessage(@NonNull TextView textView, @Nullable BaseMessage message, @Nullable MessageUIConfig uiConfig, boolean enableMention) {
        drawTextMessage(textView, message, uiConfig, enableMention, null, null);
    }


    public static void drawTextMessage(
        @NonNull TextView textView,
        @Nullable BaseMessage message,
        @Nullable MessageUIConfig uiConfig,
        boolean enableMention,
        @Nullable TextUIConfig mentionedCurrentUserUIConfig,
        @Nullable OnItemClickListener<User> mentionClickListener
    ) {
        drawTextMessage(textView, message, uiConfig, enableMention, false, mentionedCurrentUserUIConfig, mentionClickListener);
    }

    public static void drawTextMessage(
        @NonNull TextView textView,
        @Nullable BaseMessage message,
        @Nullable MessageUIConfig uiConfig,
        boolean enableMention,
        boolean enableMarkdown,
        @Nullable TextUIConfig mentionedCurrentUserUIConfig,
        @Nullable OnItemClickListener<User> mentionClickListener
    ) {
        if (message == null) {
            return;
        }

        if (MessageUtils.isUnknownType(message)) {
            drawUnknownMessage(textView, MessageUtils.isMine(message));
            return;
        }

        final boolean isMine = MessageUtils.isMine(message);
        final Context context = textView.getContext();
        final CharSequence text = getDisplayableText(
            context,
            message,
            uiConfig,
            mentionedCurrentUserUIConfig,
            true,
            mentionClickListener,
            enableMention
        );
        final SpannableStringBuilder builder;
        if (enableMarkdown) {
            builder = new SpannableStringBuilder(MarkdownExtensionsKt.applyMarkdown(text, Arrays.asList(MarkdownType.BOLD, MarkdownType.LINK), url -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(intent);
                return Unit.INSTANCE;
            }));
        } else {
            builder = new SpannableStringBuilder(text);
        }

        if (message.getUpdatedAt() > 0L) {
            final String edited = textView.getResources().getString(R.string.sb_text_channel_message_badge_edited);
            final Spannable editedString = new SpannableString(edited);
            if (uiConfig != null) {
                final TextUIConfig editedTextMarkUIConfig = isMine ? uiConfig.getMyEditedTextMarkUIConfig() : uiConfig.getOtherEditedTextMarkUIConfig();
                editedTextMarkUIConfig.bind(context, editedString, 0, editedString.length());
            }
            builder.append(editedString);
        }
        textView.setText(builder);
    }

    @NonNull
    public static CharSequence getDisplayableText(
        @NonNull Context context,
        @NonNull BaseMessage message,
        @Nullable MessageUIConfig uiConfig,
        @Nullable TextUIConfig mentionedCurrentUserUIConfig,
        boolean mentionClickable,
        @Nullable OnItemClickListener<User> mentionClickListener,
        boolean enabledMention
    ) {
        final String mentionedText = message.getMentionedMessageTemplate();
        String displayedMessage = message.getMessage();
        final MessageDisplayData viewData = MessageDisplayDataManager.getOrNull(message);
        if (viewData instanceof UserMessageDisplayData) {
            displayedMessage = ((UserMessageDisplayData) viewData).getMessage();
        }
        final SpannableString text = new SpannableString(displayedMessage);
        if (uiConfig != null) {
            final TextUIConfig messageTextUIConfig = MessageUtils.isMine(message) ? uiConfig.getMyMessageTextUIConfig() : uiConfig.getOtherMessageTextUIConfig();
            messageTextUIConfig.bind(context, text, 0, text.length());
        }

        CharSequence displayText = text;
        if (enabledMention && !message.getMentionedUsers().isEmpty() && !TextUtils.isEmpty(mentionedText)) {
            final SpannableString mentionedSpannableString = new SpannableString(mentionedText);
            if (uiConfig != null) {
                final TextUIConfig messageTextUIConfig = MessageUtils.isMine(message) ? uiConfig.getMyMessageTextUIConfig() : uiConfig.getOtherMessageTextUIConfig();
                messageTextUIConfig.bind(context, mentionedSpannableString, 0, mentionedSpannableString.length());
            }
            final Matcher matcher = MENTION.matcher(mentionedSpannableString);
            final List<String> sources = new ArrayList<>();
            final List<CharSequence> destinations = new ArrayList<>();
            while (matcher.find()) {
                if (matcher.groupCount() < 2) break;
                Logger.d("_____ matched group[0] = %s, group[1] = %s, start=%d, end=%d, count=%d", matcher.group(0), matcher.group(1), matcher.start(), matcher.end(), matcher.groupCount());

                final String mentionedUserId = matcher.group(1);
                if (mentionedUserId != null) {
                    final User mentionedUser = getMentionedUser(message, mentionedUserId);
                    if (mentionedUser != null) {
                        final boolean isMine = MessageUtils.isMine(message);
                        final boolean isMentionedCurrentUser = MessageUtils.isMine(mentionedUserId);
                        final String trigger = SendbirdUIKit.getUserMentionConfig().getTrigger();
                        final SpannableString spannable;
                        if (uiConfig != null) {
                            final TextUIConfig config = isMine ? uiConfig.getMyMentionUIConfig() : uiConfig.getOtherMentionUIConfig();
                            final String nickname = UserUtils.getDisplayName(context, mentionedUser);
                            final MentionSpan mentionSpan = new MentionSpan(context, trigger, nickname, mentionedUser, config, isMentionedCurrentUser ? mentionedCurrentUserUIConfig : null);
                            spannable = new SpannableString(mentionSpan.getDisplayText());
                            spannable.setSpan(mentionSpan, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else {
                            spannable = new SpannableString(trigger + mentionedUser.getNickname());
                        }
                        if (mentionClickable) {
                            spannable.setSpan(new ClickableSpan() {
                                @Override
                                public void onClick(@NonNull View widget) {
                                    if (mentionClickListener != null) {
                                        mentionClickListener.onItemClick(widget, 0, mentionedUser);
                                    }
                                }

                                @Override
                                public void updateDrawState(@NonNull TextPaint paint) {
                                    paint.setUnderlineText(false);
                                }
                            }, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        destinations.add(spannable);
                        sources.add(matcher.group(0));
                    }
                }
            }
            int arraySize = sources.size();
            displayText = TextUtils.replace(mentionedSpannableString, sources.toArray(new String[arraySize]), destinations.toArray(new CharSequence[arraySize]));
        }
        return displayText;
    }

    @Nullable
    private static User getMentionedUser(@NonNull BaseMessage message, @NonNull String targetUserId) {
        final List<User> mentionedUserList = message.getMentionedUsers();
        for (User user : mentionedUserList) {
            if (user.getUserId().equals(targetUserId)) {
                return user;
            }
        }
        return null;
    }

    public static void drawOgtag(@NonNull ViewGroup parent, @Nullable OGMetaData ogMetaData) {
        if (ogMetaData == null) {
            return;
        }

        parent.removeAllViews();
        OgtagView ogtagView = OgtagView.inflate(parent.getContext(), parent);
        ogtagView.drawOgtag(ogMetaData);
        parent.setOnClickListener(v -> {
            if (ogMetaData.getUrl() == null) {
                return;
            }

            Intent intent = IntentUtils.getWebViewerIntent(ogMetaData.getUrl());
            try {
                ogtagView.getContext().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Logger.e(e);
            }
        });
    }

    public static void drawReactionEnabled(@NonNull EmojiReactionListView view, @NonNull BaseChannel channel, @NonNull ChannelConfig channelConfig) {
        boolean canSendReaction = ChannelConfig.canSendReactions(channelConfig, channel);
        view.setClickable(canSendReaction);
        if (view.useMoreButton() != canSendReaction) {
            view.setUseMoreButton(canSendReaction);
            view.refresh();
        }
    }

    public static void drawNickname(
        @NonNull TextView tvNickname,
        @Nullable BaseMessage message,
        @Nullable MessageUIConfig uiConfig,
        boolean isOperator
    ) {
        if (message == null) {
            return;
        }

        final Sender sender = message.getSender();
        final Spannable nickname = new SpannableString(UserUtils.getDisplayName(tvNickname.getContext(), sender));
        if (uiConfig != null) {
            final boolean isMine = MessageUtils.isMine(message);
            final TextUIConfig textUIConfig = isOperator ? uiConfig.getOperatorNicknameTextUIConfig() : (isMine ? uiConfig.getMyNicknameTextUIConfig() : uiConfig.getOtherNicknameTextUIConfig());
            textUIConfig.bind(tvNickname.getContext(), nickname, 0, nickname.length());
        }

        tvNickname.setText(nickname);
    }

    public static void drawNotificationProfile(@NonNull ImageView ivProfile, @Nullable BaseMessage message) {
        int iconTint = SendbirdUIKit.isDarkMode() ? R.color.onlight_text_high_emphasis : R.color.ondark_text_high_emphasis;
        int backgroundTint = R.color.background_300;
        int inset = ivProfile.getContext().getResources().getDimensionPixelSize(R.dimen.sb_size_6);
        final Drawable profile = DrawableUtils.createOvalIconWithInset(ivProfile.getContext(), backgroundTint, R.drawable.icon_channels, iconTint, inset);
        ivProfile.setImageDrawable(profile);
    }

    public static void drawProfile(@NonNull ImageView ivProfile, @Nullable BaseMessage message) {
        if (message == null) {
            return;
        }
        Sender sender = message.getSender();

        String url = "";
        String plainUrl = "";
        if (sender != null && !TextUtils.isEmpty(sender.getProfileUrl())) {
            url = sender.getProfileUrl();
            plainUrl = sender.getPlainProfileImageUrl();
        }

        drawProfile(ivProfile, url, plainUrl);
    }

    public static void drawProfile(@NonNull ImageView ivProfile, @Nullable String url, @Nullable String plainUrl) {
        int iconTint = SendbirdUIKit.isDarkMode() ? R.color.onlight_text_high_emphasis : R.color.ondark_text_high_emphasis;
        int backgroundTint = R.color.background_300;
        Drawable errorDrawable = DrawableUtils.createOvalIcon(ivProfile.getContext(), backgroundTint, R.drawable.icon_user, iconTint);

        if (url == null || plainUrl == null) return;
        GlideCachedUrlLoader.load(Glide.with(ivProfile.getContext()), url, String.valueOf(plainUrl.hashCode())).diskCacheStrategy(DiskCacheStrategy.ALL).error(errorDrawable).apply(RequestOptions.circleCropTransform()).into(ivProfile);
    }

    public static void drawThumbnail(@NonNull RoundCornerView view, @NonNull FileMessage message) {
        drawThumbnail(
            view,
            message.getRequestId(),
            getUrl(message),
            message.getPlainUrl(),
            message.getType(),
            message.getThumbnails(),
            null,
            R.dimen.sb_size_48
        );
    }

    public static void drawQuotedMessageThumbnail(@NonNull RoundCornerView view, @NonNull FileMessage message, @Nullable RequestListener<Drawable> requestListener) {
        drawThumbnail(
            view,
            message.getRequestId(),
            getUrl(message),
            message.getPlainUrl(),
            message.getType(),
            message.getThumbnails(),
            requestListener,
            R.dimen.sb_size_24
        );
    }

    private static String getUrl(@NonNull FileMessage message) {
        String url = message.getUrl();
        if (TextUtils.isEmpty(url) && message.getMessageCreateParams() != null && message.getMessageCreateParams().getFile() != null) {
            url = message.getMessageCreateParams().getFile().getAbsolutePath();
        }

        return url;
    }

    public static void drawThumbnail(
        @NonNull RoundCornerView view,
        @NonNull String requestId,
        @NonNull String url,
        @NonNull String plainUrl,
        @NonNull String fileType,
        @NonNull List<Thumbnail> thumbnails,
        @Nullable RequestListener<Drawable> requestListener,
        @DimenRes int iconSize
    ) {
        Context context = view.getContext();
        RequestOptions options = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
        RequestBuilder<Drawable> builder = Glide.with(context).asDrawable().apply(options);

        Pair<Integer, Integer> defaultResizingSize = SendbirdUIKit.getResizingSize();
        int width = defaultResizingSize.first / 2;
        int height = defaultResizingSize.second / 2;
        FileInfo fileInfo = PendingMessageRepository.getInstance().getFileInfo(requestId);
        if (fileInfo != null) {
            width = fileInfo.getThumbnailWidth();
            height = fileInfo.getThumbnailHeight();
            builder = builder.override(width, height);
            if (!TextUtils.isEmpty(fileInfo.getThumbnailPath())) {
                url = fileInfo.getThumbnailPath();
            }
        } else {
            Thumbnail thumbnail = null;
            if (thumbnails.size() > 0) {
                thumbnail = thumbnails.get(0);
            }
            if (thumbnail != null && !TextUtils.isEmpty(thumbnail.getUrl())) {
                Logger.dev("++ thumbnail width : %s, thumbnail height : %s", thumbnail.getRealWidth(), thumbnail.getRealHeight());
                width = Math.max(MINIMUM_THUMBNAIL_WIDTH, thumbnail.getRealWidth());
                height = Math.max(MINIMUM_THUMBNAIL_HEIGHT, thumbnail.getRealHeight());
                url = thumbnail.getUrl();
                builder = builder.override(width, height);
            } else {
                final int size = Math.min(Math.max(MINIMUM_THUMBNAIL_WIDTH, width), Math.max(MINIMUM_THUMBNAIL_HEIGHT, height));
                builder = builder.override(size);
            }
        }

        if (fileType.toLowerCase().contains(StringSet.image) && !fileType.toLowerCase().contains(StringSet.gif)) {
            view.getContent().setScaleType(ImageView.ScaleType.CENTER);
            int thumbnailIconTint = SendbirdUIKit.isDarkMode() ? R.color.ondark_text_mid_emphasis : R.color.onlight_text_mid_emphasis;
            builder = builder.placeholder(DrawableUtils.setTintList(ImageUtils.resize(context.getResources(), AppCompatResources.getDrawable(context, R.drawable.icon_photo), iconSize, iconSize), AppCompatResources.getColorStateList(context, thumbnailIconTint))).error(DrawableUtils.setTintList(ImageUtils.resize(context.getResources(), AppCompatResources.getDrawable(context, R.drawable.icon_thumbnail_none), iconSize, iconSize), AppCompatResources.getColorStateList(context, thumbnailIconTint)));
        }

        final String cacheKey = generateThumbnailCacheKey(requestId, plainUrl);
        GlideCachedUrlLoader.load(builder, url, cacheKey).centerCrop().listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                if (requestListener != null) {
                    requestListener.onLoadFailed(e, model, target, isFirstResource);
                }
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                view.getContent().setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (requestListener != null) {
                    requestListener.onResourceReady(resource, model, target, dataSource, isFirstResource);
                }
                return false;
            }
        }).into(view.getContent());
    }

    private static String generateThumbnailCacheKey(@NonNull String requestId, @NonNull String plainUrl) {
        if (TextUtils.isNotEmpty(requestId)) {
            return "thumbnail_" + requestId;
        }
        return String.valueOf(plainUrl.hashCode());
    }

    public static void drawThumbnailIcon(@NonNull ImageView imageView, @NonNull FileMessage message) {
        drawThumbnailIcon(imageView, message.getType());
    }

    public static void drawThumbnailIcon(@NonNull ImageView imageView, @NonNull String fileType) {
        Context context = imageView.getContext();
        int backgroundTint = R.color.ondark_text_high_emphasis;
        int iconTint = R.color.onlight_text_mid_emphasis;
        if (fileType.toLowerCase().contains(StringSet.gif)) {
            imageView.setImageDrawable(DrawableUtils.createOvalIcon(context, backgroundTint, R.drawable.icon_gif, iconTint));
        } else if (fileType.toLowerCase().contains(StringSet.video)) {
            imageView.setImageDrawable(DrawableUtils.createOvalIcon(context, backgroundTint, R.drawable.icon_play, iconTint));
        } else {
            imageView.setImageResource(android.R.color.transparent);
        }
    }

    public static void drawFileIcon(@NonNull ImageView imageView, @NonNull FileMessage fileMessage) {
        drawFileIcon(imageView, fileMessage.getType());
    }

    public static void drawFileIcon(@NonNull ImageView imageView, @NonNull String fileType) {
        Context context = imageView.getContext();
        int backgroundTint = SendbirdUIKit.isDarkMode() ? R.color.background_600 : R.color.background_50;
        int iconTint = SendbirdUIKit.getDefaultThemeMode().getPrimaryTintResId();
        int inset = (int) context.getResources().getDimension(R.dimen.sb_size_4);
        Drawable background = DrawableUtils.setTintList(context, R.drawable.sb_rounded_rectangle_light_corner_10, backgroundTint);
        if (fileType.toLowerCase().startsWith(StringSet.audio)) {
            Drawable icon = DrawableUtils.setTintList(imageView.getContext(), R.drawable.icon_file_audio, iconTint);
            imageView.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset));
        } else {
            Drawable icon = DrawableUtils.setTintList(imageView.getContext(), R.drawable.icon_file_document, iconTint);
            imageView.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset));
        }
    }

    public static void drawFileMessageIconToReply(@NonNull ImageView imageView, @NonNull FileMessage fileMessage) {
        drawFileMessageIconToReply(imageView, fileMessage.getType());
    }

    public static void drawFileMessageIconToReply(@NonNull ImageView imageView, @NonNull String fileType) {
        Context context = imageView.getContext();
        int backgroundTint = SendbirdUIKit.isDarkMode() ? R.color.background_500 : R.color.background_100;
        int iconTint = SendbirdUIKit.isDarkMode() ? R.color.ondark_text_mid_emphasis : R.color.onlight_text_mid_emphasis;
        int inset = (int) context.getResources().getDimension(R.dimen.sb_size_8);
        Drawable background = DrawableUtils.setTintList(context, R.drawable.sb_rounded_rectangle_light_corner_10, backgroundTint);

        if (fileType.toLowerCase().startsWith(StringSet.audio)) {
            Drawable icon = DrawableUtils.setTintList(imageView.getContext(), R.drawable.icon_file_audio, iconTint);
            imageView.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset));
        } else if ((fileType.startsWith(StringSet.image) && !fileType.contains(StringSet.svg)) || fileType.toLowerCase().contains(StringSet.gif) || fileType.toLowerCase().contains(StringSet.video)) {
            imageView.setImageResource(android.R.color.transparent);
        } else {
            Drawable icon = DrawableUtils.setTintList(imageView.getContext(), R.drawable.icon_file_document, iconTint);
            imageView.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset));
        }
    }

    public static void drawQuotedMessage(
        @NonNull BaseQuotedMessageView replyPanel,
        @NonNull GroupChannel channel,
        @NonNull BaseMessage message,
        @Nullable TextUIConfig uiConfig,
        @NonNull MessageListUIParams params) {
        final boolean hasParentMessage = MessageUtils.hasParentMessage(message);
        replyPanel.setVisibility(hasParentMessage ? View.VISIBLE : View.GONE);
        replyPanel.drawQuotedMessage(channel, message, uiConfig, params);
    }

    public static void drawSentAt(@NonNull TextView tvSentAt, @Nullable BaseMessage message, @Nullable MessageUIConfig uiConfig) {
        if (message == null) {
            return;
        }

        final Spannable sentAt = new SpannableString(DateUtils.formatTime(tvSentAt.getContext(), message.getCreatedAt()));
        if (uiConfig != null) {
            final boolean isMine = MessageUtils.isMine(message);
            final TextUIConfig textUIConfig = isMine ? uiConfig.getMySentAtTextUIConfig() : uiConfig.getOtherSentAtTextUIConfig();
            textUIConfig.bind(tvSentAt.getContext(), sentAt, 0, sentAt.length());
        }
        tvSentAt.setText(sentAt);
    }

    public static void drawParentMessageSentAt(@NonNull TextView tvSentAt, @Nullable BaseMessage message, @Nullable MessageUIConfig uiConfig) {
        if (message == null) {
            return;
        }

        final Context context = tvSentAt.getContext();
        final long createdAt = message.getCreatedAt();
        final String sentAtTime = DateUtils.formatTime(context, createdAt);
        final String sentAtDate = DateUtils.isThisYear(createdAt) ? DateUtils.formatDate2(createdAt) : DateUtils.formatDate4(createdAt);
        final Spannable sentAt = new SpannableString(sentAtDate + " " + sentAtTime);
        if (uiConfig != null) {
            final boolean isMine = MessageUtils.isMine(message);
            final TextUIConfig textUIConfig = isMine ? uiConfig.getMySentAtTextUIConfig() : uiConfig.getOtherSentAtTextUIConfig();
            textUIConfig.bind(context, sentAt, 0, sentAt.length());
        }
        tvSentAt.setText(sentAt);
    }

    public static void drawFilename(@NonNull TextView tvFilename, @Nullable FileMessage message, @Nullable MessageUIConfig uiConfig) {
        if (message == null) {
            return;
        }

        drawFilename(tvFilename, message.getName(), MessageUtils.isMine(message), uiConfig);
    }

    public static void drawFilename(@NonNull TextView tvFilename, @NonNull String fileName, boolean isMine, @Nullable MessageUIConfig uiConfig) {
        final Spannable filename = new SpannableString(fileName);
        if (uiConfig != null) {
            final TextUIConfig textUIConfig = isMine ? uiConfig.getMyMessageTextUIConfig() : uiConfig.getOtherMessageTextUIConfig();
            textUIConfig.bind(tvFilename.getContext(), filename, 0, filename.length());
        }

        tvFilename.setText(filename);
    }

    public static void drawThreadInfo(@NonNull ThreadInfoView threadInfoView, @NonNull BaseMessage message, @NonNull MessageListUIParams messageListUIParams) {
        if (message instanceof CustomizableMessage) return;
        if (messageListUIParams.getChannelConfig().getReplyType() != ReplyType.THREAD) {
            threadInfoView.setVisibility(View.GONE);
            return;
        }
        threadInfoView.drawThreadInfo(message.getThreadInfo());
    }

    public static void drawVoiceMessage(@NonNull VoiceMessageView voiceMessageView, @NonNull FileMessage message) {
        voiceMessageView.drawVoiceMessage(message);
    }

    public static void drawTimeline(@NonNull TextView timelineView, int milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long sec = TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(minutes);
        timelineView.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, sec));
    }

    public static void drawVoicePlayerProgress(@NonNull VoiceProgressView progressView, int milliseconds, int duration) {
        if (duration == 0) return;
        int progress = milliseconds * 1000 / duration;
        int prevProgress = progressView.getProgress();

        if (prevProgress <= progress) {
            progressView.drawProgressWithAnimation(progress);
        } else {
            progressView.drawProgress(progress);
        }
    }
}
