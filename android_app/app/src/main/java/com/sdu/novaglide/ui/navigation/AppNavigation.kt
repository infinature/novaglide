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
import com.sdu.novaglide.ui.features.home.HomeScreen
import com.sdu.novaglide.ui.features.profile.ProfileScreen
import com.sdu.novaglide.ui.features.profile.EditUserInfoScreen // 导入新的编辑屏幕
import com.sdu.novaglide.ui.features.profile.UserInfoScreen // 确保导入
import com.sdu.novaglide.ui.features.profile.UserInfoViewModel
import androidx.compose.ui.platform.LocalContext
// import com.sdu.novaglide.core.database.AppDatabase // AppDatabase 通常不直接在此使用
import com.sdu.novaglide.data.repository.UserRepositoryImpl
import com.sdu.novaglide.data.repository.UserRepository
import com.sdu.novaglide.NovaGlideApplication
import com.sdu.novaglide.ui.features.auth.LoginScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.sdu.novaglide.ui.features.auth.RegisterScreen

private const val TAG_NAV = "AppNavigation" // 修改TAG名称

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
    const val EDIT_USER_INFO = "edit_user_info" // 新增编辑用户信息路由
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
    startDestination: String = AppRoute.LOGIN, // 确保这里是 LOGIN
    chatRepository: ChatRepository,
    apiKeyStore: ApiKeyStore
) {
    val context = LocalContext.current
    val qnaViewModel = remember { QnaViewModel(chatRepository, apiKeyStore) }
    val apiSettingsViewModel = remember { ApiSettingsViewModel(apiKeyStore) }
    
    val userInfoViewModel = remember {
        val application = context.applicationContext as NovaGlideApplication
        val userDao = application.userDao
        val userRepository: UserRepository = UserRepositoryImpl(userDao)
        UserInfoViewModel(userRepository)
    } 
    
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
            com.sdu.novaglide.ui.features.home.HomeScreen(
                onNavigateToQna = { navController.navigate(AppRoute.QNA) },
                onNavigateToProfile = { navController.navigate(AppRoute.PROFILE) },
                onNavigateToNewsDetail = { newsId ->
                    navController.navigate("${AppRoute.NEWS_DETAIL}/$newsId")
                }
            )
        }
        
        // 智能问答
        composable(AppRoute.QNA) {
            QnaScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(AppRoute.API_SETTINGS) },
                viewModel = qnaViewModel
            )
        }
        
        // API设置
        composable(AppRoute.API_SETTINGS) {
            ApiSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = apiSettingsViewModel
            )
        }
        
        // 个人资料主页
        composable(AppRoute.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserInfo = { navController.navigate(AppRoute.USER_INFO) },
                viewModel = userInfoViewModel, 
                onNavigateToHome = {
                    navController.navigate(AppRoute.HOME) {
                        launchSingleTop = true
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
                },
                onNavigateToChat = {
                    navController.navigate(AppRoute.QNA) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToLogout = {
                    navController.navigate(AppRoute.LOGIN) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true 
                    }
                },
                onNavigateToEditUserInfo = { navController.navigate(AppRoute.EDIT_USER_INFO) } // 添加导航
            )
        }

        // 用户信息详情页
        composable(AppRoute.USER_INFO) {
            UserInfoScreen(
                viewModel = userInfoViewModel, // 传递共享的 ViewModel
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 新增：编辑用户信息页
        composable(AppRoute.EDIT_USER_INFO) {
            EditUserInfoScreen(
                viewModel = userInfoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // 新闻
        composable(AppRoute.NEWS) {
            TemporaryScreen("新闻页面", onNavigateBack = { navController.popBackStack() })
        }

        // 新增资讯详情页路由
        composable("${AppRoute.NEWS_DETAIL}/{newsId}") { backStackEntry ->
            val newsId = backStackEntry.arguments?.getString("newsId")
            com.sdu.novaglide.ui.features.home.NewsDetailScreen(
                newsId = newsId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 新增登录页路由
        composable(AppRoute.LOGIN) {
            LoginScreen(
                viewModel = userInfoViewModel, // Pass shared ViewModel
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

        // 新增注册页路由
        composable(AppRoute.REGISTER) {
            RegisterScreen(
                viewModel = userInfoViewModel, // Pass shared ViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Log.d(TAG_NAV, "渲染旧的HomeScreen (AppNavigation 内定义)")
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { 
                Text(
                    text = "NovaGlide",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ) 
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "欢迎使用NovaGlide",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "请选择您要使用的功能",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 主要功能按钮
        HomeFeatureButton(
            title = "智能问答",
            description = "使用DeepSeek模型进行智能对话",
            icon = Icons.Filled.QuestionAnswer,
            onClick = { onNavigate(AppRoute.QNA) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        HomeFeatureButton(
            title = "API设置",
            description = "配置DeepSeek与RagFlow API",
            icon = Icons.Filled.Settings,
            onClick = { onNavigate(AppRoute.API_SETTINGS) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        HomeFeatureButton(
            title = "个人资料",
            description = "查看和编辑个人信息",
            icon = Icons.Filled.Person,
            onClick = { onNavigate(AppRoute.PROFILE) }
        )
    }
}

@Composable
fun HomeFeatureButton(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .height(72.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

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
