package com.kashmir.bislei

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.kashmir.bislei.navigation.NavigationGraph
import com.kashmir.bislei.ui.theme.BisleiTheme

// Development Flow of the App:->

     /*
     Phase 1: Authentication (Done)
     Phase 2: Fishing Spots
     Phase 3: Fish Uploads (Feed)
     Phase 4: Fish Species Info
     Phase 5: Bonus Features
         -> Fish identification using ML
         -> Ranking system (based on likes or posts)
         -> Admin panel for approving content
      */


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BisleiTheme {
                val navController = rememberNavController()

                NavigationGraph(navController = navController)
            }
        }
    }
}
