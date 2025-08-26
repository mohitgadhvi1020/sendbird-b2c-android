package `in`.sendbirdpoc.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sendbird.android.user.Member
import com.sendbird.android.user.User
import `in`.sendbirdpoc.R
import `in`.sendbirdpoc.databinding.ItemMemberGridBinding

class ExistingMemberAdapter(
    private var list: List<Member>,
    private var loggedInUserId: String? = null,
    private val onClickListener: OnItemClickListener
) : RecyclerView.Adapter<ExistingMemberAdapter.ViewHolder>() {

    var blockedUserIds = listOf<String>()

    interface OnItemClickListener {
        fun onMemberRemove(
            data: Member,
            position: Int
        )
    }

    fun setData(listData: List<Member>, blockedUserIds: List<String>) {
        list = listData
        this@ExistingMemberAdapter.blockedUserIds = blockedUserIds
        notifyDataSetChanged()
    }

    fun removeMember(userId: String) {
        val index = list.indexOfFirst { it.userId == userId }
        val tempList = list.toMutableList()
        tempList.removeAt(index)
        list = tempList
        notifyDataSetChanged()
    }

    class ViewHolder(var itemBinding: ItemMemberGridBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemMemberGridBinding =
            ItemMemberGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ViewHolder, listPosition: Int) {

        val item = list[listPosition]
        val context = holder.itemBinding.root.context

        holder.itemBinding.apply {
            Glide.with(context).load(item.profileUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(AppCompatResources.getDrawable(context, R.drawable.ic_user_avatar))
                .placeholder(AppCompatResources.getDrawable(context, R.drawable.ic_user_avatar))
                .into(ivProfilePic)

            if (item.connectionStatus == User.ConnectionStatus.ONLINE) {
                onlineIndicator.visibility = android.view.View.VISIBLE
            } else {
                onlineIndicator.visibility = android.view.View.GONE
            }

            tvName.text = item.nickname.ifEmpty { item.userId }

            if (blockedUserIds.contains(item.userId)) {
                ivRemove.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_block)
                )
            } else {
                ivRemove.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_remove)
                )
            }

            if (item.userId == loggedInUserId){
                ivRemove.isVisible = false
            }else{
                ivRemove.isVisible = true
            }
            ivRemove.setOnClickListener {
                onClickListener.onMemberRemove(item, listPosition)
            }
        }
    }

    fun updateBlockUserList(isBlock: Boolean, userId: String) {
        if (isBlock) {
            blockedUserIds.plus(userId).let {
                blockedUserIds = it
            }
        } else {
            blockedUserIds.minus(userId).let {
                blockedUserIds = it
            }
        }
        notifyDataSetChanged()
    }

    fun getList(): List<Member> {
        return list
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
