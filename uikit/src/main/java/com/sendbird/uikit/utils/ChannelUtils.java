package com.sendbird.uikit.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.user.Member;
import com.sendbird.android.user.Sender;
import com.sendbird.android.user.User;
import com.sendbird.uikit.test.R;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.internal.ui.channels.ChannelCoverView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChannelUtils {

    @NonNull
    public static String makeTitleText(@NonNull Context context, @NonNull GroupChannel channel) {
        String channelName = channel.getName();
        if (!TextUtils.isEmpty(channelName) && !channelName.equals(StringSet.GROUP_CHANNEL)) {
            return channelName;
        }

        List<Member> members = channel.getMembers();

        String result;
        if (members.size() < 2 || SendbirdChat.getCurrentUser() == null) {
            result = context.getString(R.string.sb_text_channel_list_title_no_members);
        } else if (members.size() == 2) {
            StringBuilder names = new StringBuilder();
            for (Member member : members) {
                if (member.getUserId().equals(SendbirdChat.getCurrentUser().getUserId())) {
                    continue;
                }

                String nickName = member.getNickname();
                names.append(", ")
                    .append(TextUtils.isEmpty(nickName) ?
                        context.getString(R.string.sb_text_channel_list_title_unknown) : nickName);
            }
            result = names.delete(0, 2).toString();
        } else {
            int count = 0;
            StringBuilder names = new StringBuilder();
            for (Member member : members) {
                if (member.getUserId().equals(SendbirdChat.getCurrentUser().getUserId())) {
                    continue;
                }

                count++;
                String nickName = member.getNickname();
                names.append(", ")
                    .append(TextUtils.isEmpty(nickName) ?
                        context.getString(R.string.sb_text_channel_list_title_unknown) : nickName);

                if (count >= 10) {
                    break;
                }
            }
            result = names.delete(0, 2).toString();
        }

        return result;
    }

    @NonNull
    public static String getLastMessage(@NonNull Context context, @NonNull GroupChannel channel) {
        // Bind last message text according to the type of message. Specifically, if
        // the last message is a File Message, there must be special formatting.

        final BaseMessage lastMessage = channel.getLastMessage();
        if (lastMessage == null) {
            return "";
        }

        if (lastMessage instanceof FileMessage) {
            Sender sender = lastMessage.getSender();
            return String.format(context.getString(R.string.sb_text_channel_list_last_message_file),
                sender != null ? sender.getNickname() : context.getString(R.string.sb_text_channel_list_last_message_file_unknown));
        }
        return lastMessage.getMessage();
    }

    public static void loadImage(@NonNull ChannelCoverView coverView, @Nullable String url) {
        List<String> urls = new ArrayList<>();
        if (TextUtils.isNotEmpty(url)) {
            urls.add(url);
        }
        coverView.loadImages(urls);
    }

    public static void loadChannelCover(@NonNull ChannelCoverView coverView, @NonNull BaseChannel channel) {
        if (channel instanceof GroupChannel) {
            GroupChannel groupChannel = (GroupChannel) channel;
            if (groupChannel.isBroadcast() && isDefaultChannelCover(channel)) {
                coverView.drawBroadcastChannelCover();
                return;
            }
            List<String> urls = makeProfileUrlsFromChannel(groupChannel);
            coverView.loadImages(urls);
        } else {
            coverView.loadImage(channel.getCoverUrl());
        }
    }

    @NonNull
    public static List<String> makeProfileUrlsFromChannel(@NonNull GroupChannel channel) {
        List<String> urls = new ArrayList<>();
        if (!isDefaultChannelCover(channel)) {
            urls.add(channel.getCoverUrl());
        } else {
            String myUserId = "";
            if (SendbirdChat.getCurrentUser() != null) {
                myUserId = SendbirdChat.getCurrentUser().getUserId();
            }
            List<Member> memberList = channel.getMembers();
            int index = 0;
            while (index < memberList.size() && urls.size() < 4) {
                Member member = memberList.get(index);
                ++index;
                if (member.getUserId().equals(myUserId)) {
                    continue;
                }
                urls.add(member.getProfileUrl());
            }
        }
        return urls;
    }

    private static boolean isDefaultChannelCover(@NonNull BaseChannel channel) {
        return TextUtils.isEmpty(channel.getCoverUrl()) || channel.getCoverUrl().contains(StringSet.DEFAULT_CHANNEL_COVER_URL);
    }

    @NonNull
    public static String makeTypingText(@NonNull Context context, @NonNull List<? extends User> typingUsers) {
        if (typingUsers.size() == 1) {
            return String.format(context.getString(R.string.sb_text_channel_typing_indicator_single),
                UserUtils.getDisplayName(context, typingUsers.get(0), false));
        } else if (typingUsers.size() == 2) {
            return String.format(context.getString(R.string.sb_text_channel_typing_indicator_double),
                UserUtils.getDisplayName(context, typingUsers.get(0), false), UserUtils.getDisplayName(context, typingUsers.get(1), false));
        } else {
            return context.getString(R.string.sb_text_channel_typing_indicator_multiple);
        }
    }

    public static boolean isChannelPushOff(@NonNull GroupChannel channel) {
        return channel.getMyPushTriggerOption() == GroupChannel.PushTriggerOption.OFF;
    }

    @NonNull
    public static CharSequence makeMemberCountText(int memberCount) {
        String text = String.valueOf(memberCount);
        if (memberCount > 1000) {
            text = String.format(Locale.US, "%.1fK", memberCount / 1000F);
        }
        return text;
    }

    @NonNull
    public static String makePushSettingStatusText(@NonNull Context context, @NonNull GroupChannel.PushTriggerOption triggerOption) {
        switch (triggerOption) {
            case OFF:
                return context.getString(R.string.sb_text_push_setting_off);
            case MENTION_ONLY:
                return context.getString(R.string.sb_text_push_setting_mentions_only);
            default:
                return context.getString(R.string.sb_text_push_setting_on);
        }
    }
}
