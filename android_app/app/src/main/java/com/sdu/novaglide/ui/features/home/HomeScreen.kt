package com.sdu.novaglide.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdu.novaglide.ui.components.BottomNavBar
import com.sdu.novaglide.ui.theme.scaledSp
import com.sdu.novaglide.ui.features.profile.BrowsingHistoryViewModel
import com.sdu.novaglide.ui.features.profile.UserInfoState
import com.sdu.novaglide.ui.features.profile.UserInfoViewModel
import com.sdu.novaglide.ui.features.home.NewsArticle
import com.sdu.novaglide.ui.features.home.NewsRepository
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userInfoViewModel: UserInfoViewModel,
    browsingHistoryViewModel: BrowsingHistoryViewModel, // Receive BrowsingHistoryViewModel
    newsViewModel: NewsViewModel,
    onNavigateToQna: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNewsDetail: (String) -> Unit
) {
    val newsList by newsViewModel.newsList.collectAsState()
    val searchQuery by newsViewModel.searchQuery.collectAsState()
    val selectedTabIndex by newsViewModel.selectedTabIndex.collectAsState()
    val isRefreshing by newsViewModel.isRefreshing.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // 下拉刷新状态
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    LaunchedEffect(Unit) {
        if (newsList.isEmpty()) {
            newsViewModel.fetchNews()
        }
    }
    val tabs = listOf("保研", "考研", "留学", "考公","推荐")
    val currentUserState by userInfoViewModel.userInfoState.collectAsState()
    val displayedNewsItems = remember(selectedTabIndex, newsList) {
        val tab = tabs[selectedTabIndex]
        if (tab == "推荐") newsList else newsList.filter { it.category == tab }
    }

    Scaffold(
        topBar = {
            // 顶部标签栏
            Column {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { newsViewModel.updateSelectedTabIndex(index) },
                            text = { 
                                Text(
                                    text = title,
                                fontSize = 16.sp.scaledSp(),
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                                ) 
                            }
                        )
                    }
                }
                
                // 搜索栏
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .heightIn(min = 56.dp)
                        .clickable { onNavigateToSearch() },
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Search, 
                            contentDescription = "搜索",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) searchQuery else "搜索资讯",
                            fontSize = 16.sp.scaledSp(),
                            color = if (searchQuery.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { newsViewModel.clearSearch() },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = "清空搜索",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        }
                }
                

            }
        },
        bottomBar = {
            BottomNavBar(
                selectedIndex = 0,
                onHomeClick = { },
                onChatClick = { onNavigateToQna() },
                onProfileClick = { onNavigateToProfile() }
            )
        }
    ) { paddingValues ->
        // 下拉刷新容器
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { newsViewModel.refreshNews() },
            modifier = Modifier.fillMaxSize()
        ) {
            // 资讯列表
            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(displayedNewsItems, key = { it.id }) { newsArticle -> 
                    NewsCard( 
                        newsArticle = newsArticle,
                        onClick = {
                            // 当用户登出后，currentUserState 会是 UserInfoState.Error
                            // 此时不应该尝试记录浏览历史，因为没有有效的 userId
                            if (currentUserState is UserInfoState.Success) {
                                val userId = (currentUserState as UserInfoState.Success).userInfo.userId
                                browsingHistoryViewModel.addBrowsingHistory(userId, newsArticle.id, newsArticle.title)
                            }
                            onNavigateToNewsDetail(newsArticle.id)
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun NewsCard(newsArticle: NewsArticle, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = newsArticle.title,
                fontSize = 16.sp.scaledSp(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = newsArticle.summary,
                fontSize = 14.sp.scaledSp(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "来源: ${newsArticle.source} · ${formatPublishTime(newsArticle.publishTime)}",
                    fontSize = 12.sp.scaledSp(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = newsArticle.category,
                        fontSize = 12.sp.scaledSp(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

fun formatPublishTime(timestamp: Long): String {
    return try {
        val dateTime = java.time.Instant.ofEpochSecond(timestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDateTime()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        dateTime.format(formatter)
    } catch (e: Exception) {
        "-"
    }
}