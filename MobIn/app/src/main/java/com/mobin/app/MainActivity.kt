package com.mobin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mobin.app.ui.navigation.MobInNavGraph
import com.mobin.app.ui.theme.MobInTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobInTheme {
                MobInNavGraph()
            }
        }
    }
}