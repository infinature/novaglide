package com.sdu.novaglide.ui.features.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdu.novaglide.data.local.entity.FavoriteArticleEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteArticlesScreen(
    userInfoViewModel: UserInfoViewModel,
    favoriteArticleViewModel: FavoriteArticleViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNewsDetail: (String) -> Unit
) {
    val currentUserState by userInfoViewModel.userInfoState.collectAsState()
    val favoritesList by favoriteArticleViewModel.favoriteArticles.collectAsState()
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }


    LaunchedEffect(currentUserState) {
        if (currentUserState is UserInfoState.Success) {
            val userId = (currentUserState as UserInfoState.Success).userInfo.userId
            favoriteArticleViewModel.loadUserFavorites(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的收藏") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (currentUserState !is UserInfoState.Success) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("请先登录以查看收藏")
                }
            } else if (favoritesList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Favorite, contentDescription = "无收藏记录", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("暂无收藏记录")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(favoritesList, key = { it.id }) { favoriteItem ->
                        FavoriteItemView(
                            favorite = favoriteItem,
                            dateFormat = dateFormat,
                            onClick = { onNavigateToNewsDetail(favoriteItem.newsId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteItemView(
    favorite: FavoriteArticleEntity,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = favorite.newsTitle,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "收藏于: ${dateFormat.format(favorite.favoritedAt)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
