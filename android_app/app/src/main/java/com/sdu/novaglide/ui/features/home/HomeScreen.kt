package com.sdu.novaglide.ui.features.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.sdu.novaglide.ui.features.profile.FavoriteArticleViewModel
import com.sdu.novaglide.data.local.entity.BrowsingHistoryEntity
import com.sdu.novaglide.data.local.entity.FavoriteArticleEntity
import com.sdu.novaglide.ui.features.home.NewsArticle
import com.sdu.novaglide.ui.features.home.NewsRepository
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * 获取类别映射关系，用于灵活匹配不同的类别名称
 */
fun getCategoryMappings(): Map<String, List<String>> {
    return mapOf(
        "保研" to listOf("保研", "保送", "研究生推免", "推免", "保送研究生"),
        "考研" to listOf("考研", "研究生考试", "考研究生", "硕士研究生", "研究生"),
        "就业" to listOf("就业", "求职", "工作", "招聘", "职业", "找工作", "就业指导"),
        "考公" to listOf("考公", "公务员", "国考", "省考", "事业单位", "公考", "公务员考试")
    )
}

/**
 * 检查文章是否属于指定类别
 */
fun isArticleInCategory(article: NewsArticle, targetCategory: String): Boolean {
    val categoryMappings = getCategoryMappings()
    val possibleCategories = categoryMappings[targetCategory] ?: listOf(targetCategory)
    return possibleCategories.any { category ->
        article.category.contains(category, ignoreCase = true) ||
        category.contains(article.category, ignoreCase = true)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userInfoViewModel: UserInfoViewModel,
    browsingHistoryViewModel: BrowsingHistoryViewModel, // Receive BrowsingHistoryViewModel
    favoriteArticleViewModel: FavoriteArticleViewModel, // 添加收藏文章ViewModel
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
    val tabs = listOf("保研", "考研", "就业", "考公","推荐")
    val currentUserState by userInfoViewModel.userInfoState.collectAsState()
    
    // 获取用户浏览历史和收藏记录用于推荐算法
    val browsingHistoryList by browsingHistoryViewModel.browsingHistoryState.collectAsState()
    val favoritesList by favoriteArticleViewModel.favoriteArticles.collectAsState()
    
    // 添加一个刷新计数器，确保下拉刷新时推荐内容会改变
    val refreshTimestamp by newsViewModel.refreshTimestamp.collectAsState()
    
    // 添加LazyColumn滚动状态
    val listState = rememberLazyListState()
    
    // 监听刷新完成，自动滚动到顶部
    LaunchedEffect(refreshTimestamp) {
        if (refreshTimestamp > 0) { // 避免初始化时触发
            listState.animateScrollToItem(0)
        }
    }
    
    val displayedNewsItems = remember(selectedTabIndex, newsList, browsingHistoryList, favoritesList, currentUserState, refreshTimestamp) {
        val tab = tabs[selectedTabIndex]
        Log.d("HomeScreen", "当前选中标签: $tab")
        Log.d("HomeScreen", "所有新闻数量: ${newsList.size}")
        
        if (tab == "推荐") {
            // 应用推荐算法
            if (currentUserState is UserInfoState.Success) {
                generateRecommendations(newsList, browsingHistoryList, favoritesList)
            } else {
                // 未登录用户显示默认推荐（按时间排序的混合内容）
                generateDefaultRecommendations(newsList)
            }
        } else {
            // 使用类别映射进行更灵活的筛选
            val filteredNews = newsList.filter { article ->
                isArticleInCategory(article, tab)
            }
            
            Log.d("HomeScreen", "标签'$tab'筛选后文章数: ${filteredNews.size}")
            if (filteredNews.isEmpty()) {
                Log.d("HomeScreen", "该类别无文章，所有文章的类别: ${newsList.map { it.category }.distinct()}")
            }
            
            // 为所有其他类别也添加随机排列功能
            generateCategoryRecommendations(filteredNews, refreshTimestamp)
        }
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
                state = listState, // 添加滚动状态
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

/**
 * 基于用户行为的推荐算法
 * 综合考虑浏览历史、收藏记录、文章时间等因素
 */
fun generateRecommendations(
    allNews: List<NewsArticle>,
    browsingHistory: List<BrowsingHistoryEntity>,
    favorites: List<FavoriteArticleEntity>
): List<NewsArticle> {
    if (allNews.isEmpty()) return emptyList()
    
    // 获取用户偏好类别（基于浏览历史和收藏）
    val userPreferredCategories = getUserPreferredCategories(allNews, browsingHistory, favorites)
    
    // 获取用户已读文章ID（避免重复推荐）
    val readNewsIds = browsingHistory.map { it.newsId }.toSet()
    val favoriteNewsIds = favorites.map { it.newsId }.toSet()
    
    // 计算推荐分数
    val newsWithScores = allNews.map { article ->
        var score = 0.0
        
        // 1. 时间权重（越新的文章分数越高）
        val daysSincePublish = (System.currentTimeMillis() / 1000 - article.publishTime) / (24 * 60 * 60)
        val timeScore = when {
            daysSincePublish <= 1 -> 3.0  // 今天发布
            daysSincePublish <= 3 -> 2.0  // 3天内
            daysSincePublish <= 7 -> 1.0  // 一周内
            else -> 0.5
        }
        score += timeScore
        
        // 2. 类别偏好权重
        val categoryScore = userPreferredCategories[article.category] ?: 0.5
        score += categoryScore * 2.0
        
        // 3. 多样性权重（避免单一类别）
        val diversityBonus = if (userPreferredCategories.size > 1) 0.5 else 0.0
        score += diversityBonus
        
        // 4. 已读文章处理：降低分数但不完全排除
        if (article.id in readNewsIds) {
            score *= 0.6 // 降低已读文章的权重，但保留一定可见性
        }
        
        // 5. 收藏过的类别加分
        if (article.id in favoriteNewsIds) {
            score += 1.0
        }
        
        // 6. 内容质量权重（基于标题和摘要长度）
        val contentQualityScore = when {
            article.title.length > 20 && article.summary.length > 50 -> 1.0
            article.title.length > 10 && article.summary.length > 20 -> 0.5
            else -> 0.0
        }
        score += contentQualityScore
        
        // 7. 随机因子增加多样性（每次刷新都会不同）
        val randomFactor = kotlin.random.Random.nextDouble(0.0, 0.3)
        score += randomFactor
        
        Pair(article, score)
    }
    
    // 按分数排序，显示更多文章（增加到50个）
    val sortedNews = newsWithScores.sortedByDescending { it.second }
    
    // 确保推荐内容足够多样化
    val recommendations = mutableListOf<NewsArticle>()
    val usedCategories = mutableSetOf<String>()
    
    // 优先确保每个类别都有代表
    val categories = listOf("保研", "考研", "就业", "考公")
    categories.forEach { category ->
        val categoryArticles = sortedNews.filter { newsWithScore ->
            val article = newsWithScore.first
            article !in recommendations && isArticleInCategory(article, category)
        }
        if (categoryArticles.isNotEmpty()) {
            recommendations.addAll(categoryArticles.take(3).map { it.first })
            usedCategories.add(category)
        }
    }
    
    // 然后填充剩余位置，最多显示所有可用文章的80%
    val remaining = sortedNews
        .filter { it.first !in recommendations }
        .map { it.first }
    
    recommendations.addAll(remaining)
    
    // 限制最大数量但保证有足够的内容
    val maxRecommendations = maxOf(30, (allNews.size * 0.8).toInt())
    return recommendations.take(maxRecommendations)
}

/**
 * 未登录用户的默认推荐算法
 * 基于时间和内容质量的混合推荐
 */
fun generateDefaultRecommendations(allNews: List<NewsArticle>): List<NewsArticle> {
    if (allNews.isEmpty()) return emptyList()
    
    // 按类别分组，每个类别取更多文章
    val recommendations = mutableListOf<NewsArticle>()
    
    // 从每个类别中选择最新的文章（增加每类别的文章数量）
    val categories = listOf("保研", "考研", "就业", "考公")
    categories.forEach { category ->
        val categoryNews = allNews.filter { article ->
            isArticleInCategory(article, category)
        }.sortedByDescending { it.publishTime }
         .take(6) // 每个类别增加到6篇
        recommendations.addAll(categoryNews)
    }
    
    // 补充其他文章，确保有足够的内容
    val remaining = allNews
        .filter { it !in recommendations }
        .sortedByDescending { it.publishTime }
    
    recommendations.addAll(remaining)
    
    // 计算推荐文章数量（确保有足够的内容）
    val minRecommendations = 25
    val maxRecommendations = maxOf(minRecommendations, (allNews.size * 0.9).toInt())
    
    // 为了增加下拉刷新的效果，每次都重新随机排序
    val shuffledRecommendations = recommendations.distinctBy { it.id }.shuffled()
    
    return shuffledRecommendations.take(maxRecommendations)
}

/**
 * 分析用户偏好类别
 * 基于浏览历史和收藏记录计算各类别的权重
 */
private fun getUserPreferredCategories(
    allNews: List<NewsArticle>,
    browsingHistory: List<BrowsingHistoryEntity>,
    favorites: List<FavoriteArticleEntity>
): Map<String, Double> {
    val categoryWeights = mutableMapOf<String, Double>()
    
    // 基于浏览历史分析
    browsingHistory.forEach { history ->
        val article = allNews.find { it.id == history.newsId }
        article?.let {
            val currentWeight = categoryWeights[it.category] ?: 0.0
            categoryWeights[it.category] = currentWeight + 1.0
        }
    }
    
    // 基于收藏记录分析（收藏的权重更高）
    favorites.forEach { favorite ->
        val article = allNews.find { it.id == favorite.newsId }
        article?.let {
            val currentWeight = categoryWeights[it.category] ?: 0.0
            categoryWeights[it.category] = currentWeight + 2.0 // 收藏权重更高
        }
    }
    
    // 归一化权重
    val maxWeight = categoryWeights.values.maxOrNull() ?: 1.0
    return categoryWeights.mapValues { (_, weight) ->
        (weight / maxWeight).coerceAtLeast(0.1) // 最低权重0.1
    }
}

/**
 * 为特定类别生成随机排列的推荐
 * 每次刷新都会重新排列，展示未曾看到的内容
 */
fun generateCategoryRecommendations(
    categoryNews: List<NewsArticle>, 
    refreshTimestamp: Long
): List<NewsArticle> {
    if (categoryNews.isEmpty()) return emptyList()
    
    // 使用refreshTimestamp作为随机种子，确保每次刷新都有不同的排列
    val random = kotlin.random.Random(refreshTimestamp)
    
    // 按时间排序，然后随机打乱
    val sortedByTime = categoryNews.sortedByDescending { it.publishTime }
    
    // 将文章分成几个时间段，然后在每个时间段内随机排列
    val now = System.currentTimeMillis() / 1000
    val recentNews = mutableListOf<NewsArticle>()
    val olderNews = mutableListOf<NewsArticle>()
    
    sortedByTime.forEach { article ->
        val daysSincePublish = (now - article.publishTime) / (24 * 60 * 60)
        if (daysSincePublish <= 7) { // 一周内的新闻
            recentNews.add(article)
        } else {
            olderNews.add(article)
        }
    }
    
    // 分别随机打乱近期和较旧的新闻
    val shuffledRecent = recentNews.shuffled(random)
    val shuffledOlder = olderNews.shuffled(random)
    
    // 组合结果：优先展示近期新闻，但顺序随机
    return shuffledRecent + shuffledOlder
}