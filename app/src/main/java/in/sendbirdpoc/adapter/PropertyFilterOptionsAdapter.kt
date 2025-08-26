package `in`.sendbirdpoc.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import `in`.sendbirdpoc.databinding.ItemPropertyFilterOptionsBinding

class PropertyFilterOptionsAdapter(
    private var list: List<String>,
    private val onClickListener: OnItemClickListener
) : RecyclerView.Adapter<PropertyFilterOptionsAdapter.ViewHolder>() {

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

    class ViewHolder(var itemBinding: ItemPropertyFilterOptionsBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemPropertyFilterOptionsBinding =
            ItemPropertyFilterOptionsBinding.inflate(
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
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
