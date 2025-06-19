package com.sdu.novaglide.ui.features.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdu.novaglide.data.local.entity.SearchHistoryEntity
import com.sdu.novaglide.ui.theme.scaledSp
import com.sdu.novaglide.ui.features.profile.UserInfoState
import com.sdu.novaglide.ui.features.profile.UserInfoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel,
    userInfoViewModel: UserInfoViewModel,
    newsViewModel: com.sdu.novaglide.ui.features.home.NewsViewModel,
    onNavigateBack: () -> Unit,
    onSearch: (String) -> Unit
) {
    val searchQuery by newsViewModel.searchQuery.collectAsState()
    val searchHistory by searchViewModel.searchHistory.collectAsState()
    val hotSearches by searchViewModel.hotSearches.collectAsState()
    val searchSuggestions by searchViewModel.getSearchSuggestions(newsViewModel.searchQuery).collectAsState()
    val userInfoState by userInfoViewModel.userInfoState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // 设置当前用户ID
    LaunchedEffect(userInfoState) {
        if (userInfoState is UserInfoState.Success) {
            val userId = (userInfoState as UserInfoState.Success).userInfo.userId
            android.util.Log.d("SearchScreen", "设置用户ID: $userId")
            searchViewModel.setCurrentUserId(userId)
        } else {
            android.util.Log.w("SearchScreen", "用户状态不是Success: $userInfoState")
        }
    }
    
    // 页面进入时确保搜索状态的一致性
    LaunchedEffect(Unit) {
        // 如果从其他页面传入了搜索查询，但用户已经清空了本地状态，保持清空状态
        android.util.Log.d("SearchScreen", "页面加载，当前搜索查询: '$searchQuery'")
    }
    
    // 获取默认热门搜索
    val defaultHotSearches = remember { searchViewModel.getDefaultHotSearches() }
    val displayHotSearches = if (hotSearches.isNotEmpty()) {
        hotSearches.map { it.searchQuery }
    } else {
        defaultHotSearches
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { newsViewModel.updateSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { 
                            Text(
                                text = "搜索资讯",
                                fontSize = 16.sp.scaledSp()
                            ) 
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp.scaledSp()),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (searchQuery.isNotEmpty()) {
                                    searchViewModel.performSearch(searchQuery)
                                    keyboardController?.hide()
                                    onSearch(searchQuery)
                                }
                            }
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        newsViewModel.clearSearch()
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.Clear,
                                        contentDescription = "清空搜索",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (searchQuery.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                searchViewModel.performSearch(searchQuery)
                                keyboardController?.hide()
                                onSearch(searchQuery)
                            }
                        ) {
                            Text("搜索", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp.scaledSp())
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 搜索建议
            if (searchSuggestions.isNotEmpty()) {
                item {
                    SearchSuggestionsSection(
                        suggestions = searchSuggestions,
                        onSuggestionClick = { suggestion ->
                            newsViewModel.updateSearchQuery(suggestion)
                            searchViewModel.performSearch(suggestion)
                            keyboardController?.hide()
                            onSearch(suggestion)
                        }
                    )
                }
            }
            
            // 热门搜索
            if (displayHotSearches.isNotEmpty()) {
                item {
                    HotSearchesSection(
                        hotSearches = displayHotSearches,
                        onHotSearchClick = { hotSearch ->
                            newsViewModel.updateSearchQuery(hotSearch)
                            searchViewModel.performSearch(hotSearch)
                            keyboardController?.hide()
                            onSearch(hotSearch)
                        }
                    )
                }
            }
            
            // 搜索历史
            if (searchHistory.isNotEmpty()) {
                item {
                    SearchHistorySection(
                        searchHistory = searchHistory,
                        onHistoryClick = { historyItem ->
                            newsViewModel.updateSearchQuery(historyItem.searchQuery)
                            searchViewModel.performSearch(historyItem.searchQuery)
                            keyboardController?.hide()
                            onSearch(historyItem.searchQuery)
                        },
                        onDeleteHistory = { historyItem ->
                            searchViewModel.deleteSearchHistory(historyItem.searchQuery)
                        },
                        onClearAllHistory = {
                            searchViewModel.clearAllSearchHistory()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchSuggestionsSection(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    Column {
        Text(
            text = "搜索建议",
            fontSize = 16.sp.scaledSp(),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        suggestions.forEach { suggestion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSuggestionClick(suggestion) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = suggestion,
                    fontSize = 14.sp.scaledSp(),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HotSearchesSection(
    hotSearches: List<String>,
    onHotSearchClick: (String) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                Icons.Filled.Whatshot,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "热门搜索",
                fontSize = 16.sp.scaledSp(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(hotSearches) { hotSearch ->
                Surface(
                    modifier = Modifier.clickable { onHotSearchClick(hotSearch) },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = hotSearch,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp.scaledSp(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchHistorySection(
    searchHistory: List<SearchHistoryEntity>,
    onHistoryClick: (SearchHistoryEntity) -> Unit,
    onDeleteHistory: (SearchHistoryEntity) -> Unit,
    onClearAllHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "搜索历史",
                        fontSize = 16.sp.scaledSp(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${searchHistory.size}",
                            fontSize = 10.sp.scaledSp(),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                TextButton(
                    onClick = onClearAllHistory,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Filled.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "清空",
                        fontSize = 12.sp.scaledSp(),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            if (searchHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "暂无搜索历史",
                            fontSize = 14.sp.scaledSp(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                searchHistory.forEachIndexed { index, historyItem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onHistoryClick(historyItem) }
                            .padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 10.sp.scaledSp(),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = historyItem.searchQuery,
                                    fontSize = 14.sp.scaledSp(),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = formatSearchTime(historyItem.searchTime),
                                        fontSize = 11.sp.scaledSp(),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (historyItem.searchCount > 1) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "搜索${historyItem.searchCount}次",
                                                fontSize = 9.sp.scaledSp(),
                                                color = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            IconButton(
                                onClick = { onDeleteHistory(historyItem) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "删除",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    
                    if (index < searchHistory.size - 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

private fun formatSearchTime(date: Date): String {
    val formatter = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return formatter.format(date)
} 