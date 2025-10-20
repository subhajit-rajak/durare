package com.subhajitrajak.durare.ui.askAi

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.subhajitrajak.durare.R
import io.noties.markwon.Markwon

data class ChatMessage(
    val message: String,
    val isUser: Boolean
)

class AiChatAdapter(
    private val context: Context,
    private val chatList: MutableList<ChatMessage>,
    private val userProfileUrl: String?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_AI = 2
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
        return if (chatList[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_AI
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
        val chat = chatList[position]
        if (holder is UserViewHolder) {
            holder.textMessage.text = chat.message
            Glide.with(context).load(userProfileUrl).into(holder.userImage)
        } else if (holder is AiViewHolder) {
            val markwon = Markwon.create(context)
            val markdown = markwon.toMarkdown(chat.message)
            markwon.setParsedMarkdown(holder.textMessage, markdown)
        }
    }

    override fun getItemCount(): Int = chatList.size

    // add messages dynamically
    fun addMessage(message: ChatMessage) {
        chatList.add(message)
        notifyItemInserted(chatList.size - 1)
    }

    fun updateAiMessage(position: Int, markdown: CharSequence) {
        if (position in chatList.indices) {
            val chat = chatList[position]
            if (!chat.isUser) {
                chatList[position] = chat.copy(message = markdown.toString())
                notifyItemChanged(position)
            }
        }
    }

    // removes the last message
    fun removeLastMessage() {
        if (chatList.isNotEmpty()) {
            val position = chatList.size - 1
            chatList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}