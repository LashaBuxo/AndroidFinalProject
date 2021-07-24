package ge.lbukhnikashvili.androidfinalproject.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ge.lbukhnikashvili.androidfinalproject.R

class DemoAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_TYPE_1) {
            ViewHolder1(
                LayoutInflater.from(parent.context).inflate(R.layout.conversation_page_item1, parent, false)
            )
        } else {
            ViewHolder2(
                LayoutInflater.from(parent.context).inflate(R.layout.conversation_page_item2, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 20
    }

    override fun getItemViewType(position: Int): Int {
        return if (position % 2 == 0) {
            ITEM_TYPE_1
        } else {
            ITEM_TYPE_2
        }
    }

    inner class ViewHolder1(view: View) : RecyclerView.ViewHolder(view) {

    }

    inner class ViewHolder2(view: View) : RecyclerView.ViewHolder(view) {

    }

    companion object {
        private const val ITEM_TYPE_1 = 1
        private const val ITEM_TYPE_2 = 2

    }
}