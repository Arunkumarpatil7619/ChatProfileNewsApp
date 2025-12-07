package com.assisment.newschatprofileapp.data.local.dao



import androidx.room.*
import com.assisment.newschatprofileapp.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Insert
    suspend fun insert(message: MessageEntity)

    @Insert
    suspend fun insertAll(messages: List<MessageEntity>)

    @Query("DELETE FROM messages")
    suspend fun deleteAll()

    @Query("SELECT * FROM messages WHERE dateGroup = :date ORDER BY timestamp ASC")
    fun getMessagesByDate(date: String): Flow<List<MessageEntity>>

    @Query("SELECT DISTINCT dateGroup FROM messages ORDER BY dateGroup DESC")
    fun getMessageDates(): Flow<List<String>>
}