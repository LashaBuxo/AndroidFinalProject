package ge.lbukhnikashvili.androidfinalproject.Adapters

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import com.nostra13.universalimageloader.utils.DiskCacheUtils
import de.hdodenhof.circleimageview.CircleImageView
import ge.lbukhnikashvili.androidfinalproject.DataClasses.UserInfo
import ge.lbukhnikashvili.androidfinalproject.R


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
        var imageUri=users[position].image_url
        ImageLoader.getInstance().displayImage(imageUri, holder.itemView.findViewById<CircleImageView>(R.id.search_page_item_icon),
            DisplayImageOptions.createSimple(), null);
    }

    override fun getItemCount(): Int {
        return users.count()
    }
}