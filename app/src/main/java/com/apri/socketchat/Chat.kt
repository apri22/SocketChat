package com.apri.socketchat

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Chat(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "type") val type: ChatType,
    @ColumnInfo(name = "actions") val actions: List<String> = listOf(),
    @ColumnInfo(name = "isMyChat") val isMyChat: Boolean = false,
    @ColumnInfo(name = "channelId") val channelId: Int? = null
) {
    constructor(
        name: String,
        content: String,
        type: ChatType,
        actions: List<String> = listOf(),
        isMyChat: Boolean = false
    ) : this(null, name, content, type, actions, isMyChat, null)
}

enum class ChatType {
    @SerializedName("text")
    text,
    @SerializedName("image")
    image,
    @SerializedName("action")
    action
}
