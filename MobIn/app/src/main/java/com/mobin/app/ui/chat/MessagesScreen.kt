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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobin.app.data.model.ChatConversation
import com.mobin.app.ui.theme.*

@Composable
fun MessagesScreen(
    onOpenChat: (String) -> Unit,
    onNavigateToArchived: () -> Unit = {},
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
        onNavigateToArchived = onNavigateToArchived,
        onArchiveConversation = { viewModel.archiveConversation(it) },
        onDeleteConversation = { viewModel.deleteConversation(it) },
    )
}

@Composable
internal fun MessagesContent(
    uiState: MessagesUiState,
    onQueryChange: (String) -> Unit,
    onTabSelect: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onNavigateToArchived: () -> Unit = {},
    onArchiveConversation: (String) -> Unit = {},
    onDeleteConversation: (String) -> Unit = {},
) {
    var conversationToArchive by remember { mutableStateOf<ChatConversation?>(null) }
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
            // ── Header Title & Top-Right Archive Icon Button ───────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Messages",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = ComfortaaFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = Color(0xFF1E1E1E),
                    ),
                )

                IconButton(
                    onClick = onNavigateToArchived,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Archive,
                        contentDescription = "Archived messages",
                        tint = Color(0xFF333333),
                        modifier = Modifier.size(26.dp),
                    )
                }
            }

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
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    items(uiState.filteredConversations, key = { it.id }) { conversation ->
                        SwipeableConversationActionRow(
                            conversation = conversation,
                            isArchivedView = false,
                            onPrimaryAction = { conversationToArchive = conversation },
                            onDeleteAction = { conversationToDelete = conversation },
                            onOpenChat = { onOpenChat(conversation.id) },
                        )
                    }
                }
            }
        }
    }

    // ── Archive Confirmation Dialog ────────────────────────────────────────────
    if (conversationToArchive != null) {
        AlertDialog(
            onDismissRequest = { conversationToArchive = null },
            title = {
                Text(
                    text = "Archive conversation?",
                    fontFamily = ComfortaaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E1E1E),
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to archive this chat? You can access it anytime by tapping the archive icon at the top right.",
                    fontFamily = AlbertSansFamily,
                    fontSize = 14.sp,
                    color = Color(0xFF555555),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val idToArchive = conversationToArchive?.id
                        conversationToArchive = null
                        if (idToArchive != null) {
                            onArchiveConversation(idToArchive)
                        }
                    }
                ) {
                    Text(
                        text = "Archive",
                        color = Color(0xFFBF6B04),
                        fontWeight = FontWeight.Bold,
                        fontFamily = AlbertSansFamily,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { conversationToArchive = null }
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
            onNavigateToArchived = {},
        )
    }
}
