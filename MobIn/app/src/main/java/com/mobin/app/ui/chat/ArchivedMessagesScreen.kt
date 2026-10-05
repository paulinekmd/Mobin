package com.mobin.app.ui.chat

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mobin.app.data.model.ChatConversation
import com.mobin.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ArchivedMessagesScreen(
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    viewModel: ArchivedMessagesViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    ArchivedMessagesContent(
        uiState = uiState,
        onBack = onBack,
        onQueryChange = { viewModel.onQueryChange(it) },
        onOpenChat = onOpenChat,
        onUnarchiveConversation = { viewModel.unarchiveConversation(it) },
        onDeleteConversation = { viewModel.deleteConversation(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ArchivedMessagesContent(
    uiState: ArchivedMessagesUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onUnarchiveConversation: (String) -> Unit,
    onDeleteConversation: (String) -> Unit,
) {
    var conversationToUnarchive by remember { mutableStateOf<ChatConversation?>(null) }
    var conversationToDelete by remember { mutableStateOf<ChatConversation?>(null) }

    Scaffold(
        containerColor = Color(0xFFFDFDFD),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Archived Messages",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = ComfortaaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF1E1E1E),
                        ),
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFDFDFD)),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 22.dp, vertical = 8.dp),
        ) {
            // ── Search Input ───────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Search archived messages",
                        fontSize = 13.5.sp,
                        color = Color(0xFF888888),
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF8C4E03),
                        modifier = Modifier.size(20.dp),
                    )
                },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear",
                                tint = Color(0xFF888888),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8C4E03),
                    unfocusedBorderColor = Color(0xFF8C4E03),
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedTextColor = Color(0xFF1E1E1E),
                    unfocusedTextColor = Color(0xFF1E1E1E),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(18.dp))

            // ── Archived Feed ──────────────────────────────────────────────────
            if (uiState.filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Archive,
                            contentDescription = null,
                            tint = Color(0xFFBDBDBD),
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (uiState.query.isNotBlank()) {
                                "No archived messages matching \"${uiState.query}\""
                            } else {
                                "No archived messages"
                            },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color(0xFF555555),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Conversations you archive will appear here.",
                            fontSize = 12.5.sp,
                            color = Color(0xFF888888),
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
                            isArchivedView = true,
                            onPrimaryAction = { conversationToUnarchive = conversation },
                            onDeleteAction = { conversationToDelete = conversation },
                            onOpenChat = { onOpenChat(conversation.id) },
                        )
                    }
                }
            }
        }
    }

    // ── Unarchive Confirmation Dialog ──────────────────────────────────────────
    if (conversationToUnarchive != null) {
        AlertDialog(
            onDismissRequest = { conversationToUnarchive = null },
            title = {
                Text(
                    text = "Unarchive conversation?",
                    fontFamily = ComfortaaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E1E1E),
                )
            },
            text = {
                Text(
                    text = "This conversation with ${conversationToUnarchive?.contactName} will return to your active messages tab.",
                    fontFamily = AlbertSansFamily,
                    fontSize = 14.sp,
                    color = Color(0xFF555555),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val idToUnarchive = conversationToUnarchive?.id
                        conversationToUnarchive = null
                        if (idToUnarchive != null) {
                            onUnarchiveConversation(idToUnarchive)
                        }
                    }
                ) {
                    Text(
                        text = "Unarchive",
                        color = Color(0xFFBF6B04),
                        fontWeight = FontWeight.Bold,
                        fontFamily = AlbertSansFamily,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { conversationToUnarchive = null }
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
                    text = "Are you sure you want to delete this conversation with ${conversationToDelete?.contactName}?",
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

@Composable
fun SwipeableConversationActionRow(
    conversation: ChatConversation,
    isArchivedView: Boolean = false,
    onPrimaryAction: () -> Unit, // Archive or Unarchive
    onDeleteAction: () -> Unit,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val maxRevealWidthPx = with(LocalDensity.current) { 152.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFDFDFD)),
    ) {
        // Background Actions (revealed when swiped left)
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Primary Action: Archive / Unarchive
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .clickable {
                        coroutineScope.launch { offsetX.animateTo(0f) }
                        onPrimaryAction()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = if (isArchivedView) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                        contentDescription = if (isArchivedView) "Unarchive" else "Archive",
                        tint = Color(0xFFBF6B04),
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = if (isArchivedView) "Unarchive" else "Archive",
                        color = Color(0xFFBF6B04),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Divider line between Archive and Delete
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(Color(0xFFE0E0E0))
            )

            // Delete Action
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                    .clickable {
                        coroutineScope.launch { offsetX.animateTo(0f) }
                        onDeleteAction()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "Delete",
                        color = Color(0xFFE53935),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // Foreground content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .background(Color(0xFFFDFDFD))
                .draggable(
                    orientation = androidx.compose.foundation.gestures.Orientation.Horizontal,
                    state = androidx.compose.foundation.gestures.rememberDraggableState { delta ->
                        coroutineScope.launch {
                            val target = (offsetX.value + delta).coerceIn(-maxRevealWidthPx, 0f)
                            offsetX.snapTo(target)
                        }
                    },
                    onDragStopped = { velocity: Float ->
                        if (offsetX.value < -maxRevealWidthPx / 2.5f || velocity < -300f) {
                            offsetX.animateTo(-maxRevealWidthPx, spring(stiffness = Spring.StiffnessMediumLow))
                        } else {
                            offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                        }
                    }
                )
        ) {
            ConversationRowItem(
                conversation = conversation,
                onClick = {
                    if (offsetX.value < -10f) {
                        coroutineScope.launch { offsetX.animateTo(0f) }
                    } else {
                        onOpenChat()
                    }
                },
            )
        }
    }
}

@Composable
fun ConversationRowItem(
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
private fun ArchivedMessagesScreenPreview() {
    MobInTheme {
        ArchivedMessagesContent(
            uiState = ArchivedMessagesUiState(
                allConversations = listOf(
                    ChatConversation(
                        id = "1",
                        contactName = "Mary Ann Dasalo",
                        propertyName = "Casa Urgello",
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
                ),
            ),
            onBack = {},
            onQueryChange = {},
            onOpenChat = {},
            onUnarchiveConversation = {},
            onDeleteConversation = {},
        )
    }
}
