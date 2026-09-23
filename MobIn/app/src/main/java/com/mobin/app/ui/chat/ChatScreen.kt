package com.mobin.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mobin.app.data.model.ChatConversation
import com.mobin.app.data.model.ChatMessage
import com.mobin.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    initialMessage: String = "",
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(chatId, initialMessage) {
        viewModel.loadChat(chatId, initialMessage)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    val conversation = uiState.conversation ?: ChatConversation(
        id = chatId,
        contactName = "Mary Ann Dasalo",
        propertyName = "Casa Urgello",
    )

    Scaffold(
        containerColor = Color(0xFFFDFDFD),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD9D9D9)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (!conversation.avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = conversation.avatarUrl,
                                    contentDescription = conversation.contactName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF7A7A7A),
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                text = conversation.contactName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1E1E1E),
                                ),
                            )
                            Text(
                                text = if (conversation.isOnline) "Online" else "Offline",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = if (conversation.isOnline) Color(0xFF2E7D32) else Color(0xFF7A7A7A),
                                ),
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFBF6B04),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.MoreHoriz,
                            contentDescription = "Options",
                            tint = Color(0xFF1E1E1E),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFDFDFD)),
            )
        },
        bottomBar = {
            Surface(
                color = White,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Attachment (+) button
                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Attach",
                            tint = Color(0xFF1E1E1E),
                            modifier = Modifier.size(26.dp),
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // Input Text Field
                    androidx.compose.foundation.text.BasicTextField(
                        value = uiState.inputText,
                        onValueChange = { viewModel.onInputChange(it) },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color = Color(0xFF1E1E1E),
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { viewModel.sendMessage() }),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF3F3F3), RoundedCornerShape(24.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (uiState.inputText.isEmpty()) {
                                    Text(
                                        text = "Type a message...",
                                        fontSize = 13.5.sp,
                                        color = Color(0xFF888888),
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )

                    Spacer(Modifier.width(10.dp))

                    // Send Button
                    IconButton(
                        onClick = { viewModel.sendMessage() },
                        modifier = Modifier
                            .size(42.dp)
                            .background(GoldenMarigold, CircleShape),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),
        ) {
            Spacer(Modifier.height(10.dp))

            // Date Separator (Today)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFE6E8EC),
                ) {
                    Text(
                        text = "Today",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF555555),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    )
                }
            }

            // Messages LazyList
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 12.dp),
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    ChatMessageBubble(message = message)
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier,
) {
    if (message.isFromCurrentUser) {
        // Outgoing Message (Right-aligned, Golden Yellow)
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 4.dp,
                ),
                color = Color(0xFFFBD67A), // Soft Golden Marigold
                modifier = Modifier.widthIn(max = 280.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = message.text,
                        fontSize = 13.5.sp,
                        color = Color(0xFF1E1E1E),
                        lineHeight = 19.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = message.timestamp,
                            fontSize = 10.5.sp,
                            color = Color(0xFF6B5808),
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Delivered",
                            tint = Color(0xFF6B5808),
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }
        }
    } else {
        // Incoming Message (Left-aligned, Soft Peach with Avatar)
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF333333)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(16.dp),
                )
            }

            Spacer(Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomEnd = 16.dp,
                    bottomStart = 4.dp,
                ),
                color = Color(0xFFEAA67C), // Peach Amber
                modifier = Modifier.widthIn(max = 260.dp),
            ) {
                Text(
                    text = message.text,
                    fontSize = 13.5.sp,
                    color = Color(0xFF1E1E1E),
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun ChatScreenPreview() {
    MobInTheme {
        ChatScreen(
            chatId = "1",
            onBack = {},
        )
    }
}
