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

private const val TAG = "AppNavigation"

/**
 * 应用导航路由
 */
object AppRoute {
    const val HOME = "home"
    const val QNA = "qna"
    const val API_SETTINGS = "api_settings"
    const val PROFILE = "profile"
    const val NEWS = "news"
}

/**
 * 应用导航组件
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppRoute.HOME,
    chatRepository: ChatRepository,
    apiKeyStore: ApiKeyStore
) {
    // 创建视图模型实例
    val qnaViewModel = remember { QnaViewModel(chatRepository, apiKeyStore) }
    val apiSettingsViewModel = remember { ApiSettingsViewModel(apiKeyStore) }
    
    LaunchedEffect(key1 = Unit) {
        Log.d(TAG, "AppNavigation 初始化完成，开始导航至: $startDestination")
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 首页
        composable(AppRoute.HOME) {
            HomeScreen(onNavigate = { route -> 
                Log.d(TAG, "正在导航至: $route")
                navController.navigate(route) 
            })
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
        
        // 个人资料
        composable(AppRoute.PROFILE) {
            // ProfileScreen(onNavigateBack = { navController.popBackStack() })
            TemporaryScreen("个人资料页面", onNavigateBack = { navController.popBackStack() })
        }
        
        // 新闻
        composable(AppRoute.NEWS) {
            // NewsScreen(onNavigateBack = { navController.popBackStack() })
            TemporaryScreen("新闻页面", onNavigateBack = { navController.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Log.d(TAG, "渲染HomeScreen")
    
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
