package com.sdu.novaglide.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sdu.novaglide.core.util.ApiKeyStore
import com.sdu.novaglide.data.repository.ChatRepository
import com.sdu.novaglide.ui.features.qna.ApiSettingsScreen // Corrected import path
import com.sdu.novaglide.ui.features.qna.ApiSettingsViewModel // Corrected import path
import com.sdu.novaglide.ui.features.qna.QnaScreen
import com.sdu.novaglide.ui.features.qna.QnaViewModel
import com.sdu.novaglide.ui.features.home.HomeScreen
import com.sdu.novaglide.ui.features.profile.ProfileScreen
import com.sdu.novaglide.ui.features.profile.EditUserInfoScreen
import com.sdu.novaglide.ui.features.profile.BrowsingHistoryScreen
import com.sdu.novaglide.ui.features.profile.UserInfoScreen
import com.sdu.novaglide.ui.features.profile.UserInfoViewModel
import com.sdu.novaglide.ui.features.profile.BrowsingHistoryViewModel
import com.sdu.novaglide.ui.features.profile.FavoriteArticleViewModel
import com.sdu.novaglide.ui.features.profile.FavoriteArticlesScreen
import com.sdu.novaglide.NovaGlideApplication
import com.sdu.novaglide.ui.features.auth.LoginScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.sdu.novaglide.ui.features.auth.RegisterScreen
import com.sdu.novaglide.ui.features.home.NewsDetailScreen
import com.sdu.novaglide.ui.features.home.NewsViewModel

private const val TAG_NAV = "AppNavigation"

/**
 * 应用导航路由
 */
