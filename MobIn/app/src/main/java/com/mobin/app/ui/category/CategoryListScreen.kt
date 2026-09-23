package com.mobin.app.ui.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mobin.app.data.model.Property
import com.mobin.app.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    categoryName: String,
    onBack: () -> Unit,
    onPropertyClick: (Property) -> Unit,
    viewModel: CategoryListViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(categoryName) {
        viewModel.loadCategory(categoryName)
    }

    Scaffold(
        containerColor = Color(0xFFFDFDFD),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.displayTitle,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = ComfortaaFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF1E1E1E),
                            ),
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = uiState.subtitleCount,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = Color(0xFF7A7A7A),
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
                    IconButton(onClick = { viewModel.openFilterSheet() }) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "Filter",
                            tint = Color(0xFF1E1E1E),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFDFDFD)),
            )
        },
    ) { padding ->
        if (uiState.filteredProperties.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = null,
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(54.dp),
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "No ${uiState.displayTitle.lowercase()} found",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            fontSize = 16.sp,
                        ),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Check back soon for new listings",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF888888),
                            fontSize = 13.sp,
                        ),
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
                items(uiState.filteredProperties, key = { it.id }) { property ->
                    CategoryPropertyCard(
                        property = property,
                        onClick = { onPropertyClick(property) },
                        onToggleFavorite = { viewModel.toggleFavorite(property.id) },
                    )
                }
            }
        }
    }

    // ── Pull-up / Pull-down Filter Bottom Sheet ────────────────────────
    if (uiState.showFilterSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeFilterSheet() },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = White,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            FilterBottomSheetContent(
                tempState = uiState.tempFilterState,
                onPriceChange = { min, max -> viewModel.updateTempPriceRange(min, max) },
                onBedsChange = { viewModel.updateTempBeds(it) },
                onRatingChange = { viewModel.updateTempRating(it) },
                onVerifiedChange = { viewModel.updateTempVerified(it) },
                onReset = { viewModel.resetFilters() },
                onApply = { viewModel.applyFilters() },
            )
        }
    }
}

@Composable
private fun CategoryPropertyCard(
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
            // Main Card Image
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
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape),
                ) {
                    Icon(
                        imageVector = if (property.isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Save",
                        tint = if (property.isSaved) GoldenMarigold else Color(0xFFBF6B04),
                        modifier = Modifier.size(20.dp),
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

@Composable
private fun FilterBottomSheetContent(
    tempState: CategoryFilterState,
    onPriceChange: (Float, Float) -> Unit,
    onBedsChange: (Int?) -> Unit,
    onRatingChange: (String) -> Unit,
    onVerifiedChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
) {
    val ratingOptions = listOf("4.5 and Above", "4.0 and Above", "3.5 and Above", "Any rating")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        // ── 1. Price Range Section ─────────────────────────────────────────
        Text(
            text = "Price Range",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1E1E1E),
            ),
        )

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "₱1,500", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = "₱20,000", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        RangeSlider(
            value = tempState.minPrice..tempState.maxPrice,
            onValueChange = { range ->
                onPriceChange(range.start, range.endInclusive)
            },
            valueRange = 1500f..20000f,
            steps = 37, // step of 500
            colors = SliderDefaults.colors(
                thumbColor = GoldenMarigold,
                activeTrackColor = GoldenMarigold,
                inactiveTrackColor = Color(0xFFF3E5D8),
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Minimum Box
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = White,
                border = BorderStroke(1.dp, Color(0xFFDCDCDC)),
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Minimum", fontSize = 10.5.sp, color = Color(0xFF7A7A7A))
                    Text(
                        text = "₱${NumberFormat.getNumberInstance(Locale.US).format(tempState.minPrice.toInt())}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                    )
                }
            }

            // Maximum Box
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = White,
                border = BorderStroke(1.dp, Color(0xFFDCDCDC)),
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Maximum", fontSize = 10.5.sp, color = Color(0xFF7A7A7A))
                    Text(
                        text = "₱${NumberFormat.getNumberInstance(Locale.US).format(tempState.maxPrice.toInt())}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                    )
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        // ── 2. Beds Available Section ──────────────────────────────────────
        Text(
            text = "Beds Available",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1E1E1E),
            ),
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            listOf(1, 2, 3).forEach { count ->
                val isSelected = tempState.selectedBeds == count
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onBedsChange(count) },
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) GoldenMarigold else White,
                    border = BorderStroke(1.dp, if (isSelected) GoldenMarigold else Color(0xFFDCDCDC)),
                ) {
                    Text(
                        text = "$count bed${if (count > 1) "s" else ""}",
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) White else Color(0xFF333333),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = White,
                border = BorderStroke(1.dp, Color(0xFFDCDCDC)),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onBedsChange(null) },
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Custom",
                    tint = Color(0xFF555555),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp).size(16.dp),
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        // ── 3. Rating Section ──────────────────────────────────────────────
        Text(
            text = "Rating",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1E1E1E),
            ),
        )

        Spacer(Modifier.height(6.dp))

        ratingOptions.forEach { option ->
            val isSelected = tempState.selectedRatingOption == option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = isSelected, onClick = { onRatingChange(option) })
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onRatingChange(option) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = GoldenMarigold,
                        unselectedColor = Color(0xFFB0B0B0),
                    ),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = option,
                    fontSize = 13.5.sp,
                    color = Color(0xFF333333),
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // ── 4. Verified Properties Section ─────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Verified Properties",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        color = Color(0xFF1E1E1E),
                    ),
                )
                Row {
                    Text(
                        text = "Show verified properties ",
                        fontSize = 12.sp,
                        color = Color(0xFF7A7A7A),
                    )
                    Text(
                        text = "only",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBF6B04),
                    )
                }
            }

            Switch(
                checked = tempState.verifiedOnly,
                onCheckedChange = onVerifiedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = White,
                    checkedTrackColor = GoldenMarigold,
                    uncheckedThumbColor = White,
                    uncheckedTrackColor = Color(0xFFDCDCDC),
                ),
            )
        }

        Spacer(Modifier.height(28.dp))

        // ── 5. Action Buttons ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OutlinedButton(
                onClick = onReset,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.2.dp, GoldenMarigold),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF8C4E03),
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
            ) {
                Text(text = "Reset", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Button(
                onClick = onApply,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldenMarigold,
                    contentColor = White,
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
            ) {
                Text(text = "Apply Filters", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun CategoryListScreenPreview() {
    MobInTheme {
        CategoryListScreen(
            categoryName = "Boarding Houses",
            onBack = {},
            onPropertyClick = {},
        )
    }
}
