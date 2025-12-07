package com.assisment.newschatprofileapp.domain.usecase



import com.assisment.newschatprofileapp.data.repository.MessageRepository
import javax.inject.Inject

class SendImageMessageUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(imageUri: String) {
        repository.sendImageMessage(imageUri)
    }
}