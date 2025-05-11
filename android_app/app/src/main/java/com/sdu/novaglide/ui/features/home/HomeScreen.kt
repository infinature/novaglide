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

@Composable
fun HomeScreen(
    onNavigateToQna: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("保研", "考研", "留学", "考公", "推荐")
    
    Scaffold(
        topBar = {
            // 顶部标签栏
            Column {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    edgePadding = 8.dp,
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            height = 2.dp,
                            color = Color.Black
                        )
                    }
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
            // 模拟资讯列表
            val newsItems = List(5) { 
                NewsItem(
                    title = "资讯标题",
                    summary = "摘要内容摘要内容摘要内容摘要内容摘要内容",
                    source = "来源",
                    timeAgo = "2小时前",
                    category = tabs[selectedTabIndex]
                )
            }
            
            items(newsItems) { newsItem ->
                NewsCard(newsItem = newsItem)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun NewsCard(newsItem: NewsItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* 点击资讯卡片 */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = newsItem.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = newsItem.summary,
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
                    text = "${newsItem.source} · ${newsItem.timeAgo}",
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
                        text = newsItem.category,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

data class NewsItem(
    val title: String,
    val summary: String,
    val source: String,
    val timeAgo: String,
    val category: String
) 