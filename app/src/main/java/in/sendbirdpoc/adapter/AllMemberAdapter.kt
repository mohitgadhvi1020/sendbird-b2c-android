package `in`.sendbirdpoc.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sendbird.android.user.User
import `in`.sendbirdpoc.R
import `in`.sendbirdpoc.databinding.ItemMemberListBinding

class AllMemberAdapter(
    private var list: List<User>,
    private var selectedUserId: MutableList<String>,
    private val onClickListener: OnItemClickListener
) : RecyclerView.Adapter<AllMemberAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun isShowLimitReachedDialog(
            data: User,
            position: Int
        )
    }

    fun setData(listData: List<User>, userIds: MutableList<String>) {
        list = listData
        selectedUserId = userIds
        notifyDataSetChanged()
    }

    class ViewHolder(var itemBinding: ItemMemberListBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemMemberListBinding =
            ItemMemberListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

            chkSelection.isChecked = selectedUserId.contains(item.userId)
            chkSelection.isEnabled = !selectedUserId.contains(item.userId)

            chkSelection.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    if (selectedUserId.size > 4) {
                        chkSelection.isChecked = false
                        onClickListener.isShowLimitReachedDialog(item, listPosition)
                    } else {
                        selectedUserId.add(item.userId)
                    }
                } else {
                    if (selectedUserId.contains(item.userId)) {
                        selectedUserId.remove(item.userId)
                    }
                }
            }
            tvName.text = item.nickname.ifEmpty {
                item.userId
            }
        }
    }

    fun getSelectedUserIds(): List<String> {
        return selectedUserId
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
