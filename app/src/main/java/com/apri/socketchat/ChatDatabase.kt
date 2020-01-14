package com.apri.socketchat

import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Completable
import io.reactivex.Single
import java.lang.reflect.Type


@Database(entities = [Chat::class], version = 1)
@TypeConverters(ListStringConverter::class, ChatTypeConverter::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}

@Dao
interface ChatDao {

    @Query("Select * from chat where channelId == :channelId")
    fun getAll(channelId: Int): Single<List<Chat>>

    @Insert
    fun insert(chat: Chat): Completable
}

object ListStringConverter {
    @TypeConverter
    @JvmStatic
    fun fromString(value: String?): List<String> {
        val listType: Type = object : TypeToken<List<String?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun fromArrayList(list: List<String?>?): String {
        return Gson().toJson(list)
    }
}

object ChatTypeConverter {
    @TypeConverter
    @JvmStatic
    fun fromString(value: String?): ChatType {
        val listType: Type = object : TypeToken<ChatType>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun fromChatType(chatType: ChatType): String {
        return Gson().toJson(chatType)
    }
}