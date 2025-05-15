package com.sdu.novaglide.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.sdu.novaglide.domain.model.News
import com.sdu.novaglide.ui.components.BottomNavBar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToQna: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNewsDetail: (String) -> Unit
) {
    val state = viewModel.state.collectAsState().value
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val tabs = listOf("保研", "考研", "留学", "考公", "推荐")
    
    // 监听列表滚动，实现无限加载
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val totalItemsCount = state.news.size
                if (lastVisibleIndex != null && totalItemsCount > 0 && lastVisibleIndex >= totalItemsCount - 3) {
                    viewModel.onEvent(HomeScreenEvent.LoadMoreNews)
                }
            }
    }
    
    Scaffold(
        topBar = {
            // 顶部标签栏
            Column {
                TabRow(
                    selectedTabIndex = state.selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.White,
                    contentColor = Color.Black,
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = state.selectedTabIndex == index,
                            onClick = { 
                                viewModel.onEvent(HomeScreenEvent.TabSelected(index))
                                scope.launch {
                                    listState.scrollToItem(0) // 切换标签时滚动到顶部
                                }
                            },
                            text = { 
                                Text(
                                    text = title,
                                    fontSize = 16.sp,
                                    fontWeight = if (state.selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }
                
                // 搜索栏
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onEvent(HomeScreenEvent.SearchQueryChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(48.dp),
                    placeholder = { Text("搜索资讯") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "搜索") },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onEvent(HomeScreenEvent.ClearSearch) }) {
                                Icon(Icons.Filled.Clear, contentDescription = "清除")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color(0xFFF5F5F5),
                        unfocusedBorderColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        if (state.searchQuery.isNotBlank()) {
                            viewModel.onEvent(HomeScreenEvent.SearchSubmitted(state.searchQuery))
                            keyboardController?.hide()
                        }
                    })
                )
                
                // 二级标题
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.isSearchActive) "搜索结果" else "${tabs[state.selectedTabIndex]}资讯",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (state.isSearchActive) {
                        TextButton(onClick = { viewModel.onEvent(HomeScreenEvent.ClearSearch) }) {
                            Text("返回")
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
        // 使用SwipeRefresh包装列表，实现下拉刷新
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = state.isLoading && state.news.isNotEmpty()),
            onRefresh = { viewModel.onEvent(HomeScreenEvent.RefreshNews) },
            modifier = Modifier.fillMaxSize()
        ) {
            // 资讯列表
            Box(modifier = Modifier.fillMaxSize()) {
                if (state.news.isEmpty() && !state.isLoading && state.error == null) {
                    // 空状态
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无资讯",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = paddingValues.calculateBottomPadding() + 8.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.news) { news ->
                            NewsCard(
                                news = news,
                                onNewsClick = { onNavigateToNewsDetail(news.id) }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // 底部加载更多指示器
                        if (state.isLoading && state.news.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
                
                // 错误提示
                state.error?.let { error ->
                    if (state.news.isEmpty()) {
                        // 如果没有任何数据，显示全屏错误
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "加载失败: $error",
                                    color = Color.Red,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.onEvent(HomeScreenEvent.RefreshNews) }) {
                                    Text("重试")
                                }
                            }
                        }
                    } else {
                        // 如果有数据，显示底部错误条
                        Snackbar(
                            modifier = Modifier.padding(16.dp),
                            action = {
                                TextButton(onClick = { viewModel.onEvent(HomeScreenEvent.RefreshNews) }) {
                                    Text("重试", color = Color.White)
                                }
                            }
                        ) {
                            Text("加载失败: $error")
                        }
                    }
                }
                
                // 首次加载指示器
                if (state.isLoading && state.news.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun NewsCard(news: News, onNewsClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNewsClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = news.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = news.summary,
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
                    text = "${news.source} · ${getTimeAgoString(news.publishDate)}",
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
                        text = news.category,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

// 计算时间差的辅助函数
fun getTimeAgoString(date: Date): String {
    val now = Date()
    val diffInMillis = now.time - date.time
    
    return when {
        diffInMillis < TimeUnit.MINUTES.toMillis(1) -> "刚刚"
        diffInMillis < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diffInMillis)}分钟前"
        diffInMillis < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diffInMillis)}小时前"
        diffInMillis < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diffInMillis)}天前"
        else -> {
            val format = SimpleDateFormat("MM-dd", Locale.getDefault())
            format.format(date)
        }
    }
}
