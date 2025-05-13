package com.sdu.novaglide.ui.features.newsdetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sdu.novaglide.data.model.NewsItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    viewModel: NewsDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("资讯详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                     Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("加载失败: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text("重试")
                        }
                    }
                }
                uiState.newsItem != null -> {
                    NewsDetailContent(newsItem = uiState.newsItem!!) { url ->
                        // Open article URL in browser
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun NewsDetailContent(newsItem: NewsItem, onReadMoreClicked: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Make content scrollable
    ) {
        // Image
        newsItem.imageUrl?.let {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(it)
                    .crossfade(true)
                    .build(),
                contentDescription = newsItem.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp), // Larger image for detail screen
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Content Padding
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // Title
            Text(
                text = newsItem.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Source and Time
            Text(
                text = "${newsItem.source} · ${newsItem.publishedAt}", // TODO: Proper time formatting
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Summary/Body (Consider Markdown support for richer text)
            Text(
                text = newsItem.summary, // Use summary as body for now
                fontSize = 16.sp,
                lineHeight = 24.sp // Improve readability
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Read More Button
            Button(
                onClick = { onReadMoreClicked(newsItem.articleUrl) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("阅读原文")
            }
             Spacer(modifier = Modifier.height(16.dp)) // Padding at the bottom
        }
    }
} 