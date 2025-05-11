package com.sdu.novaglide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sdu.novaglide.ui.features.home.HomeScreen
import com.sdu.novaglide.ui.features.qna.QnaScreen
import com.sdu.novaglide.ui.features.profile.ProfileScreen
import com.sdu.novaglide.ui.theme.NovaGlideTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NovaGlideTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NovaGlideApp()
                }
            }
        }
    }
}

@Composable
fun NovaGlideApp() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToQna = { navController.navigate("qna") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }
        composable("qna") {
            QnaScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("profile") {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
} 