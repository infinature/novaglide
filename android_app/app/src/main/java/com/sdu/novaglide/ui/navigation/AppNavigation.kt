package com.sdu.novaglide.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sdu.novaglide.ui.features.home.HomeScreen // 假设存在
import com.sdu.novaglide.ui.features.qna.QnaScreen // 假设存在
import com.sdu.novaglide.ui.features.profile.ProfileScreen
import com.sdu.novaglide.ui.features.profile.UserInfoScreen

// 定义路由常量
object AppDestinations {
    const val HOME_ROUTE = "home"
    const val QNA_ROUTE = "qna"
    const val PROFILE_ROUTE = "profile"
    const val USER_INFO_ROUTE = "user_info"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppDestinations.HOME_ROUTE) {
        composable(AppDestinations.HOME_ROUTE) {
            HomeScreen(
                onNavigateToQna = { navController.navigate(AppDestinations.QNA_ROUTE) },
                onNavigateToProfile = { navController.navigate(AppDestinations.PROFILE_ROUTE) }
            )
        }
        composable(AppDestinations.QNA_ROUTE) {
            QnaScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(AppDestinations.PROFILE_ROUTE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }, // 或者导航到首页等
                onNavigateToUserInfo = { navController.navigate(AppDestinations.USER_INFO_ROUTE) } // 点击时导航到用户信息页
            )
        }
        composable(AppDestinations.USER_INFO_ROUTE) { // 为UserInfoScreen添加新的composable条目
            UserInfoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        // 其他可能的路由...
    }
}
