package ge.lbukhnikashvili.androidfinalproject.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import de.hdodenhof.circleimageview.CircleImageView
import ge.lbukhnikashvili.androidfinalproject.DataClasses.Conversation
import ge.lbukhnikashvili.androidfinalproject.DataClasses.ConversationInfo
import ge.lbukhnikashvili.androidfinalproject.DataClasses.Message
import ge.lbukhnikashvili.androidfinalproject.R

class MainPageAdapter(var conversationsInfo: MutableList<ConversationInfo>) :
    RecyclerView.Adapter<MainPageAdapter.ItemViewHolder>() {
        inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    onItemClick?.invoke(conversationsInfo[adapterPosition].toUid!!)
                }
            }
        }
    var onItemClick: ((String) -> Unit)? = null

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        var conversationInfo=conversationsInfo[position]
        holder.itemView.findViewById<TextView>(R.id.main_page_item_timetag).text = conversationInfo.timeTag
        holder.itemView.findViewById<TextView>(R.id.main_page_item_name).text = conversationInfo.toName
        holder.itemView.findViewById<TextView>(R.id.main_page_item_message).text = conversationInfo.message

        var imageUri=conversationInfo.profileUrl
        ImageLoader.getInstance().displayImage(imageUri, holder.itemView.findViewById<CircleImageView>(R.id.main_page_item_icon),
            DisplayImageOptions.createSimple(), null);
    }

    override fun getItemCount(): Int {
        if (conversationsInfo.isNullOrEmpty()) return 0
        return conversationsInfo.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.main_page_item, parent, false)
        return ItemViewHolder(view)
    }
}