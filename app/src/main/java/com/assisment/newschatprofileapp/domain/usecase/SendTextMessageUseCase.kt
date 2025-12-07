package com.assisment.newschatprofileapp.domain.usecase




import com.assisment.newschatprofileapp.data.repository.MessageRepository
import javax.inject.Inject

class SendTextMessageUseCase @Inject constructor(
    private val repository: MessageRepository
) {

    suspend operator fun invoke(text: String) {
        repository.sendTextMessage(text)
    }
}