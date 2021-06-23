package com.example.labs.pages.lab7

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import com.example.labs.R
import java.text.SimpleDateFormat
import java.util.*

data class Msg(
    val id : Int,
    val msg : String,
    val cDate: String = getDate(),
    val cTime: String = getTime()
)
fun getDate(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

fun getTime(): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())


class ChatAdapter(private val chatHistory: MutableList<Msg>):  RecyclerView.Adapter<ChatAdapter.MsgViewHolder>()  {
    inner class MsgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var msgText: TextView = itemView.findViewById(R.id.messageText)
        var timeText: TextView = itemView.findViewById(R.id.timeText)
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatHistory[position].id == CHAT_ID.user) {
            CHAT_ID.user
        } else {
            CHAT_ID.bot
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MsgViewHolder {
        val layout  = if(viewType == CHAT_ID.user) R.layout.list_item_user_message else R.layout.list_item_bot_message

        val itemView = LayoutInflater.from(parent.context).inflate(layout, parent, false)

        return MsgViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MsgViewHolder, position: Int) {
        val item = chatHistory[position]

        holder.apply {
            msgText.text = item.msg
            timeText.text = item.cTime
        }


    }

    override fun getItemCount() = chatHistory.size


    companion object{
        var chatHistory = mutableListOf<Msg>()
        object CHAT_ID {
            const val user = 1
            const val bot = 2
        }
    }
}