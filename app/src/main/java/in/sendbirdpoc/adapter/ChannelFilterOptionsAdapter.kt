package `in`.sendbirdpoc.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import `in`.sendbirdpoc.R
import `in`.sendbirdpoc.databinding.ItemChannelFilterOptionsBinding

class ChannelFilterOptionsAdapter(
    private var list: List<String>,
    private val onClickListener: OnItemClickListener
) : RecyclerView.Adapter<ChannelFilterOptionsAdapter.ViewHolder>() {

    private var selectedPosition: Int =0

    interface OnItemClickListener {
        fun onItemCLick(
            data: String,
            position: Int
        )
    }

    fun setData(listData: List<String>) {
        list = listData
        notifyDataSetChanged()
    }

    class ViewHolder(var itemBinding: ItemChannelFilterOptionsBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemChannelFilterOptionsBinding =
            ItemChannelFilterOptionsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ViewHolder, listPosition: Int) {

        val item = list[listPosition]
        val context = holder.itemBinding.root.context

        holder.itemBinding.apply {
            holder.itemBinding.tvName.text = item
            holder.itemBinding.root.setOnClickListener {
                onClickListener.onItemCLick(item, listPosition)
            }

            if (selectedPosition == listPosition) {
                tvName.setBackgroundResource(R.drawable.bg_channel_filter_selected)
                tvName.setTextColor(context.getColor(R.color.white))
            } else {
                tvName.setBackgroundResource(R.drawable.bg_channel_filter_options)
                tvName.setTextColor(context.getColor(R.color.purple))
            }
        }
    }

    internal fun updateMenuBackground(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
