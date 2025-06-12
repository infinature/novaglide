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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userInfoViewModel: UserInfoViewModel,
    browsingHistoryViewModel: BrowsingHistoryViewModel, // Receive BrowsingHistoryViewModel
    onNavigateToQna: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNewsDetail: (String) -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("保研", "考研", "留学", "考公", "推荐")
    val currentUserState by userInfoViewModel.userInfoState.collectAsState()

    // 获取实际的新闻数据
    val allNewsItems = NewsRepository.sampleNewsData
    // 根据选中的tab过滤新闻 (这是一个简化的过滤逻辑，您可能需要更复杂的分类)
    // 为了简单起见，我们暂时显示所有新闻，或者您可以根据 newsItem.id 或添加 category 字段来过滤
    val displayedNewsItems = remember(selectedTabIndex, allNewsItems) {
        // 示例：如果您的 NewsArticle 有 category 字段，可以这样过滤
        // allNewsItems.filter { it.category == tabs[selectedTabIndex] }
        // 当前 NewsArticle 没有 category，所以我们暂时不过滤或使用一个简单的模拟
        // 为了演示，我们假设前几个ID对应不同的tab，或者您可以修改NewsArticle添加category
        when (tabs[selectedTabIndex]) {
            "保研" -> allNewsItems.filter { it.id == "news_0" }
            "考研" -> allNewsItems.filter { it.id == "news_1" }
            "留学" -> allNewsItems.filter { it.id == "news_2" }
            "考公" -> allNewsItems.filter { it.id == "news_3" }
            "推荐" -> allNewsItems.filter { it.id == "news_4" } // 或者显示所有
            else -> allNewsItems // 默认显示所有
        }.ifEmpty { allNewsItems.take(1) } // 如果过滤后为空，至少显示一个或所有
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
fun NewsCard(newsArticle: NewsArticle, onClick: () -> Unit) { // 参数类型改为 NewsArticle
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = newsArticle.title, // 使用 newsArticle.title
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            // NewsArticle 中没有 summary, source, timeAgo, category 字段
            // 您可以从 newsArticle.content 中截取一部分作为摘要
            val summary = newsArticle.content.take(100) + if (newsArticle.content.length > 100) "..." else ""
            Text(
                text = summary, // 显示部分内容作为摘要
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
                    text = "来源: NovaGlide · 刚刚", // 模拟来源和时间
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                // 如果需要显示类别，您需要在 NewsArticle 中添加 category 字段
                // 或者根据 newsArticle.id 来判断类别
                val categoryDisplay = when {
                    newsArticle.id.startsWith("news_0") -> "保研"
                    newsArticle.id.startsWith("news_1") -> "考研"
                    newsArticle.id.startsWith("news_2") -> "留学"
                    newsArticle.id.startsWith("news_3") -> "考公"
                    else -> "推荐"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEEEEEE))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = categoryDisplay, // 模拟类别
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}