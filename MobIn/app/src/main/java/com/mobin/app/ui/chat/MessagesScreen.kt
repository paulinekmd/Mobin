package com.mobin.app.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mobin.app.data.model.ChatConversation
import com.mobin.app.ui.theme.*

@Composable
fun MessagesScreen(
    onOpenChat: (String) -> Unit,
    viewModel: MessagesViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    MessagesContent(
        uiState = uiState,
        onQueryChange = { viewModel.onQueryChange(it) },
        onTabSelect = { viewModel.selectTab(it) },
        onOpenChat = onOpenChat,
        onDeleteConversation = { viewModel.deleteConversation(it) },
    )
}

@Composable
internal fun MessagesContent(
    uiState: MessagesUiState,
    onQueryChange: (String) -> Unit,
    onTabSelect: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onDeleteConversation: (String) -> Unit = {},
) {
    var conversationToDelete by remember { mutableStateOf<ChatConversation?>(null) }

    Scaffold(
        containerColor = Color(0xFFFDFDFD),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 22.dp, vertical = 16.dp),
        ) {
            // ── Header Title ───────────────────────────────────────────────────
            Text(
                text = "Messages",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = ComfortaaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = Color(0xFF1E1E1E),
                ),
            )

            Spacer(Modifier.height(18.dp))

            // ── Search Input ───────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Search messages",
                        fontSize = 13.5.sp,
                        color = Color(0xFF888888),
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(20.dp),
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE2E4E8),
                    unfocusedBorderColor = Color(0xFFE2E4E8),
                    focusedContainerColor = Color(0xFFF6F6F6),
                    unfocusedContainerColor = Color(0xFFF6F6F6),
                    focusedTextColor = Color(0xFF1E1E1E),
                    unfocusedTextColor = Color(0xFF1E1E1E),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(18.dp))

            // ── Filter Pills Row ───────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf("All", "Unread").forEach { tab ->
                    val isSelected = uiState.selectedTab.equals(tab, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onTabSelect(tab) },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) Color(0xFFD9D9D9) else White,
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFFD9D9D9) else Color(0xFFE0E0E0)),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp),
                        ) {
                            Text(
                                text = tab,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF1E1E1E) else Color(0xFF555555),
                                ),
                            )
                            if (tab == "Unread" && uiState.allConversations.any { it.unreadCount > 0 }) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(GoldenMarigold, CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Conversations Feed ─────────────────────────────────────────────
            if (uiState.filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = Color(0xFFBDBDBD),
                            modifier = Modifier.size(44.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (uiState.selectedTab.equals("Unread", ignoreCase = true)) {
                                "No unread messages"
                            } else if (uiState.query.isNotBlank()) {
                                "No messages matching \"${uiState.query}\""
                            } else {
                                "No messages found"
                            },
                            fontSize = 14.sp,
                            color = Color(0xFF7A7A7A),
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    items(uiState.filteredConversations, key = { it.id }) { conversation ->
                        SwipeableConversationRow(
                            conversation = conversation,
                            onOpenChat = { onOpenChat(conversation.id) },
                            onDeleteRequest = { conversationToDelete = conversation },
                        )
                    }
                }
            }
        }
    }

    // ── Delete Confirmation Dialog ─────────────────────────────────────────────
    if (conversationToDelete != null) {
        AlertDialog(
            onDismissRequest = { conversationToDelete = null },
            title = {
                Text(
                    text = "Delete Chat",
                    fontFamily = ComfortaaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E1E1E),
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this chat?",
                    fontFamily = AlbertSansFamily,
                    fontSize = 14.sp,
                    color = Color(0xFF555555),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val idToDelete = conversationToDelete?.id
                        conversationToDelete = null
                        if (idToDelete != null) {
                            onDeleteConversation(idToDelete)
                        }
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Bold,
                        fontFamily = AlbertSansFamily,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { conversationToDelete = null }
                ) {
                    Text(
                        text = "Cancel",
                        color = Color(0xFF7A7A7A),
                        fontFamily = AlbertSansFamily,
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableConversationRow(
    conversation: ChatConversation,
    onOpenChat: () -> Unit,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequest()
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        backgroundContent = {
            val isSwipingDelete = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSwipingDelete) Color(0xFFE53935) else Color.Transparent)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (isSwipingDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete chat",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        },
        content = {
            ConversationRowItem(
                conversation = conversation,
                onClick = onOpenChat,
            )
        }
    )
}

@Composable
private fun ConversationRowItem(
    conversation: ChatConversation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isUnread = conversation.unreadCount > 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFDFDFD))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Contact Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
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
                    modifier = Modifier.size(26.dp),
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        // Middle Content (Name | Property & Last Message)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.displayHeader,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E1E1E),
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = conversation.lastMessage,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isUnread) Color(0xFF1E1E1E) else Color(0xFF7A7A7A),
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.width(10.dp))

        // Timestamp & Yellow Unread Dot on right
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = conversation.lastTimestamp,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                    color = if (isUnread) GoldenMarigold else Color(0xFF9E9E9E),
                ),
            )
            if (isUnread) {
                Spacer(Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(GoldenMarigold, CircleShape)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun MessagesScreenPreview() {
    MobInTheme {
        MessagesContent(
            uiState = MessagesUiState(
                allConversations = listOf(
                    ChatConversation(
                        id = "1",
                        contactName = "Mary Ann Dasalo",
                        propertyName = "Casa Urgello",
                        lastMessage = "Hi! I'm interested in the 3 bed room. Is this still available?",
                        lastTimestamp = "10:33 AM",
                    ),
                    ChatConversation(
                        id = "2",
                        contactName = "Maria Santos",
                        propertyName = "Maria's Boarding",
                        lastMessage = "Hi! I'm interested in the 3 bed room. Is this still available?",
                        lastTimestamp = "10:33 AM",
                    ),
                ),
                filteredConversations = listOf(
                    ChatConversation(
                        id = "1",
                        contactName = "Mary Ann Dasalo",
                        propertyName = "Casa Urgello",
                        lastMessage = "Hi! I'm interested in the 3 bed room. Is this still available?",
                        lastTimestamp = "10:33 AM",
                    ),
                    ChatConversation(
                        id = "2",
                        contactName = "Maria Santos",
                        propertyName = "Maria's Boarding",
                        lastMessage = "Hi! I'm interested in the 3 bed room. Is this still available?",
                        lastTimestamp = "10:33 AM",
                    ),
                ),
            ),
            onQueryChange = {},
            onTabSelect = {},
            onOpenChat = {},
        )
    }
}
