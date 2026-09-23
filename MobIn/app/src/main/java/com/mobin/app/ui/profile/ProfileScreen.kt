package com.mobin.app.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mobin.app.data.model.Profile
import com.mobin.app.ui.theme.*

@Composable
fun ProfileScreen(
    onNavigateToReviewInfo: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.uploadAvatar(context, it) } }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(uiState.error, uiState.successMessage) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    ProfileContent(
        uiState = uiState,
        currentEmail = viewModel.currentEmail,
        snackbarHostState = snackbarHostState,
        onPickPhoto = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onNavigateToReviewInfo = onNavigateToReviewInfo,
        onNavigateToChangePassword = onNavigateToChangePassword,
        onLogout = { viewModel.signOut(onLogout) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileContent(
    uiState: ProfileUiState,
    currentEmail: String?,
    snackbarHostState: SnackbarHostState,
    onPickPhoto: () -> Unit,
    onNavigateToReviewInfo: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onLogout: () -> Unit,
) {
    val profile = uiState.profile
    val isNameSet = !profile?.fullName.isNullOrBlank()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFFDFDFD),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E1E),
                        ),
                    )
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF333333),
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFFDFDFD)),
            )
        },
    ) { padding ->

        if (uiState.isLoading && profile == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = GoldenMarigold)
                    Spacer(Modifier.height(12.dp))
                    Text("Loading profile...", style = MaterialTheme.typography.bodyMedium, color = OliveBronze)
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Top User Profile Card ──────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = Color(0xFFFFFDF8),
                border = BorderStroke(1.dp, Color(0xFFFEECCB)),
                shape = RoundedCornerShape(20.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Circular Avatar (Tap to change - Optional)
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B3E43))
                            .clickable(onClick = onPickPhoto),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!profile?.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profile?.avatarUrl,
                                contentDescription = "Profile photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Avatar",
                                tint = White,
                                modifier = Modifier.size(36.dp),
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    // User Details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isNameSet) profile!!.fullName!! else "No name set",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isNameSet) Color(0xFF222222) else Color(0xFF757575),
                                fontSize = 16.sp,
                            ),
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = currentEmail ?: "alfecheangel2@gmail.com",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF7A7A7A),
                                fontSize = 12.sp,
                            ),
                        )
                        Spacer(Modifier.height(6.dp))
                        // Renter badge
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFDCE0E5),
                        ) {
                            Text(
                                text = profile?.accountType?.replaceFirstChar { it.uppercase() } ?: "Renter",
                                color = Color(0xFF4A4F57),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
            }

            // ── Incomplete Profile Message Banner (Only shown if name not set) ──
            if (!isNameSet) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onNavigateToReviewInfo),
                    color = Color(0xFFFFF8E7),
                    border = BorderStroke(1.dp, Color(0xFFFBD784)),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = AmberSpice,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Profile is incomplete. Tap to add your name.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ChestnutBark,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = AmberSpice,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            if (uiState.isLoading && profile != null) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = GoldenMarigold,
                    trackColor = LightGray,
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Account Section ────────────────────────────────────────────────
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E),
                    fontSize = 15.sp,
                ),
            )

            Spacer(Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = White,
                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Outlined.Person,
                        title = "Personal Information",
                        subtitle = "View and update your personal details",
                        onClick = onNavigateToReviewInfo,
                    )
                    HorizontalDivider(color = Color(0xFFF3F3F3), thickness = 1.dp)
                    ProfileMenuRow(
                        icon = Icons.Outlined.Lock,
                        title = "Change Password",
                        subtitle = "Update your account password",
                        onClick = onNavigateToChangePassword,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Support Section ────────────────────────────────────────────────
            Text(
                text = "Support",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E),
                    fontSize = 15.sp,
                ),
            )

            Spacer(Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = White,
                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Outlined.HelpOutline,
                        title = "Personal Information",
                        subtitle = "View and update your personal details",
                        onClick = {},
                    )
                    HorizontalDivider(color = Color(0xFFF3F3F3), thickness = 1.dp)
                    ProfileMenuRow(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        title = "Change Password",
                        subtitle = "Update your account password",
                        onClick = {},
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Log Out Button ─────────────────────────────────────────────────
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.5.dp, Color(0xFFEB5757)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFEB5757),
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = "Log Out",
                        tint = Color(0xFFEB5757),
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Log Out",
                        color = Color(0xFFEB5757),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF333333),
            modifier = Modifier.size(26.dp),
        )

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222),
                    fontSize = 13.5.sp,
                ),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF888888),
                    fontSize = 11.sp,
                ),
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF222222),
            modifier = Modifier.size(24.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun ProfileScreenPreview() {
    MobInTheme {
        ProfileContent(
            uiState = ProfileUiState(
                profile = Profile(
                    id = "1",
                    fullName = "Angel Ann Labora",
                    phone = "+63 912 345 6789",
                    accountType = "renter",
                )
            ),
            currentEmail = "alfecheangel2@gmail.com",
            snackbarHostState = SnackbarHostState(),
            onPickPhoto = {},
            onNavigateToReviewInfo = {},
            onNavigateToChangePassword = {},
            onLogout = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915, name = "Incomplete Profile")
@Composable
private fun ProfileIncompletePreview() {
    MobInTheme {
        ProfileContent(
            uiState = ProfileUiState(
                profile = Profile(
                    id = "1",
                    fullName = null,
                    phone = null,
                    accountType = "renter",
                )
            ),
            currentEmail = "newuser@gmail.com",
            snackbarHostState = SnackbarHostState(),
            onPickPhoto = {},
            onNavigateToReviewInfo = {},
            onNavigateToChangePassword = {},
            onLogout = {},
        )
    }
}
