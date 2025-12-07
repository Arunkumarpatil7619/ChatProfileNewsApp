package com.assisment.newschatprofileapp.presentation.messages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.assisment.newschatprofileapp.domain.model.Message
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessagesScreen() {

    val viewModel: MessagesViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var messageText by remember { mutableStateOf(uiState.typingText) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Get theme colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer
    val onSecondaryContainerColor = MaterialTheme.colorScheme.onSecondaryContainer
    val outlineColor = MaterialTheme.colorScheme.outline

    // ⭐ Gallery Picker Launcher
    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                viewModel.onEvent(MessagesEvent.SendImage(uri.toString()))
            }
        }

    LaunchedEffect(uiState.typingText) {
        messageText = uiState.typingText
    }

    // Auto scroll on new message
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(state.messages.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {

        // ------------------ CHAT LIST ------------------
        LazyColumn(
            modifier = Modifier
                .weight(1f),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            state.groupedMessages.forEach { (date, dateMessages) ->

                item { DateHeader(date) }

                items(dateMessages) { msg ->
                    ChatBubble(msg)
                }
            }
        }

        // ------------------ INPUT AREA ------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceVariantColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {

                // Test Receive
                Button(
                    onClick = { viewModel.onEvent(MessagesEvent.SimulateReceived) },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = onPrimaryColor
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Receive Message")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    // ---------- ADD IMAGE BUTTON ----------
                    IconButton(
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(primaryContainerColor)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            tint = onPrimaryContainerColor,
                            contentDescription = "Add Image"
                        )
                    }

                    // TextField
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = {
                            messageText = it
                            viewModel.onEvent(MessagesEvent.UpdateTypingText(it))
                        },
                        placeholder = {
                            Text(
                                "Type a message...",
                                color = onSurfaceVariantColor
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = outlineColor,
                            focusedTextColor = onBackgroundColor,
                            unfocusedTextColor = onBackgroundColor,
                            cursorColor = primaryColor,
                            focusedLabelColor = primaryColor,
                            unfocusedLabelColor = onSurfaceVariantColor,
                            focusedPlaceholderColor = onSurfaceVariantColor,
                            unfocusedPlaceholderColor = onSurfaceVariantColor
                        )
                    )

                    // Send Button
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.onEvent(MessagesEvent.SendText(messageText))
                                messageText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            tint = onPrimaryColor,
                            contentDescription = "Send"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {

    val isMine = message.isSentByMe
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {

        Card(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isMine) 20.dp else 0.dp,
                bottomEnd = if (isMine) 0.dp else 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isMine)
                    primaryColor
                else
                    primaryContainerColor
            ),
            modifier = Modifier.widthIn(max = 260.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {

                message.text?.let {
                    Text(
                        text = it,
                        color = if (isMine) onPrimaryColor else onPrimaryContainerColor
                    )
                }

                message.imageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Image Message",
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun DateHeader(date: String) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = primaryContainerColor
            )
        ) {
            Text(
                text = formatDateForDisplay(date),
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
                color = onPrimaryContainerColor
            )
        }
    }
}

fun formatDateForDisplay(dateStr: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        val date = inputFormat.parse(dateStr)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateStr
    }
}