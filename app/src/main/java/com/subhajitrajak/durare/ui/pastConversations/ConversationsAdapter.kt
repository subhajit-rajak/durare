package com.subhajitrajak.durare.ui.pastConversations

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.subhajitrajak.durare.R
import com.subhajitrajak.durare.data.local.ChatMessageEntity
import io.noties.markwon.Markwon

class ConversationsAdapter(
    private val context: Context,
    private val userProfileUrl: String?
) : ListAdapter<ChatMessageEntity, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_AI = 2

        private val DiffCallback = object : DiffUtil.ItemCallback<ChatMessageEntity>() {
            override fun areItemsTheSame(oldItem: ChatMessageEntity, newItem: ChatMessageEntity): Boolean {
                // If you have a unique ID field in your entity, use that.
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ChatMessageEntity, newItem: ChatMessageEntity): Boolean {
                return oldItem == newItem
            }
        }
    }

    // user
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textMessage: TextView = itemView.findViewById(R.id.text_message)
        val userImage: ImageView = itemView.findViewById(R.id.user_image)
    }

    // ai
    inner class AiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textMessage: TextView = itemView.findViewById(R.id.text_message)
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).role == "user") VIEW_TYPE_USER else VIEW_TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_USER) {
            val view = inflater.inflate(R.layout.item_chat_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_chat_ai, parent, false)
            AiViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chat = getItem(position)
        if (holder is UserViewHolder) {
            holder.textMessage.text = chat.content
            Glide.with(context).load(userProfileUrl).into(holder.userImage)
        } else if (holder is AiViewHolder) {
            val markwon = Markwon.create(context)
            val markdown = markwon.toMarkdown(chat.content)
            markwon.setParsedMarkdown(holder.textMessage, markdown)
        }
    }
}