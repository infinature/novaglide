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
import com.sdu.novaglide.ui.features.profile.UserInfoState
import com.sdu.novaglide.ui.features.profile.UserInfoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel,
    userInfoViewModel: UserInfoViewModel,
    newsViewModel: com.sdu.novaglide.ui.features.home.NewsViewModel? = null,
    onNavigateBack: () -> Unit,
    onSearch: (String) -> Unit
) {
    val searchQuery by searchViewModel.searchQuery.collectAsState()
    val searchHistory by searchViewModel.searchHistory.collectAsState()
    val hotSearches by searchViewModel.hotSearches.collectAsState()
    val searchSuggestions by searchViewModel.searchSuggestions.collectAsState()
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
    
    // 同步NewsViewModel的当前搜索查询
    LaunchedEffect(newsViewModel) {
        newsViewModel?.let { vm ->
            val currentQuery = vm.searchQuery.value
            if (currentQuery.isNotEmpty() && searchQuery.isEmpty()) {
                searchViewModel.updateSearchQuery(currentQuery)
            }
        }
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
                        onValueChange = { searchViewModel.updateSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { 
                            Text(
                                text = "搜索资讯",
                                fontSize = 16.sp
                            ) 
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp),
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
                                val query = searchViewModel.performSearch(searchQuery)
                                if (query.isNotEmpty()) {
                                    keyboardController?.hide()
                                    onSearch(query)
                                }
                            }
                        )
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
                                val query = searchViewModel.performSearch(searchQuery)
                                if (query.isNotEmpty()) {
                                    keyboardController?.hide()
                                    onSearch(query)
                                }
                            }
                        ) {
                            Text("搜索", color = MaterialTheme.colorScheme.primary)
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
                            searchViewModel.updateSearchQuery(suggestion)
                            val query = searchViewModel.performSearch(suggestion)
                            if (query.isNotEmpty()) {
                                keyboardController?.hide()
                                onSearch(query)
                            }
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
                            searchViewModel.updateSearchQuery(hotSearch)
                            val query = searchViewModel.performSearch(hotSearch)
                            if (query.isNotEmpty()) {
                                keyboardController?.hide()
                                onSearch(query)
                            }
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
                            searchViewModel.updateSearchQuery(historyItem.searchQuery)
                            val query = searchViewModel.performSearch(historyItem.searchQuery)
                            if (query.isNotEmpty()) {
                                keyboardController?.hide()
                                onSearch(query)
                            }
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
            fontSize = 16.sp,
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
                    fontSize = 14.sp,
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
                fontSize = 16.sp,
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
                        fontSize = 12.sp,
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
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "搜索历史",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            TextButton(onClick = onClearAllHistory) {
                Text(
                    text = "清空",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        searchHistory.forEach { historyItem ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHistoryClick(historyItem) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = historyItem.searchQuery,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatSearchTime(historyItem.searchTime),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { onDeleteHistory(historyItem) },
                    modifier = Modifier.size(24.dp)
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
    }
}

private fun formatSearchTime(date: Date): String {
    val formatter = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return formatter.format(date)
} 