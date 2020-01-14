package com.apri.socketchat

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import io.reactivex.subjects.PublishSubject
import okhttp3.*
import java.io.ByteArrayOutputStream


class ChatSocketHandler : WebSocketListener() {

    private val accessToken =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjAyNzkyNGZlYzRlOWU4MGFhYjAxOTU2NmRlNTUyNzg3YTU0YTJiZmVmM2E0OTE0N2E4NjBmNDdkMTM2NGZiOTk3NmU2MDY3M2ZiY2FjODNjIn0"
    private val baseSocketURL = "wss://connect.websocket.in/v2/{channel_id}?token=$accessToken"
    private var socketURL = ""
    private var name: String = ""
    private var channel: Int = -1

    private val gson by lazy { Gson() }
    private val client: OkHttpClient by lazy { OkHttpClient() }
    lateinit var webSocket: WebSocket
    val onMessageReceived by lazy { PublishSubject.create<Chat>() }
    val connectionStatus by lazy { PublishSubject.create<String>() }


    fun connectInChannel(channel: Int, name: String) {
        this.name = name
        this.channel = channel
        this.socketURL = baseSocketURL.replace("{channel_id}", "$channel")

        val request = Request.Builder().url(socketURL).build()
        this.webSocket = client.newWebSocket(request, this)
        client.dispatcher.executorService.shutdown()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        try {
            val chat = gson.fromJson<Chat>(text, Chat::class.java)
            onMessageReceived.onNext(chat.copy(channelId = channel))
            Log.d("onMessage", text)
        } catch (ex: Exception) {
            Log.d("onMessage", text)
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        connectionStatus.onNext("Connected")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        connectionStatus.onNext("Disconnecting")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        connectionStatus.onNext("Disconnected")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        connectionStatus.onNext("Failure")
        Log.d("ChatSocketHandler", "onFailure: ${t.localizedMessage}")
    }

    fun sendText(text: String) {
        val chat = if (text.contains("actions:", ignoreCase = true)) {
            val actions = text.replace("actions:", "")
                .split(",")
                .map { it.trim() }
            Chat(name, "", ChatType.action, actions = actions)
        } else {
            Chat(name, text, ChatType.text)
        }
        this.sendChat(chat)
    }

    fun sendImage(bitmap: Bitmap) {
        val encoded = convertBitmapToBase64(bitmap)
        val chat = Chat(name, encoded, ChatType.image)
        this.sendChat(chat)
    }

    private fun sendChat(chat: Chat) {
        val json = gson.toJson(chat)
        webSocket.send(json)
        if (chat.type != ChatType.action) {
            onMessageReceived.onNext(chat.copy(channelId = channel, isMyChat = true))
        }

    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }
}