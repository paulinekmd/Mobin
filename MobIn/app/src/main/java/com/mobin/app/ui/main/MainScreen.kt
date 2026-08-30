package com.mobin.app.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobin.app.ui.home.HomeScreen
import com.mobin.app.ui.profile.ProfileScreen
import com.mobin.app.ui.theme.GoldenMarigold
import com.mobin.app.ui.theme.White

private data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem("home",    "Home",    Icons.Filled.Home,       Icons.Outlined.Home),
    BottomNavItem("saved",   "Saved",   Icons.Filled.Favorite,   Icons.Outlined.FavoriteBorder),
    BottomNavItem("chats",   "Chats",   Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline),
    BottomNavItem("profile", "Profile", Icons.Filled.Person,     Icons.Outlined.Person),
)

@Composable
fun MainScreen(
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCategoryList: (String) -> Unit = {},
    onNavigateToPropertyDetails: (String) -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToReviewInfo: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onLogout: () -> Unit,
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    Scaffold(
        containerColor = Color(0xFFFDFDFD),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = White,
                    border = BorderStroke(1.dp, Color(0xFFE2E2E2)),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentRoute == item.route
                            val itemColor = if (selected) GoldenMarigold else Color(0xFF4A4A4A)

                            IconButton(
                                onClick = {
                                    if (currentRoute != item.route) {
                                        bottomNavController.navigate(item.route) {
                                            popUpTo(bottomNavController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label,
                                        tint = itemColor,
                                        modifier = Modifier.size(22.dp),
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = item.label,
                                        color = itemColor,
                                        fontSize = 10.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("home") {
                HomeScreen(
                    onSearchClick = onNavigateToSearch,
                    onPropertyClick = { property -> onNavigateToPropertyDetails(property.id) },
                    onSeeAllClick = { category -> onNavigateToCategoryList(category) },
                )
            }
            composable("saved") {
                com.mobin.app.ui.saved.SavedScreen(
                    onPropertyClick = { property -> onNavigateToPropertyDetails(property.id) },
                    onBack = {
                        bottomNavController.navigate("home") {
                            popUpTo(bottomNavController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable("chats") {
                com.mobin.app.ui.chat.MessagesScreen(
                    onOpenChat = { chatId -> onNavigateToChat(chatId) },
                )
            }
            composable("profile") {
                ProfileScreen(
                    onNavigateToReviewInfo = onNavigateToReviewInfo,
                    onNavigateToChangePassword = onNavigateToChangePassword,
                    onLogout = onLogout,
                )
            }
        }
    }
}
