package com.stillness.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.stillness.app.ui.screens.TimerScreen
import com.stillness.app.ui.theme.DarkBackground
import com.stillness.app.ui.theme.StillnessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            StillnessTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    TimerScreen(
                        onNavigateToSettings = {
                            // TODO: Navigate to settings
                        }
                    )
                }
            }
        }
    }
}
