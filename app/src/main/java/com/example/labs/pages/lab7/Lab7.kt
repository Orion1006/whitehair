package com.example.labs.pages.lab7

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.labs.R
import com.example.labs.pages.lab7.ChatAdapter.Companion.CHAT_ID
import com.example.labs.pages.lab7.ChatAdapter.Companion.chatHistory
import kotlinx.coroutines.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class Lab7 : Fragment() {

    lateinit var chatRV: RecyclerView

    lateinit var messageEdit : EditText

    lateinit var sendMessageBtn : ImageButton

    lateinit var connectBtn : Button
    private lateinit var webSocketClient: WebSocketClient

    val socketUri: URI? = URI(WEB_SOCKET_URL)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.lab7, container, false)

        chatRV = root.findViewById(R.id.chat_RV)

        chatRV.layoutManager = LinearLayoutManager(context).apply{
            stackFromEnd = true
        }
        chatRV.adapter = ChatAdapter(chatHistory)


        messageEdit = root.findViewById(R.id.message)
        sendMessageBtn = root.findViewById(R.id.send_message)
        sendMessageBtn.isEnabled = false

        connectBtn = root.findViewById(R.id.connect_to_socket)

        connectBtn.setOnClickListener{
            GlobalScope.launch(Dispatchers.Main){
                createWebSocketClient(socketUri)
                wsConnect()
                sendMessageBtn.isEnabled = true
            }
        }
        sendMessageBtn.setOnClickListener{
            val msgText = messageEdit.text.toString()
            messageEdit.text.clear()
            if(msgText==""){
                return@setOnClickListener
            }
            val msg = Msg(CHAT_ID.user,msgText)
            chatHistory.add(msg)
            updateUI()
            Log.d("User Msg", msg.toString())

            if (webSocketClient.connection.isClosed){
                GlobalScope.launch(Dispatchers.Main) {
                    wsReconnect()
                    webSocketClient.send(msg.msg)
                }
                return@setOnClickListener
            }

            webSocketClient.send(msg.msg)
        }


        return root
    }

    suspend fun wsConnect(): Boolean{
        return suspendCoroutine { continuation ->
            Thread(Runnable {
                    try {
                        continuation.resume(webSocketClient.connectBlocking())
                    } catch (ex: Exception) {
                        continuation.resumeWithException(ex)
                    } finally {

                    }

            }).start()
        }
    }
    suspend fun wsReconnect(): Boolean{
        return  suspendCoroutine { continuation ->
            Thread(Runnable {
                try {
                    continuation.resume(webSocketClient.reconnectBlocking())
                } catch (ex: Exception) {
                    continuation.resumeWithException(ex)
                } finally {

                }

            }).start()
        }
    }

    private fun createWebSocketClient(uri: URI?) {
        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d("data", handshakedata.toString())
            }

            override fun onMessage(message: String?) {
                if (message != null) {
                    val msg = Msg(CHAT_ID.bot,message)
                    chatHistory.add(msg)
                    updateUI()
                    Log.d("Bot Msg", msg.toString())
                }
            }


            override fun onClose(code: Int, reason: String?, remote: Boolean) {
              webSocketClient.close()
            }

            override fun onError(ex: Exception?) {
                Log.d("ERROR", ex.toString())
            }

        }
    }

    fun scrollToItem(){
        chatRV.smoothScrollToPosition(chatRV.adapter!!.itemCount-1)
    }


    private fun updateUI() {
        chatRV.adapter!!.notifyDataSetChanged()
        scrollToItem()
    }

    companion object {
        private const val WEB_SOCKET_URL = "wss://chat-bot-ildar.herokuapp.com/"
    }


}