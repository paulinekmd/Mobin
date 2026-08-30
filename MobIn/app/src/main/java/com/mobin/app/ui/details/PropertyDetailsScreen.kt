package com.mobin.app.ui.details

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun PropertyDetailsScreen(
    propertyId: String,
    onBack: () -> Unit,
    onMessageOwner: () -> Unit = {},
    viewModel: PropertyDetailsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(propertyId) {
        viewModel.loadProperty(propertyId)
    }

    val property = uiState.property

    if (property != null) {
        PropertyDetailsContent(
            property = property,
            isSaved = uiState.isSaved,
            selectedImageIndex = uiState.selectedImageIndex,
            onSelectImage = { viewModel.selectImage(it) },
            onToggleSave = { viewModel.toggleSave() },
            onShare = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Check out ${property.title} on Mob'in! Location: ${property.location} - Price: ₱${property.price.toInt()}/month")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Property"))
            },
            onMessageOwner = onMessageOwner,
            onBack = onBack,
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = GoldenMarigold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun PropertyDetailsContent(
    property: Property,
    isSaved: Boolean,
    selectedImageIndex: Int,
    onSelectImage: (Int) -> Unit,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    onMessageOwner: () -> Unit,
    onBack: () -> Unit,
) {
    val formattedPrice = NumberFormat.getNumberInstance(Locale.US).format(property.price.toInt())

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
                            text = "Property Details",
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
                            tint = Color(0xFFBF6B04),
                        )
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFDFDFD)),
            )
        },
        bottomBar = {
            Surface(
                color = White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                ) {
                    Button(
                        onClick = onMessageOwner,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldenMarigold,
                            contentColor = White,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(50),
                                ambientColor = Color(0x33FBB81F),
                                spotColor = Color(0x66FBB81F),
                            ),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubble,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Message Now",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = White,
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 12.dp),
        ) {
            // ── Main Hero Gallery Stack ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(20.dp))
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
                        modifier = Modifier.size(54.dp),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Thumbnails Row + Save / Share Actions ──────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 4 Thumbnail previews
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(4) { idx ->
                        val isCurrent = selectedImageIndex == idx
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEBEBEB))
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) GoldenMarigold else Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(10.dp),
                                )
                                .clickable { onSelectImage(idx) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Image,
                                contentDescription = null,
                                tint = Color(0xFF9E9E9E),
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }

                // Save & Share Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable(onClick = onToggleSave),
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) GoldenMarigold else Color(0xFFBF6B04),
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Save",
                            fontSize = 11.sp,
                            color = Color(0xFF555555),
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable(onClick = onShare),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = Color(0xFF4A4A4A),
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Share",
                            fontSize = 11.sp,
                            color = Color(0xFF555555),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Verified Property Badge ────────────────────────────────────────
            if (property.isVerified) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.wrapContentSize(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Verified Property",
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Title & Rating ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = property.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = ComfortaaFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color(0xFF1E1E1E),
                    ),
                    modifier = Modifier.weight(1f),
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFFFF7EA),
                    border = BorderStroke(1.dp, Color(0xFFFEECCB)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = GoldenMarigold,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = property.rating.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // ── Location ───────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF7A7A7A),
                    modifier = Modifier.size(15.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = property.location,
                    fontSize = 13.sp,
                    color = Color(0xFF7A7A7A),
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Category & Availability Tags ───────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF2ECE6),
                ) {
                    Text(
                        text = property.category,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4A4F57),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF2ECE6),
                ) {
                    Text(
                        text = "${property.availableBeds} beds available",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4A4F57),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Rental Price Section ───────────────────────────────────────────
            Text(
                text = "Rental Price",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E1E1E),
                ),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "₱$formattedPrice / month",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                ),
            )

            Spacer(Modifier.height(24.dp))

            // ── Overview Section ───────────────────────────────────────────────
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E1E1E),
                ),
            )
            Spacer(Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = White,
                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
            ) {
                Text(
                    text = property.overview,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        lineHeight = 21.sp,
                        color = Color(0xFF555555),
                    ),
                    modifier = Modifier.padding(16.dp),
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Amenities Section ──────────────────────────────────────────────
            Text(
                text = "Amenities",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E1E1E),
                ),
            )
            Spacer(Modifier.height(10.dp))
            val amenityChunks = property.amenities.chunked(2)
            amenityChunks.forEach { chunk ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    chunk.forEach { amenity ->
                        Surface(
                            modifier = Modifier.weight(1f, fill = false),
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFF2ECE6),
                        ) {
                            Text(
                                text = amenity,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4A4F57),
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(24.dp))

            // ── Location Map Section ───────────────────────────────────────────
            Text(
                text = "Location",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E1E1E),
                ),
            )
            Spacer(Modifier.height(10.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFE9F1F7),
                border = BorderStroke(1.dp, Color(0xFFD6E4EF)),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Map,
                            contentDescription = "Map location",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(36.dp),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = property.location,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1976D2),
                                fontSize = 13.sp,
                            ),
                        )
                        Text(
                            text = "Near SWU PHINMA & Taboan Market",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF5C6B73),
                                fontSize = 11.sp,
                            ),
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Property Owner Section ─────────────────────────────────────────
            Text(
                text = "Property Owner",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E1E1E),
                ),
            )
            Spacer(Modifier.height(10.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = White,
                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B3E43)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!property.ownerAvatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = property.ownerAvatarUrl,
                                contentDescription = property.ownerName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = property.ownerName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = Color(0xFF1E1E1E),
                                ),
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Verified Owner",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(15.dp),
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Member since ${property.ownerJoined}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = Color(0xFF888888),
                            ),
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun PropertyDetailsPreview() {
    MobInTheme {
        PropertyDetailsContent(
            property = Property(
                id = "1",
                title = "Casa Urgello",
                category = "Boarding House",
                location = "Sambag 2, Cebu City",
                price = 3500.0,
                availableBeds = 3,
                rating = 4.5,
                isVerified = true,
                overview = "Spacious and comfortable boarding house located in a peaceful and accessible area close to universities, convenience stores, and transportation hubs.",
                amenities = listOf("Free WiFi", "CCTV", "Free electricity and water", "Study Area", "Laundry Area"),
                ownerName = "Mary Ann Dasalo",
                ownerJoined = "March 2023",
            ),
            isSaved = false,
            selectedImageIndex = 0,
            onSelectImage = {},
            onToggleSave = {},
            onShare = {},
            onMessageOwner = {},
            onBack = {},
        )
    }
}
