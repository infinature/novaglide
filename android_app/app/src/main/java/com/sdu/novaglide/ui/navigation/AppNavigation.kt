package com.sdu.novaglide.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sdu.novaglide.core.util.ApiKeyStore
import com.sdu.novaglide.data.repository.ChatRepository
import com.sdu.novaglide.ui.features.qna.ApiSettingsScreen
import com.sdu.novaglide.ui.features.qna.ApiSettingsViewModel
import com.sdu.novaglide.ui.features.qna.QnaScreen
import com.sdu.novaglide.ui.features.qna.QnaViewModel
import com.sdu.novaglide.ui.features.home.HomeScreen // 确保 HomeScreen 导入正确
import com.sdu.novaglide.ui.features.profile.ProfileScreen
import com.sdu.novaglide.ui.features.profile.EditUserInfoScreen
import com.sdu.novaglide.ui.features.profile.BrowsingHistoryScreen
import com.sdu.novaglide.ui.features.profile.UserInfoScreen
import com.sdu.novaglide.ui.features.profile.UserInfoViewModel
import com.sdu.novaglide.ui.features.profile.BrowsingHistoryViewModel
import com.sdu.novaglide.ui.features.profile.FavoriteArticleViewModel // 导入
import com.sdu.novaglide.ui.features.profile.FavoriteArticlesScreen // 导入
import androidx.compose.ui.platform.LocalContext
import com.sdu.novaglide.data.repository.UserRepositoryImpl
import com.sdu.novaglide.data.repository.UserRepository
import com.sdu.novaglide.NovaGlideApplication
import com.sdu.novaglide.ui.features.auth.LoginScreen // 确保 LoginScreen 导入正确
import androidx.navigation.NavGraph.Companion.findStartDestination // 确保 findStartDestination 导入正确
import com.sdu.novaglide.ui.features.auth.RegisterScreen // 确保 RegisterScreen 导入正确
import com.sdu.novaglide.ui.features.home.NewsDetailScreen // 确保 NewsDetailScreen 导入正确
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
    const val FAVORITE_ARTICLES = "favorite_articles" // 新增收藏路由
    const val NEWS_DETAIL = "news_detail"
    const val LOGIN = "login"
    const val REGISTER = "register"
}

/**
 * 应用导航组件
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppRoute.LOGIN,
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
        Log.d(TAG_NAV, "AppNavigation 初始化完成，开始导航至: $startDestination")
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
                    navController.navigate("${AppRoute.NEWS_DETAIL}/$documentId")
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserInfo = { navController.navigate(AppRoute.USER_INFO) },
                viewModel = actualUserInfoViewModel,
                onNavigateToHome = {
                    navController.navigate(AppRoute.HOME) {
                        launchSingleTop = true
                        popUpTo(navController.graph.findStartDestination().id) { // 确保 findStartDestination 正确调用
                            saveState = true
                        }
                    }
                },
                onNavigateToChat = {
                    navController.navigate(AppRoute.QNA) {
                        popUpTo(navController.graph.findStartDestination().id) { // 确保 findStartDestination 正确调用
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToLogout = {
                    navController.navigate(AppRoute.LOGIN) {
                        popUpTo(navController.graph.findStartDestination().id) { // 确保 findStartDestination 正确调用
                            inclusive = true
                        }
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
                    navController.navigate("${AppRoute.NEWS_DETAIL}/$documentId") // 确保导航逻辑完整
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
                    navController.navigate("${AppRoute.NEWS_DETAIL}/$documentId")
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
        composable("${AppRoute.NEWS_DETAIL}/{documentId}") { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId")
            if (documentId != null) {
                NewsDetailScreen(
                    documentId = documentId,
                    onBack = { navController.popBackStack() }
                )
            } else {
                // Handle error: documentId is null
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("错误：未提供资讯ID")
                }
            }
        }

        // 登录页路由
        composable(AppRoute.LOGIN) {
            LoginScreen( // 使用正确的 LoginScreen Composable 名称
                viewModel = actualUserInfoViewModel,
                onNavigateToHome = {
                    navController.navigate(AppRoute.HOME) {
                        popUpTo(AppRoute.LOGIN) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(AppRoute.REGISTER)
                }
            )
        }

        // 注册页路由
        composable(AppRoute.REGISTER) {
            RegisterScreen( // 使用正确的 RegisterScreen Composable 名称
                viewModel = actualUserInfoViewModel,
                onNavigateToLogin = {
                    navController.navigate(AppRoute.LOGIN) {
                        popUpTo(AppRoute.REGISTER) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
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
