package com.sendbird.uikit.consts

/**
 * Represents how to display replies in message list.
 *
 * @since 2.2.0
 */
enum class ReplyType(val value: String) {
    /**
     * Do not display replies in the message list.
     *
     * @since 2.2.0
     */
    NONE(StringSet.none),

    /**
     * Displays replies linearly in the message list.
     *
     * @since 2.2.0
     */
    QUOTE_REPLY(StringSet.quote_reply),

    /**
     * Displays replies to a parent message on a separate screen.
     *
     * @since 3.3.0
     */
    THREAD(StringSet.thread);

    companion object {
        /**
         * Convert to ReplyType that matches the given value.
         *
         * @param value the text value of the ReplyType.
         * @return the [ReplyType]
         * @since 3.6.0
         */
        @JvmStatic
        fun from(value: String): ReplyType {
            return values().firstOrNull { it.value == value } ?: NONE
        }
    }
}
