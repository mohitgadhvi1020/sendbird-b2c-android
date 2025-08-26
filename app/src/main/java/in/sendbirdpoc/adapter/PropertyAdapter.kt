package `in`.sendbirdpoc.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import `in`.sendbirdpoc.databinding.ItemPropertyBinding
import `in`.sendbirdpoc.model.PropertyListResponse

class PropertyAdapter(
    private var list: List<PropertyListResponse.Property>,
    private val onClickListener: OnItemClickListener
) : RecyclerView.Adapter<PropertyAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemCLick(
            item: PropertyListResponse.Property,
            position: Int
        )

        fun onChatCLick(
            item: PropertyListResponse.Property,
            position: Int
        )
    }

    fun setData(listData: List<PropertyListResponse.Property>) {
        list = listData
        notifyDataSetChanged()
    }

    class ViewHolder(var itemBinding: ItemPropertyBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemPropertyBinding =
            ItemPropertyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ViewHolder, listPosition: Int) {

        val item = list[listPosition]
        val context = holder.itemBinding.root.context

        holder.itemBinding.apply {
            Glide.with(context).load(item.images[0])/*.placeholder(placeHolder)*/
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)

            llChat.setOnClickListener {
                onClickListener.onChatCLick(item, listPosition)
            }

            tvVerified.text = item.status
            tvRent.text = "${item.price} AED/year"
            tvSuperAgent.text = item.agent_first_name
            tvUpdatedTime.text = item.updated_at._seconds.toString()
            tvHouseName.text = item.title
            tvLocationText.text = item.location.address
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
