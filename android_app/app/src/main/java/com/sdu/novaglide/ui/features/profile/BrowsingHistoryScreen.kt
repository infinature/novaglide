package com.sdu.novaglide.ui.features.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdu.novaglide.data.local.entity.BrowsingHistoryEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowsingHistoryScreen(
    userInfoViewModel: UserInfoViewModel, // To get current userId
    browsingHistoryViewModel: BrowsingHistoryViewModel, // For history data and actions
    onNavigateBack: () -> Unit,
    onNavigateToNewsDetail: (String) -> Unit
) {
    val currentUserState by userInfoViewModel.userInfoState.collectAsState()
    val historyList by browsingHistoryViewModel.browsingHistoryState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    LaunchedEffect(currentUserState) {
        if (currentUserState is UserInfoState.Success) {
            val userId = (currentUserState as UserInfoState.Success).userInfo.userId
            browsingHistoryViewModel.loadBrowsingHistory(userId)
        } else {
            // Clear history if user logs out or state is not success
            // browsingHistoryViewModel.clearHistory() // You might need a method for this
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("浏览历史") },
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
                    Text("请先登录以查看浏览历史。")
                }
            } else if (historyList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.History, contentDescription = "无历史记录", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("暂无浏览历史记录。")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(historyList, key = { it.id }) { historyItem ->
                        HistoryItemView(
                            history = historyItem,
                            dateFormat = dateFormat,
                            onClick = { onNavigateToNewsDetail(historyItem.newsId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemView(
    history: BrowsingHistoryEntity,
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
                    text = history.newsTitle, // 直接使用存储的标题
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "资讯ID: ${history.newsId}", // 仍然可以显示ID作为辅助信息
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = dateFormat.format(history.viewedAt),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
