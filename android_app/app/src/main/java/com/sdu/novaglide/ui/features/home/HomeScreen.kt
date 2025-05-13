package com.sdu.novaglide.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.sdu.novaglide.data.model.NewsItem
import com.sdu.novaglide.ui.components.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToQna: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isRefreshing)
    val lazyListState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                // Top Tab Row
                TabRow(
                    selectedTabIndex = uiState.selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.White,
                    contentColor = Color.Black,
                ) {
                    uiState.tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = uiState.selectedTabIndex == index,
                            onClick = { viewModel.selectCategory(index) },
                            text = { 
                                Text(
                                    text = title,
                                    fontSize = 16.sp,
                                    fontWeight = if (uiState.selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }
                
                // Search Bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(48.dp),
                    placeholder = { Text("搜索资讯...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "搜索") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color(0xFFF5F5F5),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            viewModel.performSearch()
                            keyboardController?.hide()
                        }
                    )
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
        
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refreshNews() },
            modifier = Modifier.padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.newsItems.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    uiState.error != null && uiState.newsItems.isEmpty() && !uiState.isRefreshing -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("加载失败: ${uiState.error}", color = Color.Red)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.refreshNews() }) {
                                Text("重试")
                            }
                        }
                    }
                    !uiState.isLoading && uiState.newsItems.isEmpty() -> {
                         Text(
                             text = if (uiState.searchQuery.isNotBlank()) "未找到相关资讯" else "暂无资讯",
                             modifier = Modifier.align(Alignment.Center).padding(16.dp),
                             textAlign = TextAlign.Center
                         )
                    }
                    else -> {
                        LazyColumn(
                            state = lazyListState,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(uiState.newsItems, key = { _, item -> item.id }) { index, newsItem ->
                                NewsCard(
                                    newsItem = newsItem,
                                    onClick = { onNavigateToDetail(newsItem.id) }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                if (index == uiState.newsItems.size - 1 && uiState.canLoadMore && !uiState.isLoadingMore) {
                                    LaunchedEffect(Unit) {
                                        viewModel.loadMoreNews()
                                    }
                                }
                            }
                            
                            if (uiState.isLoadingMore) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                    }
                                }
                            }
                            
                            if (uiState.error != null && uiState.isLoadingMore) {
                                item {
                                     Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                        Text("加载更多失败: ${uiState.error}", color = Color.Red, modifier = Modifier.align(Alignment.Center))
                                     }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewsCard(newsItem: NewsItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            newsItem.imageUrl?.let {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(it)
                        .crossfade(true)
                        .build(),
                    contentDescription = newsItem.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = newsItem.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = newsItem.summary,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${newsItem.source} · ${newsItem.publishedAt}",
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
} 