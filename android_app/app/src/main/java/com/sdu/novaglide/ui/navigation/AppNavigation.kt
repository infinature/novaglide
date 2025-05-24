package com.sdu.novaglide.ui.navigation

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sdu.novaglide.core.util.ApiKeyStore
import com.sdu.novaglide.data.repository.ChatRepository
import com.sdu.novaglide.data.repository.FakeNewsRepositoryImpl
import com.sdu.novaglide.domain.usecase.GetNewsByCategoryUseCase
import com.sdu.novaglide.domain.usecase.SearchNewsUseCase
import com.sdu.novaglide.ui.features.home.HomeScreen as NewsListScreen
import com.sdu.novaglide.ui.features.home.HomeViewModel
import com.sdu.novaglide.ui.features.home.NewsDetailScreen
import com.sdu.novaglide.ui.features.qna.ApiSettingsScreen
import com.sdu.novaglide.ui.features.qna.ApiSettingsViewModel
import com.sdu.novaglide.ui.features.qna.QnaScreen
import com.sdu.novaglide.ui.features.qna.QnaViewModel
import com.sdu.novaglide.ui.features.profile.ProfileScreen

// Imports for TemporaryScreen
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme // For TemporaryScreen text style

private const val TAG = "AppNavigation"

object AppRoute {
    // FEATURES_HUB is removed
    const val QNA = "qna"
    const val API_SETTINGS = "api_settings"
    const val PROFILE = "profile"
    const val NEWS_LIST = "news_list" // This is the new main entry point
    const val NEWS_DETAIL = "news_detail/{newsId}"
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppRoute.NEWS_LIST, 
    chatRepository: ChatRepository,
    apiKeyStore: ApiKeyStore
) {
    val qnaViewModel = remember { QnaViewModel(chatRepository, apiKeyStore) }
    val apiSettingsViewModel = remember { ApiSettingsViewModel(apiKeyStore) }

    val newsRepository = remember { FakeNewsRepositoryImpl() }
    val getNewsByCategoryUseCase = remember { GetNewsByCategoryUseCase(newsRepository) }
    val searchNewsUseCase = remember { SearchNewsUseCase(newsRepository) }
    val homeViewModelFactory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                    return HomeViewModel(getNewsByCategoryUseCase, searchNewsUseCase) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class for HomeViewModel")
            }
        }
    }
    val homeViewModel: HomeViewModel = viewModel(factory = homeViewModelFactory)
    
    LaunchedEffect(key1 = Unit) {
        Log.d(TAG, "AppNavigation 初始化完成，开始导航至: $startDestination")
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(AppRoute.NEWS_LIST) {
            NewsListScreen(
                viewModel = homeViewModel,
                onNavigateToQna = { navController.navigate(AppRoute.QNA) },
                onNavigateToProfile = { navController.navigate(AppRoute.PROFILE) },
                onNavigateToNewsDetail = { newsId ->
                    navController.navigate("${AppRoute.NEWS_DETAIL.substringBefore('{')}$newsId")
                }
            )
        }
        
        composable(AppRoute.QNA) {
            QnaScreen(
                onNavigateBack = { navController.popBackStack() }, 
                onNavigateToSettings = { navController.navigate(AppRoute.API_SETTINGS) },
                viewModel = qnaViewModel
            )
        }
        
        composable(AppRoute.API_SETTINGS) {
            ApiSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = apiSettingsViewModel
            )
        }
        
        composable(AppRoute.PROFILE) {
            ProfileScreen(
                onNavigateToNewsList = { navController.navigate(AppRoute.NEWS_LIST) {
                    launchSingleTop = true 
                    }
                },
                onNavigateToQna = { navController.navigate(AppRoute.QNA) { launchSingleTop = true } }
            )
        }

        composable(
            route = AppRoute.NEWS_DETAIL,
            arguments = listOf(navArgument("newsId") { type = NavType.StringType })
        ) { backStackEntry ->
            val newsId = backStackEntry.arguments?.getString("newsId") ?: ""
            val news = homeViewModel.state.value.news.find { it.id == newsId }
            
            NewsDetailScreen(
                news = news,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// FeaturesHubScreen and HomeFeatureButton are now removed.

@Composable
fun TemporaryScreen(title: String, onNavigateBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateBack) {
                Text("返回")
            }
        }
    }
} 