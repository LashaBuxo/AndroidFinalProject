package ge.lbukhnikashvili.androidfinalproject.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ge.lbukhnikashvili.androidfinalproject.DataClasses.Conversation
import ge.lbukhnikashvili.androidfinalproject.R

class ConversationPageAdapter(var conversation: Conversation, var currentUid: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == Message_Left) {
            ViewHolder1(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.conversation_page_item_left, parent, false)
            )
        } else {
            ViewHolder2(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.conversation_page_item_right, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var messages = conversation.messages.toList()
        messages=messages.sortedWith(compareBy { it.first })

        var time = messages[position].second.date
        var parts = time?.split('_')
        var innerParts = parts?.get(1)?.split('-')

        holder.itemView.findViewById<TextView>(R.id.conversation_item_time).text =
            (innerParts?.get(0) ?: "xx") + ":" + (innerParts?.get(1) ?: "xx")
        holder.itemView.findViewById<TextView>(R.id.conversation_item_message).text =
            messages[position].second.message
    }

    override fun getItemCount(): Int {
        return conversation.messages.count()
    }

    override fun getItemViewType(position: Int): Int {
        var messages = conversation.messages.toList()
        messages=messages.sortedWith(compareBy { it.first })

        return if (messages[position].second.uid == currentUid) {
            Message_Right
        } else Message_Left
    }

    inner class ViewHolder1(view: View) : RecyclerView.ViewHolder(view) {

    }

    inner class ViewHolder2(view: View) : RecyclerView.ViewHolder(view) {

    }

    companion object {
        private const val Message_Left = 1
        private const val Message_Right = 2
    }
}