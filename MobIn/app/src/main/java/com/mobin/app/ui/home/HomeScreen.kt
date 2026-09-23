package com.mobin.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.House
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobin.app.data.model.Property
import com.mobin.app.ui.components.PropertyCard
import com.mobin.app.ui.theme.*

private data class CategoryItem(
    val name: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val categories = listOf(
    CategoryItem("Boarding House", Icons.Filled.House, Icons.Outlined.House),
    CategoryItem("Apartments", Icons.Filled.Apartment, Icons.Outlined.Apartment),
    CategoryItem("Dormitories", Icons.Filled.Hotel, Icons.Outlined.Hotel),
)

@Composable
fun HomeScreen(
    onSearchClick: () -> Unit = {},
    onPropertyClick: (Property) -> Unit = {},
    onSeeAllClick: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    HomeContent(
        uiState = uiState,
        onSearchClick = onSearchClick,
        onCategorySelect = { viewModel.selectCategory(it) },
        onPropertyClick = onPropertyClick,
        onToggleFavorite = { viewModel.toggleFavorite(it) },
        onSeeAllClick = onSeeAllClick,
    )
}

@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    onSearchClick: () -> Unit = {},
    onCategorySelect: (String) -> Unit = {},
    onPropertyClick: (Property) -> Unit = {},
    onToggleFavorite: (String) -> Unit = {},
    onSeeAllClick: (String) -> Unit = {},
) {
    val greeting = if (!uiState.userName.isNullOrBlank()) "Hi there, ${uiState.userName}!" else "Hi there!"

    Scaffold(
        containerColor = Color(0xFFFDFDFD),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
        ) {
            // ── Top Header Greeting ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
            ) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = ComfortaaFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color(0xFF1E1E1E),
                    ),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Find your next place to stay",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = AlbertSansFamily,
                        fontSize = 14.sp,
                        color = Color(0xFF6B6B6B),
                    ),
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Search Bar ─────────────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onSearchClick),
                shape = RoundedCornerShape(14.dp),
                color = White,
                border = BorderStroke(1.2.dp, Color(0xFF8C4E03)), // ChestnutBark outline
                shadowElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF8C4E03),
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Search for a place...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            color = Color(0xFF888888),
                        ),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Categories Section ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
            ) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E1E1E),
                    ),
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    categories.forEach { category ->
                        val isSelected = uiState.selectedCategory.equals(category.name, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onCategorySelect(category.name) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF8C4E03) else White,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF8C4E03) else Color(0xFFE2E4E8)
                            ),
                            shadowElevation = if (isSelected) 3.dp else 0.dp,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = if (isSelected) category.selectedIcon else category.unselectedIcon,
                                    contentDescription = category.name,
                                    tint = if (isSelected) White else Color(0xFF333333),
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = category.name,
                                    color = if (isSelected) White else Color(0xFF333333),
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 2,
                                    lineHeight = 13.sp,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Section 1: Dynamic Category Section (Boarding Houses / Apartments / Dormitories) ──
            SectionHeader(
                title = uiState.categorySectionTitle,
                onSeeAllClick = { onSeeAllClick(uiState.selectedCategory) },
            )

            Spacer(Modifier.height(12.dp))

            if (uiState.categoryProperties.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF9F9F9),
                    border = BorderStroke(1.dp, Color(0xFFECECEC)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No ${uiState.categorySectionTitle.lowercase()} listed yet",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF888888),
                                fontSize = 13.5.sp,
                            ),
                        )
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(uiState.categoryProperties, key = { it.id }) { property ->
                        PropertyCard(
                            property = property,
                            onClick = { onPropertyClick(property) },
                            onToggleFavorite = { onToggleFavorite(property.id) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Section 2: Available Now ───────────────────────────────────────
            SectionHeader(
                title = "Available Now",
                onSeeAllClick = { onSeeAllClick("Available Now") },
            )

            Spacer(Modifier.height(12.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(uiState.availableNow, key = { it.id }) { property ->
                    PropertyCard(
                        property = property,
                        onClick = { onPropertyClick(property) },
                        onToggleFavorite = { onToggleFavorite(property.id) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.5.sp,
                color = Color(0xFF1E1E1E),
            ),
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(onClick = onSeeAllClick)
                .padding(vertical = 4.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "See All",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFBF6B04), // AmberSpice
                ),
            )
            Spacer(Modifier.width(2.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFBF6B04),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun HomeScreenPreview() {
    MobInTheme {
        HomeContent(
            uiState = HomeUiState(
                userName = "Angel",
                selectedCategory = "Boarding House",
                categoryProperties = listOf(
                    Property(
                        id = "1",
                        title = "Casa Urgello",
                        category = "Boarding House",
                        location = "Urgello, Cebu City",
                        price = 3500.0,
                        availableBeds = 3,
                        rating = 4.5,
                    ),
                    Property(
                        id = "2",
                        title = "Maria's Boarding",
                        category = "Boarding House",
                        location = "Urgello, Cebu City",
                        price = 3500.0,
                        availableBeds = 3,
                        rating = 4.5,
                    ),
                ),
                availableNow = listOf(
                    Property(
                        id = "3",
                        title = "Greenview Apartment",
                        category = "Apartments",
                        location = "Sambag 1, Cebu City",
                        price = 3500.0,
                        availableBeds = 3,
                        rating = 4.5,
                    ),
                    Property(
                        id = "4",
                        title = "Student Hub Dorm",
                        category = "Dormitories",
                        location = "Urgello, Cebu City",
                        price = 3500.0,
                        availableBeds = 3,
                        rating = 4.5,
                    ),
                ),
            )
        )
    }
}
