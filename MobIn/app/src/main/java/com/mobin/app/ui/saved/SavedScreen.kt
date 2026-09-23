package com.mobin.app.ui.saved

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mobin.app.data.model.Property
import com.mobin.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    onPropertyClick: (Property) -> Unit,
    onBack: () -> Unit = {},
    viewModel: SavedViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSavedList()
    }

    Scaffold(
        containerColor = Color(0xFFFDFDFD),
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Saved Properties",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = ComfortaaFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF1E1E1E),
                            ),
                        )
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
                    // Balance navigation icon for true center title
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFDFDFD)),
            )
        },
    ) { padding ->
        if (uiState.savedProperties.isEmpty()) {
            // Empty Wishlist State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(Color(0xFFFBF1E6), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = GoldenMarigold,
                            modifier = Modifier.size(36.dp),
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "No saved properties yet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = ComfortaaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF1E1E1E),
                        ),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Tap the heart icon on any property to add it to your saved wishlist.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            color = Color(0xFF7A7A7A),
                            lineHeight = 19.sp,
                        ),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            ) {
                items(uiState.savedProperties, key = { it.id }) { property ->
                    SavedPropertyCard(
                        property = property,
                        onClick = { onPropertyClick(property) },
                        onToggleFavorite = { viewModel.toggleFavorite(property.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedPropertyCard(
    property: Property,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = White,
        border = BorderStroke(1.dp, Color(0xFFE6E6E6)),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            // Main Image Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE2E4E8))
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                if (!property.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = property.imageUrl,
                        contentDescription = property.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = Color(0xFF8C929D),
                        modifier = Modifier.size(48.dp),
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp),
                ) {
                    Icon(
                        imageVector = if (property.isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Save",
                        tint = if (property.isSaved) GoldenMarigold else Color(0xFFBF6B04),
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Title, Beds badge & Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = property.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E1E1E),
                    ),
                    modifier = Modifier.weight(1f),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF4E4D7), // PeachSand
                    ) {
                        Text(
                            text = "${property.availableBeds} beds available",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF8C4E03),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = GoldenMarigold,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = property.rating.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF7A7A7A),
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = property.location,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = Color(0xFF7A7A7A),
                    ),
                )
            }

            Spacer(Modifier.height(14.dp))

            // View Details Button
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.2.dp, GoldenMarigold),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF8C4E03),
                ),
            ) {
                Text(
                    text = "View Details →",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = Color(0xFF8C4E03),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun SavedScreenPreview() {
    MobInTheme {
        SavedScreen(
            onPropertyClick = {},
        )
    }
}
