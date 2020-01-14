package com.apri.socketchat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton.setOnClickListener {
            this.toChatActivity()
        }
        etChannel.setText("2")
//        toChatActivity()
    }

    private fun toChatActivity() {
        if (!etChannel.text.isNullOrEmpty()) {
            val channelId = etChannel.text.toString().toInt()
            val name = etName.text.toString()
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra(ChatActivity.CHANNEL_ID, channelId)
            intent.putExtra(ChatActivity.NAME, if (name.isEmpty()) "(Anonymous)" else name)
            startActivity(intent)
        } else {
            Toast.makeText(applicationContext, "Channel ID is required", Toast.LENGTH_SHORT).show()
        }
    }
}
