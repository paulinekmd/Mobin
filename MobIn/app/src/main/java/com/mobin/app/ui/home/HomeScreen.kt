package com.mobin.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mobin.app.ui.theme.MobInTheme
import com.mobin.app.ui.theme.PeachSand
import com.mobin.app.ui.theme.White

@Composable
fun HomeScreen() {
    HomeContent()
}

@Composable
private fun HomeContent() {
    Box(
        modifier = Modifier.fillMaxSize().background(White),
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun HomePreview() {
    MobInTheme { HomeContent() }
}
