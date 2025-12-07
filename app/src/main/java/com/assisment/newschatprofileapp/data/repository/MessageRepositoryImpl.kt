package com.assisment.newschatprofileapp.data.repository


import com.assisment.newschatprofileapp.data.local.dao.MessageDao
import com.assisment.newschatprofileapp.domain.model.Message
import com.assisment.newschatprofileapp.utils.toEntity
import com.assisment.newschatprofileapp.utils.toMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao
) : MessageRepository {

    private val inMemoryMessages = mutableListOf<Message>()

    override fun getMessages(): Flow<List<Message>> {
        return messageDao.getAllMessages()
            .map { entities ->
                val roomMessages = entities.map { it.toMessage() }
                (roomMessages + inMemoryMessages)
                    .distinctBy { it.timestamp }
                    .sortedBy { it.timestamp }
            }
    }

    override suspend fun sendTextMessage(text: String) {
        if (text.isBlank()) return

        val message = createMessage(text = text, isSentByMe = true)

        // Add to in-memory (REQUIRED)
        inMemoryMessages.add(message)

        // Save to Room (optional but preferred)
        messageDao.insert(message.toEntity())
    }

    override suspend fun sendImageMessage(imageUri: String) {
        val message = createMessage(imageUri = imageUri, isSentByMe = true)


        inMemoryMessages.add(message)

        // Save to Room (optional but preferred)
        messageDao.insert(message.toEntity())
    }

    override suspend fun simulateReceivedMessage(text: String) {
        val message = createMessage(text = text, isSentByMe = false)
        inMemoryMessages.add(message)
        messageDao.insert(message.toEntity())
    }

    override suspend fun clearMessages() {
        inMemoryMessages.clear()
        messageDao.deleteAll()
    }

    override suspend fun getMessagesByDate(date: String): List<Message> {
        return messageDao.getMessagesByDate(date)
            .map { entities -> entities.map { it.toMessage() } }
            .first()
    }

    private fun createMessage(
        text: String? = null,
        imageUri: String? = null,
        isSentByMe: Boolean
    ): Message {
        val timestamp = System.currentTimeMillis()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateGroup = dateFormatter.format(Date(timestamp))

        return Message(
            id = 0,
            text = text,
            imageUri = imageUri,
            isSentByMe = isSentByMe,
            timestamp = timestamp,
            dateGroup = dateGroup
        )
    }
}