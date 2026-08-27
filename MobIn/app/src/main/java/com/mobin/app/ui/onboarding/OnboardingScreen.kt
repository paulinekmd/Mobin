package com.mobin.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobin.app.ui.components.MobInButton
import com.mobin.app.ui.theme.*
import kotlinx.coroutines.launch

private data class OnboardingPage(val title: String, val subtitle: String, val description: String)

private val pages = listOf(
    OnboardingPage("Find a Place That Fits You",    "Discover your home",     "Search for dormitories, apartments, and boarding houses in one place."),
    OnboardingPage("Search by Your Needs",           "Filter your way",        "Filter by location, price, property type, and availability."),
    OnboardingPage("Compare. Choose. Connect.",       "Make the right choice",  "Compare properties side-by-side and manage your wishlists."),
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit, viewModel: OnboardingViewModel = viewModel()) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(PeachSand)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            OnboardingPageContent(
                page = pages[page],
                isLastPage = page == pages.lastIndex,
                onNext = {
                    if (page < pages.lastIndex) scope.launch { pagerState.animateScrollToPage(page + 1) }
                    else viewModel.completeOnboarding(onFinished)
                },
            )
        }

        // Dot indicators
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 136.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(pages.size) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (selected) 24.dp else 8.dp, 8.dp)
                        .clip(CircleShape)
                        .background(if (selected) GoldenMarigold else MediumGray),
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage, isLastPage: Boolean, onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Card(
            modifier = Modifier.fillMaxWidth().height(280.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = GoldenMarigold),
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = page.subtitle, style = MaterialTheme.typography.titleLarge, color = White)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        Text(text = page.title, style = MaterialTheme.typography.headlineSmall, color = ChestnutBark, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = page.description, style = MaterialTheme.typography.bodyMedium, color = OliveBronze, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.weight(1f))
        MobInButton(text = if (isLastPage) "Get Started" else "Next", onClick = onNext, modifier = Modifier.padding(bottom = 60.dp))
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun OnboardingPreview() {
    MobInTheme {
        OnboardingPageContent(
            page = pages[0],
            isLastPage = false,
            onNext = {},
        )
    }
}
