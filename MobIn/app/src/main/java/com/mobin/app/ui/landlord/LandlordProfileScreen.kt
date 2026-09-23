package com.mobin.app.ui.landlord

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mobin.app.data.model.LandlordProfile
import com.mobin.app.data.model.LandlordReview
import com.mobin.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandlordProfileScreen(
    propertyId: String,
    onBack: () -> Unit,
    viewModel: LandlordProfileViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(propertyId) {
        viewModel.loadLandlord(propertyId)
    }

    val profile = uiState.profile ?: LandlordProfile()

    Scaffold(
        containerColor = Color(0xFFFDFDFD),
        topBar = {
            TopAppBar(
                title = {},
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 22.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // ── 1. Landlord Header ──────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF2F2F2)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!profile.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profile.avatarUrl,
                                contentDescription = profile.name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = profile.name,
                                tint = Color(0xFF7A7A7A),
                                modifier = Modifier.size(46.dp),
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF1E1E1E),
                                ),
                            )
                            if (profile.isVerified) {
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Verified Landlord",
                                    tint = Color(0xFF27AE60),
                                    modifier = Modifier.size(17.dp),
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = profile.memberSince,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = Color(0xFF7A7A7A),
                            ),
                        )

                        Spacer(Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating",
                                tint = GoldenMarigold,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = profile.overallRating.toString(),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1E1E),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${profile.reviewCount} Reviews",
                                fontSize = 12.sp,
                                color = Color(0xFF7A7A7A),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
            }

            // ── 2. Rating Overview Card ─────────────────────────────────────
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = White,
                    border = BorderStroke(1.dp, Color(0xFFECECEC)),
                    shadowElevation = 2.dp,
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Rating overview",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1E1E1E),
                            ),
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Left Column (Big number + 5 stars)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(0.9f),
                            ) {
                                Text(
                                    text = profile.overallRating.toString(),
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1E1E),
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    val ratingInt = profile.overallRating.toInt()
                                    for (i in 1..5) {
                                        Icon(
                                            imageVector = if (i <= ratingInt) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                            contentDescription = null,
                                            tint = GoldenMarigold,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            // Right Column (5 Star breakdown progress bars)
                            Column(
                                modifier = Modifier.weight(1.5f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                val total = profile.reviewCount.coerceAtLeast(1)
                                for (stars in 5 downTo 1) {
                                    val count = profile.starCounts[stars] ?: 0
                                    val progress = count.toFloat() / total

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text(
                                            text = "$stars",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF555555),
                                        )
                                        Spacer(Modifier.width(3.dp))
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = GoldenMarigold,
                                            modifier = Modifier.size(11.dp),
                                        )
                                        Spacer(Modifier.width(6.dp))

                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = Color(0xFF6B5808),
                                            trackColor = Color(0xFFE8E8E8),
                                        )

                                        Spacer(Modifier.width(8.dp))

                                        Text(
                                            text = "$count",
                                            fontSize = 11.sp,
                                            color = Color(0xFF7A7A7A),
                                            modifier = Modifier.width(18.dp),
                                            textAlign = TextAlign.End,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // ── 3. Reviews Section Header ───────────────────────────────────
            item {
                var sortMenuExpanded by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Reviews",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1E1E1E),
                        ),
                    )

                    Box {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = White,
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { sortMenuExpanded = true },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = uiState.selectedSort,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF555555),
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Sort options",
                                    tint = Color(0xFF555555),
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false },
                        ) {
                            listOf("Most Recent", "Highest Rating", "Lowest Rating").forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt, fontSize = 13.sp) },
                                    onClick = {
                                        viewModel.setSort(opt)
                                        sortMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
            }

            // ── 4. Reviews List ─────────────────────────────────────────────
            items(uiState.sortedReviews, key = { it.id }) { review ->
                ReviewItemRow(review = review)
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                Spacer(Modifier.height(14.dp))
            }

            // ── 5. Bottom Action: Rate Landlord Button ───────────────────────
            item {
                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { viewModel.openRateSheet() },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.2.dp, GoldenMarigold),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFBF6B04),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Text(
                        text = "Rate Landlord",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = Color(0xFFBF6B04),
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = Color(0xFF8C929D),
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "Your review helps other renters make informed decisions.",
                        fontSize = 11.sp,
                        color = Color(0xFF7A7A7A),
                    )
                }
            }
        }
    }

    // ── 6. Pull-up Rate Landlord Bottom Sheet ──────────────────────────
    if (uiState.showRateSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { viewModel.closeRateSheet() },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = White,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            RateLandlordBottomSheetContent(
                landlordName = profile.name,
                userRating = uiState.userRating,
                reviewText = uiState.reviewText,
                isSubmitting = uiState.isSubmitting,
                onRatingChange = { viewModel.setRating(it) },
                onTextChange = { viewModel.setReviewText(it) },
                onClose = { viewModel.closeRateSheet() },
                onSubmit = { viewModel.submitReview() },
            )
        }
    }
}

@Composable
private fun ReviewItemRow(
    review: LandlordReview,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Reviewer Avatar
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8E8E8)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Color(0xFF7A7A7A),
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.reviewerName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1E1E1E),
                    ),
                )
                Text(
                    text = review.rentalPeriod,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        color = Color(0xFF888888),
                    ),
                )
            }

            // 5 Stars row
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (i in 1..5) {
                    Icon(
                        imageVector = if (i <= review.rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = if (i <= review.rating) GoldenMarigold else Color(0xFFDCDCDC),
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }

        if (review.comment.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    color = Color(0xFF333333),
                    lineHeight = 18.5.sp,
                ),
            )
        }
    }
}

@Composable
private fun RateLandlordBottomSheetContent(
    landlordName: String,
    userRating: Int,
    reviewText: String,
    isSubmitting: Boolean,
    onRatingChange: (Int) -> Unit,
    onTextChange: (String) -> Unit,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .imePadding(),
    ) {
        // Header with Close (X) button
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = Color(0xFF1E1E1E),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Rate $landlordName",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E1E1E),
                    ),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Share your experience as a renter",
                    fontSize = 12.5.sp,
                    color = Color(0xFFBF6B04),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Experience Prompt
        Text(
            text = "How was your experience?",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF1E1E1E),
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(14.dp))

        // Interactive 5 Large Stars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (i in 1..5) {
                IconButton(
                    onClick = { onRatingChange(i) },
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(
                        imageVector = if (i <= userRating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "$i Stars",
                        tint = GoldenMarigold,
                        modifier = Modifier.size(34.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Review Text Field
        Text(
            text = "Write your review (optional)",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = Color(0xFF1E1E1E),
            ),
        )

        Spacer(Modifier.height(8.dp))

        BasicTextField(
            value = reviewText,
            onValueChange = onTextChange,
            textStyle = TextStyle(
                fontSize = 13.5.sp,
                color = Color(0xFF1E1E1E),
                lineHeight = 19.sp,
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(White, RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, Color(0xFFDCDCDC)), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.TopStart,
                ) {
                    if (reviewText.isEmpty()) {
                        Text(
                            text = "Share details about your experience with $landlordName...",
                            fontSize = 12.5.sp,
                            color = Color(0xFF8C929D),
                            lineHeight = 18.sp,
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        // Submit Review Button
        Button(
            onClick = onSubmit,
            enabled = !isSubmitting,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldenMarigold,
                contentColor = White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Text(
                    text = "Submit Review",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun LandlordProfileScreenPreview() {
    MobInTheme {
        LandlordProfileScreen(
            propertyId = "1",
            onBack = {},
        )
    }
}
