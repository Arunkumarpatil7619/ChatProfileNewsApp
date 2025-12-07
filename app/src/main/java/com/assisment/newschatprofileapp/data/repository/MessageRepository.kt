package com.assisment.newschatprofileapp.data.repository


import com.assisment.newschatprofileapp.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getMessages(): Flow<List<Message>>

    suspend fun sendTextMessage(text: String)

    suspend fun sendImageMessage(imageUri: String)

    suspend fun simulateReceivedMessage(text: String)

    suspend fun clearMessages()

    suspend fun getMessagesByDate(date: String): List<Message>
}