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
import com.sdu.novaglide.ui.features.home.NewsArticle // From NewsData.kt
import com.sdu.novaglide.ui.features.profile.FavoriteArticleViewModel
import com.sdu.novaglide.ui.features.profile.UserInfoState
import com.sdu.novaglide.ui.features.profile.UserInfoViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    newsId: String?,
    onNavigateBack: () -> Unit,
    userInfoViewModel: UserInfoViewModel,
    favoriteArticleViewModel: FavoriteArticleViewModel
) {
    val newsViewModel: NewsViewModel = viewModel()
    val newsList by newsViewModel.newsList.collectAsState()
    val article = newsList.find { it.id == newsId }
    val currentUserState by userInfoViewModel.userInfoState.collectAsState()
    val isFavorite by favoriteArticleViewModel.isFavorite.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = article, key2 = currentUserState) {
        article?.let { currentArticle ->
            if (currentUserState is UserInfoState.Success) {
                val userId = (currentUserState as UserInfoState.Success).userInfo.userId
                favoriteArticleViewModel.checkIfFavorite(userId, currentArticle.id)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(article?.title ?: "资讯详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    article?.let { currentArticle ->
                        IconButton(onClick = {
                            if (currentUserState is UserInfoState.Success) {
                                val userId = (currentUserState as UserInfoState.Success).userInfo.userId
                                if (isFavorite) {
                                    favoriteArticleViewModel.removeFavorite(userId, currentArticle.id)
                                } else {
                                    favoriteArticleViewModel.addFavorite(userId, currentArticle.id, currentArticle.title)
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("请先登录")
                                }
                            }
                        }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "收藏",
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (article == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                if (newsId.isNullOrEmpty()) {
                    Text("未指定资讯。")
                } else {
                    Text("资讯加载失败或未找到。")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = article.content,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
            }
        }
    }
}