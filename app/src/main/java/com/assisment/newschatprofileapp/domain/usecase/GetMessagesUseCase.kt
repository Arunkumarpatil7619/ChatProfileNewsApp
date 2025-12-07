package com.assisment.newschatprofileapp.domain.usecase


import com.assisment.newschatprofileapp.data.repository.MessageRepository
import com.assisment.newschatprofileapp.domain.model.Message
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    operator fun invoke(): Flow<List<Message>> {
        return repository.getMessages()
    }
}