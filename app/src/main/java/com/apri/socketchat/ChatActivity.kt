package com.apri.socketchat

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.github.dhaval2404.imagepicker.ImagePicker
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.content_chat.recyclerView
import java.io.File


class ChatActivity : AppCompatActivity() {

    companion object {
        const val CHANNEL_ID = "channelId"
        const val NAME = "name"
    }

    private val database by lazy {
        Room.databaseBuilder(applicationContext, ChatDatabase::class.java, "database-chat").build()
    }
    private val socketHandler by lazy { ChatSocketHandler() }
    private val disposables = CompositeDisposable()
    private val adapter = ChatAdapter()

    private var channelId: Int = -1
    private var name: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        this.channelId = intent.extras?.getInt(CHANNEL_ID) ?: -1
        this.name = intent.getStringExtra(NAME) ?: ""
        if (channelId == -1) {
            finish()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.subtitle = "Your Name: ${this.name}"

        this.fetchChatHistory()
        this.setupRecyclerView()
        this.setupChatSocketHandler(channelId, name)

        sendButton.setOnClickListener {
            val text = etChat.text.toString()
            if (text.isNotEmpty()) {
                socketHandler.sendText(text)
                etChat.text?.clear()
            }
        }
        imageButton.setOnClickListener { this.openImagePicker() }
        recyclerView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                recyclerView.postDelayed({ scrollRecyclerToBottom() }, 100)
            }
        }
    }

    private fun openImagePicker() {
        ImagePicker.with(this)
            .compress(500)
            .start { resultCode, data ->
                if (resultCode == Activity.RESULT_OK) {
                    val filePath = ImagePicker.getFilePath(data)
                    if (filePath != null && File(filePath).exists()) {
                        val bitmap = BitmapFactory.decodeFile(filePath)
                        this.socketHandler.sendImage(bitmap)
                    }
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(
                        applicationContext,
                        ImagePicker.getError(data),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    private fun setStatus(status: String) {
        supportActionBar?.title = "Channel: $channelId ($status)"
    }

    private fun setupChatSocketHandler(channelId: Int, name: String) {
        this.setStatus("Connecting")
        this.socketHandler.connectInChannel(channelId, name)
        this.disposables.add(
            this.socketHandler.onMessageReceived.subscribe {
                runOnUiThread {
                    database.chatDao().insert(it).subscribeOn(Schedulers.computation()).subscribe()
                    this.adapter.addChat(it)
                    scrollRecyclerToBottom()
                }
            }
        )

        this.disposables.add(
            this.socketHandler.connectionStatus.subscribe {
                runOnUiThread { this.setStatus(it) }
            }
        )
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(baseContext)
        recyclerView.adapter = adapter
        this.disposables.add(
            this.adapter.actionClick.subscribe {
                this.socketHandler.sendText(it)
            }
        )
    }

    private fun fetchChatHistory() {
        this.disposables.add(
            database.chatDao().getAll(channelId)
                .subscribeOn(Schedulers.computation()).subscribe(Consumer {
                    runOnUiThread {
                        adapter.addChats(it)
                        scrollRecyclerToBottom()
                    }
                })
        )
    }

    private fun scrollRecyclerToBottom() {
        if (adapter.itemCount > 0) {
            recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
        }
    }

    override fun onDestroy() {
        this.disposables.dispose()
        this.socketHandler.webSocket.close(1000, "")
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            this.finish()
        }
        return true
    }
}
