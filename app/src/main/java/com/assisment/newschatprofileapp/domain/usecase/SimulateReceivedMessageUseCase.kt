package com.assisment.newschatprofileapp.domain.usecase




import com.assisment.newschatprofileapp.data.repository.MessageRepository
import javax.inject.Inject

class SimulateReceivedMessageUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(text: String? = null) {
        repository.simulateReceivedMessage(text ?: getRandomMessage())
    }

    private fun getRandomMessage(): String {
        val messages = listOf(
            "Hello!",
            "How are you?",
            "This is a simulated message",
            "The app looks great!",
            "Thanks for building this!",
            "Can we schedule a meeting?",
            "Check out this cool feature!",
            "I'll send you the details",
            "Let me know when you're free",
            "Have a great day!"
        )
        return messages.random()
    }
}