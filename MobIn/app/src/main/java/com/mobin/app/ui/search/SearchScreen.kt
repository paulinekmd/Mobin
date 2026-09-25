package com.mobin.app.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mobin.app.data.model.Property
import com.mobin.app.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

private val searchCategories = listOf("All", "Boarding Houses", "Apartments", "Dormitories")

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onPropertyClick: (Property) -> Unit,
    viewModel: SearchViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SearchContent(
        uiState = uiState,
        onQueryChange = { viewModel.onQueryChange(it) },
        onCategorySelect = { viewModel.selectCategory(it) },
        onSuggestionClick = { viewModel.onSelectSearchSuggestion(it) },
        onSearchSubmitted = { viewModel.onSubmitSearch(it) },
        onRemoveRecent = { viewModel.removeRecentSearch(it) },
        onClearAllRecent = { viewModel.clearAllRecentSearches() },
        onToggleFavorite = { viewModel.toggleFavorite(it) },
        onPropertyClick = onPropertyClick,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun SearchContent(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onSuggestionClick: (String) -> Unit,
    onSearchSubmitted: (String) -> Unit,
    onRemoveRecent: (String) -> Unit,
    onClearAllRecent: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onPropertyClick: (Property) -> Unit,
    onBack: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

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
                            text = "Search",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = ComfortaaFamily,
                                fontWeight = FontWeight.Bold,
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
                            tint = Color(0xFFBF6B04), // AmberSpice
                        )
                    }
                },
                actions = {
                    // Balancing empty action for true center title
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFDFDFD)),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Search Input Field ─────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Search properties in Urgello...",
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        onSearchSubmitted(uiState.query)
                    }
                ),
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

            if (!uiState.isSearching) {
                // ── Initial Search State (Recent & Popular) ────────────────────

                // Recent Searches
                if (uiState.recentSearches.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Recent Searches",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1E1E1E),
                            ),
                        )
                        Text(
                            text = "Clear all",
                            color = Color(0xFFBF6B04),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable(onClick = onClearAllRecent)
                                .padding(4.dp),
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    uiState.recentSearches.forEach { term ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggestionClick(term) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF888888),
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = term,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    color = Color(0xFF333333),
                                ),
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                onClick = { onRemoveRecent(term) },
                                modifier = Modifier.size(24.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Remove",
                                    tint = Color(0xFF888888),
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }

                // Popular Searches
                Text(
                    text = "Popular Searches",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E1E1E),
                    ),
                )

                Spacer(Modifier.height(12.dp))

                val popularChunks = uiState.popularSearches.chunked(2)
                popularChunks.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowItems.forEach { pop ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onSuggestionClick(pop) },
                                shape = RoundedCornerShape(50),
                                color = Color(0xFFF2ECE6),
                            ) {
                                Text(
                                    text = pop,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF4A4F57),
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.weight(1f))

                // Bottom Illustration / Empty State
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .background(Color(0xFFF9EADA), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = Color(0xFFBF6B04),
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "Find your next place to stay",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = ComfortaaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1E1E1E),
                        ),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Browse available places in Urgello.\nChoose a category or search for a property.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.5.sp,
                            color = Color(0xFF7A7A7A),
                            lineHeight = 18.sp,
                        ),
                        textAlign = TextAlign.Center,
                    )
                }

            } else {
                // ── Search Results State (Screen 3 in Figma) ───────────────────

                Text(
                    text = "${uiState.filteredResults.size} properties found",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = Color(0xFF7A7A7A),
                    ),
                )

                Spacer(Modifier.height(12.dp))

                // Filter Category Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(searchCategories) { cat ->
                        val isSelected = uiState.selectedCategory.equals(cat, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onCategorySelect(cat) },
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) GoldenMarigold else White,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) GoldenMarigold else Color(0xFFDCC3B2)
                            ),
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) White else Color(0xFF333333),
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (uiState.filteredResults.isEmpty()) {
                    // Empty Search Results State
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 32.dp),
                        ) {
                            // Magnifying Glass
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .background(PeachSand.copy(alpha = 0.55f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = AmberSpice,
                                    modifier = Modifier.size(38.dp),
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "No results found.",
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
                                text = if (uiState.query.isNotBlank()) {
                                    "We couldn't find any properties matching \"${uiState.query}\".\nTry checking your spelling or searching for another term."
                                } else {
                                    "No properties found in this category."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF7A7A7A),
                                    lineHeight = 18.sp,
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp),
                            )
                        }
                    }
                } else {
                    // Vertical List of Search Result Cards
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 20.dp),
                    ) {
                        items(uiState.filteredResults, key = { it.id }) { property ->
                            SearchResultItemCard(
                                property = property,
                                onClick = {
                                    if (uiState.query.isNotBlank()) {
                                        onSearchSubmitted(uiState.query)
                                    }
                                    onPropertyClick(property)
                                },
                                onToggleFavorite = { onToggleFavorite(property.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItemCard(
    property: Property,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formattedPrice = NumberFormat.getNumberInstance(Locale.US).format(property.price.toInt())

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = White,
        border = BorderStroke(1.dp, Color(0xFFF0EBE5)),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left Image Thumbnail
            Box(
                modifier = Modifier
                    .size(105.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE2E4E8)),
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
                        modifier = Modifier.size(38.dp),
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Right Details Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = property.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = Color(0xFF1E1E1E),
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = if (property.isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (property.isSaved) GoldenMarigold else Color(0xFFBF6B04),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = property.location,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            color = Color(0xFF7A7A7A),
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "₱$formattedPrice / month",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF2E7D32),
                    ),
                )

                Spacer(Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${property.availableBeds} beds available",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFBF6B04),
                        ),
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = GoldenMarigold,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = property.rating.toString(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333),
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun SearchScreenPreview() {
    MobInTheme {
        SearchContent(
            uiState = SearchUiState(),
            onQueryChange = {},
            onCategorySelect = {},
            onSuggestionClick = {},
            onSearchSubmitted = {},
            onRemoveRecent = {},
            onClearAllRecent = {},
            onToggleFavorite = {},
            onPropertyClick = {},
            onBack = {},
        )
    }
}
