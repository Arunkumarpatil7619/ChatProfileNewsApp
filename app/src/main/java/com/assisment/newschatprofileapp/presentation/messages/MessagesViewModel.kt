package com.assisment.newschatprofileapp.presentation.messages





import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assisment.newschatprofileapp.domain.model.Message
import com.assisment.newschatprofileapp.domain.usecase.GetMessagesUseCase
import com.assisment.newschatprofileapp.domain.usecase.SendImageMessageUseCase
import com.assisment.newschatprofileapp.domain.usecase.SendTextMessageUseCase
import com.assisment.newschatprofileapp.domain.usecase.SimulateReceivedMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendTextMessageUseCase: SendTextMessageUseCase,
    private val sendImageMessageUseCase: SendImageMessageUseCase,
    private val simulateReceivedMessageUseCase: SimulateReceivedMessageUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MessagesState())
    val state: StateFlow<MessagesState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    init {
        loadMessages()
    }

    fun onEvent(event: MessagesEvent) {
        when (event) {
            is MessagesEvent.SendText -> {
                sendTextMessage(event.text)
            }
            is MessagesEvent.SendImage -> {
                sendImageMessage(event.imageUri)
            }
            MessagesEvent.SimulateReceived -> {
                simulateReceivedMessage()
            }
            MessagesEvent.ClearMessages -> {
                clearMessages()
            }
            is MessagesEvent.UpdateTypingText -> {
                _uiState.value = _uiState.value.copy(
                    typingText = event.text
                )
            }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            getMessagesUseCase()
                .onEach { messages ->
                    _state.value = _state.value.copy(
                        messages = messages,
                        groupedMessages = groupMessagesByDate(messages)
                    )
                }
                .launchIn(viewModelScope)
        }
    }

    private fun sendTextMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            sendTextMessageUseCase(text)
            _uiState.value = _uiState.value.copy(
                typingText = ""
            )
        }
    }

    private fun sendImageMessage(imageUri: String) {
        viewModelScope.launch {
            sendImageMessageUseCase(imageUri)
        }
    }

    private fun simulateReceivedMessage() {
        viewModelScope.launch {
            simulateReceivedMessageUseCase()
        }
    }

    private fun clearMessages() {
        // Implementation depends on repository
    }

    private fun groupMessagesByDate(messages: List<Message>): Map<String, List<Message>> {
        return messages.groupBy { it.dateGroup }
    }
}

sealed class MessagesEvent {
    data class SendText(val text: String) : MessagesEvent()
    data class SendImage(val imageUri: String) : MessagesEvent()
    object SimulateReceived : MessagesEvent()
    object ClearMessages : MessagesEvent()
    data class UpdateTypingText(val text: String) : MessagesEvent()
}

data class MessagesState(
    val messages: List<Message> = emptyList(),
    val groupedMessages: Map<String, List<Message>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class MessagesUiState(
    val typingText: String = "",
    val isSending: Boolean = false,
    val showImagePicker: Boolean = false
)