object AppRoute {
    const val HOME = "home"
    const val QNA = "qna"
    const val API_SETTINGS = "api_settings"
    const val PROFILE = "profile"
    const val NEWS = "news"
    const val USER_INFO = "user_info"
    const val EDIT_USER_INFO = "edit_user_info"
    const val BROWSING_HISTORY = "browsing_history"
    const val FAVORITE_ARTICLES = "favorite_articles"
    const val LOGIN = "login" // <-- 添加 LOGIN 路由
    const val REGISTER = "register" // <-- 添加 REGISTER 路由
    const val NEWS_DETAIL = "news_detail" // <-- 添加 NEWS_DETAIL 路由
} // <-- 移除末尾的 */

 /**
 * 应用导航组件
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String, // Changed: No default, will be provided by MainActivity
    chatRepository: ChatRepository,
    apiKeyStore: ApiKeyStore
) {
    val context = LocalContext.current
    val application = context.applicationContext as NovaGlideApplication

    // 重新添加 qnaViewModel 和 apiSettingsViewModel 的实例化
    val qnaViewModel = remember { QnaViewModel(chatRepository, apiKeyStore) }
    val apiSettingsViewModel = remember { ApiSettingsViewModel(apiKeyStore) }

    val actualUserInfoViewModel: UserInfoViewModel = remember {
         UserInfoViewModel.Factory(application.userDao).create(UserInfoViewModel::class.java)
    }

    val browsingHistoryViewModel: BrowsingHistoryViewModel = remember {
        BrowsingHistoryViewModel.Factory(application.browsingHistoryRepository).create(BrowsingHistoryViewModel::class.java)
    }

    // FavoriteArticleViewModel 实例化
    val favoriteArticleViewModel: FavoriteArticleViewModel = remember {
        FavoriteArticleViewModel.Factory(application.favoriteArticleRepository).create(FavoriteArticleViewModel::class.java)
    }

    // NewsViewModel 单例
    val newsViewModel: NewsViewModel = remember { NewsViewModel() }

    LaunchedEffect(key1 = Unit) {
        Log.d(TAG_NAV, "AppNavigation 初始化完成，NavHost 将使用的 startDestination: $startDestination")
    }

    NavHost(
        navController = navController,
        startDestination = startDestination, 
        modifier = modifier
    ) {
        // 首页
        composable(AppRoute.HOME) {
            HomeScreen(
                userInfoViewModel = actualUserInfoViewModel,
                browsingHistoryViewModel = browsingHistoryViewModel,
                newsViewModel = newsViewModel,
                onNavigateToQna = { navController.navigate(AppRoute.QNA) },
                onNavigateToProfile = { navController.navigate(AppRoute.PROFILE) },
                onNavigateToNewsDetail = { documentId -> 
                    navController.navigate("${AppRoute.NEWS_DETAIL}/$documentId") // 使用 AppRoute.NEWS_DETAIL
                }
            )
        }

        // 智能问答
        composable(AppRoute.QNA) {
            QnaScreen( // 使用正确的 QnaScreen Composable 名称
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(AppRoute.API_SETTINGS) },
                viewModel = qnaViewModel // 传递 qnaViewModel
            )
        }

        // API设置
        composable(AppRoute.API_SETTINGS) {
            ApiSettingsScreen( // 使用正确的 ApiSettingsScreen Composable 名称
                onNavigateBack = { navController.popBackStack() },
                viewModel = apiSettingsViewModel // 传递 apiSettingsViewModel
            )
        }

        // 个人资料主页
        composable(AppRoute.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }, // Or handle differently if it's a tab root
                onNavigateToUserInfo = { navController.navigate(AppRoute.USER_INFO) },
                viewModel = actualUserInfoViewModel,
                onNavigateToHome = {
                    navController.navigate(AppRoute.HOME) {
                        launchSingleTop = true
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    }
                },
                onNavigateToChat = {
                    navController.navigate(AppRoute.QNA) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToLogout = {
                    Log.d(TAG_NAV, "Logout: Navigating to LOGIN and clearing up to graph root or HOME.")
                    navController.navigate(AppRoute.LOGIN) {
                        // 尝试 popUpTo 整个图的 ID
                        popUpTo(navController.graph.id) { // navController.graph.id 是整个 NavHost 图的 ID
                            inclusive = true 
                        }
                        // 或者，如果 HOME 是确定的登录后根屏幕:
                        // popUpTo(AppRoute.HOME) {
                        //     inclusive = true
                        // }
                        launchSingleTop = true 
                    }
                },
                onNavigateToEditUserInfo = { navController.navigate(AppRoute.EDIT_USER_INFO) },
                onNavigateToBrowsingHistory = { navController.navigate(AppRoute.BROWSING_HISTORY) },
                onNavigateToFavorites = { navController.navigate(AppRoute.FAVORITE_ARTICLES) } // 添加导航
            )
        }

        // 用户信息详情页
        composable(AppRoute.USER_INFO) {
            UserInfoScreen(
                viewModel = actualUserInfoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 编辑用户信息页
        composable(AppRoute.EDIT_USER_INFO) {
            EditUserInfoScreen(
                viewModel = actualUserInfoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 浏览历史页
        composable(AppRoute.BROWSING_HISTORY) {
            BrowsingHistoryScreen(
                userInfoViewModel = actualUserInfoViewModel,
                browsingHistoryViewModel = browsingHistoryViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNewsDetail = { documentId ->
                    navController.navigate("${AppRoute.NEWS_DETAIL}/$documentId") // 使用 AppRoute.NEWS_DETAIL
                }
            )
        }

        // 新增：收藏文章页
        composable(AppRoute.FAVORITE_ARTICLES) {
            FavoriteArticlesScreen(
                userInfoViewModel = actualUserInfoViewModel,
                favoriteArticleViewModel = favoriteArticleViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNewsDetail = { documentId ->
                    navController.navigate("${AppRoute.NEWS_DETAIL}/$documentId") // 使用 AppRoute.NEWS_DETAIL
                }
            )
        }
        
        // 新闻 (临时屏幕)
        composable(AppRoute.NEWS) {
            // 假设 TemporaryScreen 是一个已定义的 Composable
            // TemporaryScreen("新闻页面", onNavigateBack = { navController.popBackStack() })
            // 如果 TemporaryScreen 不存在，可以暂时用一个简单的 Text 替代或注释掉
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("新闻页面 (临时)")
            }
        }

        // 资讯详情页路由
        composable("${AppRoute.NEWS_DETAIL}/{documentId}") { backStackEntry -> // 使用 AppRoute.NEWS_DETAIL
            val documentId = backStackEntry.arguments?.getString("documentId")
            NewsDetailScreen(
                newsId = documentId, 
                onNavigateBack = { navController.popBackStack() },
                userInfoViewModel = actualUserInfoViewModel,
                favoriteArticleViewModel = favoriteArticleViewModel,
                newsViewModel = newsViewModel 
            )
        }

        // 登录页路由
        composable(AppRoute.LOGIN) { 
            LoginScreen(
                viewModel = actualUserInfoViewModel,
                onNavigateToHome = {
                    navController.navigate(AppRoute.HOME) {
                        popUpTo(AppRoute.LOGIN) { inclusive = true } 
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = { navController.navigate(AppRoute.REGISTER) } // 使用 AppRoute.REGISTER
            )
        }

        // 注册页路由
        composable(AppRoute.REGISTER) { // 使用 AppRoute.REGISTER
            RegisterScreen(
                viewModel = actualUserInfoViewModel,
                onNavigateToLogin = {
                    navController.navigate(AppRoute.LOGIN) { // 使用 AppRoute.LOGIN
                        popUpTo(AppRoute.REGISTER) { inclusive = true } // 使用 AppRoute.REGISTER
                        launchSingleTop = true
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// 如果 TemporaryScreen 不存在，可以定义一个简单的占位符
/*
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
*/