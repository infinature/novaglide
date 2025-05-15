package com.sdu.novaglide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sdu.novaglide.data.repository.FakeNewsRepositoryImpl
import com.sdu.novaglide.domain.model.News
import com.sdu.novaglide.domain.usecase.GetNewsByCategoryUseCase
import com.sdu.novaglide.domain.usecase.SearchNewsUseCase
import com.sdu.novaglide.ui.features.home.HomeScreen
import com.sdu.novaglide.ui.features.home.HomeViewModel
import com.sdu.novaglide.ui.features.home.NewsDetailScreen
import com.sdu.novaglide.ui.features.qna.QnaScreen
import com.sdu.novaglide.ui.features.profile.ProfileScreen
import com.sdu.novaglide.ui.theme.NovaGlideTheme
import java.util.UUID

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
    
    // 创建模拟仓库和用例
    val newsRepository = remember { FakeNewsRepositoryImpl() }
    val getNewsByCategoryUseCase = remember { GetNewsByCategoryUseCase(newsRepository) }
    val searchNewsUseCase = remember { SearchNewsUseCase(newsRepository) }
    
    // 创建 ViewModel 工厂
    val viewModelFactory = remember {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                    return HomeViewModel(getNewsByCategoryUseCase, searchNewsUseCase) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
    
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
            
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToQna = { navController.navigate("qna") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToNewsDetail = { newsId -> 
                    navController.navigate("news_detail/$newsId")
                }
            )
        }
        
        composable(
            route = "news_detail/{newsId}",
            arguments = listOf(navArgument("newsId") { type = NavType.StringType })
        ) { backStackEntry ->
            val newsId = backStackEntry.arguments?.getString("newsId") ?: ""
            
            // 从 ViewModel 获取新闻列表，并查找对应 ID 的新闻
            val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
            val news = homeViewModel.state.value.news.find { it.id == newsId }
            
            NewsDetailScreen(
                news = news,
                onNavigateBack = { navController.popBackStack() }
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