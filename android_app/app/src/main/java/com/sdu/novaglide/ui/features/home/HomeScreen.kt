package com.sdu.novaglide.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdu.novaglide.ui.components.BottomNavBar
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
    onNavigateToQna: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNewsDetail: (String) -> Unit
) {
    val viewModel: NewsViewModel = viewModel()
    val newsList by viewModel.newsList.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.fetchNews()
    }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("保研", "考研", "留学", "考公", "推荐")
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
                    containerColor = Color.White,
                    contentColor = Color.Black,
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { 
                                Text(
                                    text = title,
                                    fontSize = 16.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }
                
                // 搜索栏
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(48.dp),
                    placeholder = { Text("搜索资讯") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "搜索") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color(0xFFF5F5F5),
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                
                // 二级标题
                Text(
                    text = "资讯标题",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
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
        // 资讯列表
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // 使用从 NewsRepository 获取的新闻列表
            items(displayedNewsItems, key = { it.id }) { newsArticle -> // 改为 newsArticle
                NewsCard( // NewsCard 现在接收 NewsArticle
                    newsArticle = newsArticle,
                    onClick = {
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

@Composable
fun NewsCard(newsArticle: NewsArticle, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = newsArticle.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = newsArticle.summary,
                fontSize = 14.sp,
                color = Color.Gray,
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
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEEEEEE))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = newsArticle.category,
                        fontSize = 12.sp,
                        color = Color.Black
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