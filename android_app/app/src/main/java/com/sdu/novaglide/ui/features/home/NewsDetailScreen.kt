package com.sdu.novaglide.ui.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdu.novaglide.ui.features.profile.FavoriteArticleViewModel
import com.sdu.novaglide.ui.features.profile.UserInfoState
import com.sdu.novaglide.ui.features.profile.UserInfoViewModel
import com.sdu.novaglide.ui.features.home.NewsArticle // 确保导入 NewsArticle from NewsData.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    newsId: String?,
    userInfoViewModel: UserInfoViewModel, // 获取当前用户ID
    favoriteArticleViewModel: FavoriteArticleViewModel, // 处理收藏逻辑
    onNavigateBack: () -> Unit
) {
    // article 现在应该通过 NewsRepository.getNewsById(newsId) 获取
    val article = remember(newsId) { NewsRepository.getNewsById(newsId) } // 使用 NewsData.kt 中的 NewsRepository
    val currentUserState by userInfoViewModel.userInfoState.collectAsState()
    val isFavorite by favoriteArticleViewModel.isFavorite.collectAsState()

    LaunchedEffect(key1 = newsId, key2 = currentUserState) {
        if (newsId != null && currentUserState is UserInfoState.Success) {
            val userId = (currentUserState as UserInfoState.Success).userInfo.userId
            favoriteArticleViewModel.checkIfFavorite(userId, newsId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(article.title, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (article != null && currentUserState is UserInfoState.Success) {
                        val userId = (currentUserState as UserInfoState.Success).userInfo.userId
                        IconButton(onClick = {
                            if (isFavorite) {
                                favoriteArticleViewModel.removeFavorite(userId, article.title)
                            } else {
                                favoriteArticleViewModel.addFavorite(userId, article.title, article.title)
                            }
                        }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (isFavorite) "取消收藏" else "收藏",
                                tint = if (isFavorite) Color.Red else LocalContentColor.current
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()) // 使内容可滚动
        ) {
            Text(
                text = article.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = article.content,
                fontSize = 16.sp,
                lineHeight = 24.sp // 增加行高以提高可读性
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "资讯ID: ${newsId ?: "未知"}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}