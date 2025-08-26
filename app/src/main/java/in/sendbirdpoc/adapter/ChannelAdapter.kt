package `in`.sendbirdpoc.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.UserMessage
import `in`.sendbirdpoc.databinding.ItemChannelListBinding
import `in`.sendbirdpoc.model.PropertyListResponse

class ChannelAdapter(
    private var list: List<GroupChannel>,
    private val onClickListener: OnItemClickListener
) : RecyclerView.Adapter<ChannelAdapter.ViewHolder>() {

    private var listFilterable = list
    private var isLongPressActive = false
    private var longPressSelectedChannel = mutableListOf<String>()

    interface OnItemClickListener {
        fun onItemCLick(
            data: GroupChannel,
            isLongPressActive: Boolean,
            position: Int
        )

        fun onLongPressCLick(
            data: GroupChannel,
            position: Int
        )
    }

    fun setData(listData: List<GroupChannel>) {
        list = listData
        listFilterable = listData
        notifyDataSetChanged()
    }

    fun getData(): List<GroupChannel> {
        return list
    }

    class ViewHolder(var itemBinding: ItemChannelListBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemChannelListBinding =
            ItemChannelListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ViewHolder, listPosition: Int) {

        val item = listFilterable[listPosition]
        val context = holder.itemBinding.root.context
        val channelData = Gson().fromJson<PropertyListResponse.Property>(
            item.data,
            object : TypeToken<PropertyListResponse.Property>() {}.type
        )

        holder.itemBinding.apply {

            if (channelData.images.isNotEmpty()) {
                Glide.with(context).load(channelData.images[0])
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(ivChannel)
            } else {
                Glide.with(context).load(item.coverUrl)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(ivChannel)
            }

            holder.itemBinding.root.setOnClickListener {
                if (!isLongPressActive) {
                    /*if (longPressSelectedChannel.contains(item.url)) {
                        longPressSelectedChannel.remove(item.url)
                        holder.itemBinding.root.setBackgroundColor(Color.WHITE)
                    } else {
                        longPressSelectedChannel.add(item.url)
                        holder.itemBinding.root.setBackgroundColor("#D8D6E5".toColorInt())
                    }*/

                    onClickListener.onItemCLick(item, isLongPressActive, listPosition)
                }
            }

            holder.itemBinding.root.isEnabled = !isLongPressActive

            holder.itemBinding.root.setOnLongClickListener {
                if (!isLongPressActive) {
                    isLongPressActive = true
                    longPressSelectedChannel.add(item.url)
                    holder.itemBinding.root.setBackgroundColor("#D8D6E5".toColorInt())
                    onClickListener.onLongPressCLick(item, listPosition)
                }
                true
            }

            if (channelData.starred) {
                ivStar.visibility = View.VISIBLE
            } else {
                ivStar.visibility = View.GONE
            }

            if (item.unreadMessageCount > 0) {
                tvUnreadCount.text = item.unreadMessageCount.toString()
                tvUnreadCount.visibility = View.VISIBLE
            } else {
                tvUnreadCount.visibility = View.GONE
            }

            holder.itemBinding.root.setBackgroundColor(Color.WHITE)
            tvChannelName.text = item.name

            var bedrooms = 0
            channelData.features?.bedrooms?.let {
                bedrooms = it
            }

            tvChannelDescription.text =
                "${channelData.price} AED | Bed: ${bedrooms} | Bath: ${channelData.features?.bathrooms} | Area: ${channelData.features?.square_feet} sqft"

            if (item.lastMessage != null && item.lastMessage is UserMessage) {
                val senderName =
                    if (item.lastMessage?.sender?.nickname.isNullOrEmpty()) item.lastMessage?.sender?.userId else item.lastMessage?.sender?.nickname
                tvLastMessage.text = "${senderName}: ${item.lastMessage?.message}"
            }

            if (item.members.size == 1) {
                llImageStack.visibility = View.VISIBLE
                ivImage1.visibility = View.VISIBLE
                loadImage(context, item.members[0].profileUrl, ivImage1)
            } else if (item.members.size == 2) {
                llImageStack.visibility = View.VISIBLE
                ivImage1.visibility = View.VISIBLE
                ivImage2.visibility = View.VISIBLE
                loadImage(context, item.members[0].profileUrl, ivImage1)
                loadImage(context, item.members[1].profileUrl, ivImage2)
            } else if (item.members.size == 3) {
                llImageStack.visibility = View.VISIBLE
                ivImage1.visibility = View.VISIBLE
                ivImage2.visibility = View.VISIBLE
                ivImage3.visibility = View.VISIBLE
                loadImage(context, item.members[0].profileUrl, ivImage1)
                loadImage(context, item.members[1].profileUrl, ivImage2)
                loadImage(context, item.members[2].profileUrl, ivImage3)
            } else if (item.members.size == 4) {
                llImageStack.visibility = View.VISIBLE
                ivImage1.visibility = View.VISIBLE
                ivImage2.visibility = View.VISIBLE
                ivImage3.visibility = View.VISIBLE
                ivImage4.visibility = View.VISIBLE
                loadImage(context, item.members[0].profileUrl, ivImage1)
                loadImage(context, item.members[1].profileUrl, ivImage2)
                loadImage(context, item.members[2].profileUrl, ivImage3)
                loadImage(context, item.members[3].profileUrl, ivImage4)
            } else if (item.members.size == 5) {
                llImageStack.visibility = View.VISIBLE
                ivImage1.visibility = View.VISIBLE
                ivImage2.visibility = View.VISIBLE
                ivImage3.visibility = View.VISIBLE
                ivImage4.visibility = View.VISIBLE
                ivImage5.visibility = View.VISIBLE
                loadImage(context, item.members[0].profileUrl, ivImage1)
                loadImage(context, item.members[1].profileUrl, ivImage2)
                loadImage(context, item.members[2].profileUrl, ivImage3)
                loadImage(context, item.members[3].profileUrl, ivImage4)
                loadImage(context, item.members[4].profileUrl, ivImage5)
            } else {
                llImageStack.visibility = View.GONE
            }
        }
    }

    internal fun setLongPressActiveFlagFalse() {
        isLongPressActive = false
        longPressSelectedChannel = mutableListOf()
        notifyDataSetChanged()
    }

    internal fun getLongPressActiveFlag(): Boolean {
        return isLongPressActive
    }

    internal fun getLongPressSelectedChannel(): List<String> {
        return longPressSelectedChannel
    }

    private fun loadImage(context: Context, url: String, imageView: ImageView) {
        Glide.with(context).load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    override fun getItemCount(): Int {
        return listFilterable.size
    }

    fun filter(query: String) {
        val lowerQuery = query.lowercase()
        listFilterable = if (query.isEmpty()) {
            list.toMutableList()
        } else {
            list.filter {
                it.url.lowercase().contains(lowerQuery)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }
}
