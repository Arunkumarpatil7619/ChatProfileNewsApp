package com.assisment.newschatprofileapp.data.local.entity





import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String? = null,
    val imageUri: String? = null,
    val isSentByMe: Boolean,
    val timestamp: Long,
    val dateGroup: String
)