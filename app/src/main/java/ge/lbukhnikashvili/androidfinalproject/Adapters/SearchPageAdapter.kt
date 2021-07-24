package ge.lbukhnikashvili.androidfinalproject.Adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import ge.lbukhnikashvili.androidfinalproject.R
import ge.lbukhnikashvili.androidfinalproject.DataClasses.UserInfo

class SearchPageAdapter(var users: List<UserInfo>) :
    RecyclerView.Adapter<SearchPageAdapter.ItemViewHolder>() {
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(users[adapterPosition].uid!!)
            }
        }
    }

    var onItemClick: ((String) -> Unit)? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_page_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.search_page_item_name).text = users[position].name
        holder.itemView.findViewById<TextView>(R.id.search_page_item_profession).text = users[position].profession
    }

    override fun getItemCount(): Int {
        return users.count()
    }
}