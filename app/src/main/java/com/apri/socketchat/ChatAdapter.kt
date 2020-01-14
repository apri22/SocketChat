package com.apri.socketchat

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.list_item_chat.view.*


class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val chatItems = mutableListOf<Chat>()
    val actionClick by lazy { PublishSubject.create<String>() }

    private val duration = 250L

    fun addChat(chat: Chat) {
        chatItems.add(chat)
        this.notifyItemInserted(chatItems.size - 1)
    }

    fun addChats(chats: List<Chat>) {
        chatItems.addAll(chats)
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_chat,
                null
            )
        )
    }

    override fun getItemCount(): Int {
        return chatItems.size
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatItems[position]
        holder.updateData(chat, position)

        this.setAnimation(holder.itemView)
    }

    private fun setAnimation(itemView: View) {
        itemView.alpha = 0f
        val animatorSet = AnimatorSet()
        val animator = ObjectAnimator.ofFloat(itemView, "alpha", 0f, 0.5f, 1.0f)
        ObjectAnimator.ofFloat(itemView, "alpha", 0f).start()
        animator.startDelay = duration
        animator.duration = duration
        animatorSet.play(animator)
        animator.start()
    }


    inner class ChatViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun updateData(chat: Chat, index: Int) {
            view.flowActions.visibility = View.GONE
            view.imageView.visibility = View.GONE
            view.textView.visibility = View.GONE
            view.cardView.visibility = View.GONE

            view.textName.text = chat.name
            when (chat.type) {
                ChatType.text -> {
                    view.textView.text = chat.content
                    view.cardView.visibility = View.VISIBLE
                    view.textView.visibility = View.VISIBLE
                }
                ChatType.image -> {
                    view.imageView.setImageBitmap(convertBase64ToImage(chat.content))
                    view.cardView.visibility = View.VISIBLE
                    view.imageView.visibility = View.VISIBLE
                }
                ChatType.action -> {
                    this.generateActions(chat, index)
                    view.cardView.visibility = View.GONE
                    view.flowActions.visibility = View.VISIBLE
                }
            }
            updateChatPosition(chat.isMyChat)
        }

        private fun updateChatPosition(isMyChat: Boolean) {
            val params = view.cardView.layoutParams as RelativeLayout.LayoutParams
            params.removeRule(if (isMyChat) RelativeLayout.ALIGN_PARENT_START else RelativeLayout.ALIGN_PARENT_END)
            params.addRule(
                if (isMyChat) RelativeLayout.ALIGN_PARENT_END else RelativeLayout.ALIGN_PARENT_START,
                RelativeLayout.TRUE
            )
            view.layoutParams = params
        }

        private fun generateActions(chat: Chat, index: Int) {
            view.flowActions.removeAllViews()

            val buttonTheme = R.style.Widget_MaterialComponents_Button_UnelevatedButton
            for (action in chat.actions) {
                val button = AppCompatButton(
                    ContextThemeWrapper(view.context, buttonTheme),
                    null,
                    buttonTheme
                )
                button.text = action
                button.textSize = 16f
                button.setOnClickListener {
                    actionClick.onNext(action)
                    chatItems.remove(chat)
                    notifyItemRemoved(index)
                }
                view.flowActions.addView(button)
            }
        }

        private fun convertBase64ToImage(base64: String): Bitmap? {
            val encoded = if (base64.contains("base64")) {
                val data = base64.split("base64").last()
                data
            } else {
                base64
            }
            val decodedString: ByteArray = Base64.decode(encoded, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

        }
    }
}
