package com.sdu.novaglide.ui.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel
) {
    // 从ViewModel获取设置状态
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val autoSync by viewModel.autoSync.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val wifiOnly by viewModel.wifiOnly.collectAsState()
    val analyticsEnabled by viewModel.analyticsEnabled.collectAsState()
    val personalizationEnabled by viewModel.personalizationEnabled.collectAsState()
    val notificationStartHour by viewModel.notificationStartHour.collectAsState()
    val notificationEndHour by viewModel.notificationEndHour.collectAsState()
    val cacheSize by viewModel.cacheSize.collectAsState()
    val clearCacheResult by viewModel.clearCacheResult.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 外观设置
            SettingsSection(
                title = "外观设置",
                icon = Icons.Filled.Palette
            ) {
                // 深色模式
                SettingsItem(
                    icon = Icons.Filled.DarkMode,
                    title = "深色模式",
                    subtitle = "开启后界面将使用深色主题"
                ) {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) }
                    )
                }
                
                // 字体大小
                SettingsItem(
                    icon = Icons.Filled.FormatSize,
                    title = "字体大小",
                    subtitle = when {
                        fontSize < 0.9f -> "小"
                        fontSize > 1.1f -> "大"
                        else -> "标准"
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { 
                                if (fontSize > 0.8f) {
                                    viewModel.setFontSize(fontSize - 0.1f)
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "减小")
                        }
                        Text("Aa", fontSize = (16 * fontSize).sp)
                        IconButton(
                            onClick = { 
                                if (fontSize < 1.2f) {
                                    viewModel.setFontSize(fontSize + 0.1f)
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "增大")
                        }
                    }
                }
            }
            
            // 通知设置
            SettingsSection(
                title = "通知设置",
                icon = Icons.Filled.Notifications
            ) {
                SettingsItem(
                    icon = Icons.Filled.NotificationsActive,
                    title = "推送通知",
                    subtitle = "接收新资讯和重要更新"
                ) {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                    )
                }
                
                SettingsItem(
                    icon = Icons.Filled.Schedule,
                    title = "通知时间",
                    subtitle = "设置接收通知的时间段"
                ) {
                    TextButton(onClick = { /* TODO: 打开时间选择器 */ }) {
                        Text("${String.format("%02d", notificationStartHour)}:00-${String.format("%02d", notificationEndHour)}:00")
                    }
                }
            }
            
            // 数据设置
            SettingsSection(
                title = "数据设置",
                icon = Icons.Filled.Storage
            ) {
                SettingsItem(
                    icon = Icons.Filled.Sync,
                    title = "自动同步",
                    subtitle = "自动同步收藏和浏览历史"
                ) {
                    Switch(
                        checked = autoSync,
                        onCheckedChange = { viewModel.setAutoSync(it) }
                    )
                }
                
                SettingsItem(
                    icon = Icons.Filled.CleaningServices,
                    title = "清理缓存",
                    subtitle = "缓存大小: $cacheSize"
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        TextButton(
                            onClick = { viewModel.clearCache() }
                        ) {
                            Text("清理", color = MaterialTheme.colorScheme.primary)
                        }
                        clearCacheResult?.let { result ->
                            Text(
                                text = result,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                SettingsItem(
                    icon = Icons.Filled.CloudDownload,
                    title = "离线下载",
                    subtitle = "仅在WiFi环境下下载内容"
                ) {
                    Switch(
                        checked = wifiOnly,
                        onCheckedChange = { viewModel.setWifiOnly(it) }
                    )
                }
            }
            
            // 隐私设置
            SettingsSection(
                title = "隐私设置",
                icon = Icons.Filled.Security
            ) {
                SettingsItem(
                    icon = Icons.Filled.Analytics,
                    title = "数据分析",
                    subtitle = "帮助改善应用体验"
                ) {
                    Switch(
                        checked = analyticsEnabled,
                        onCheckedChange = { viewModel.setAnalyticsEnabled(it) }
                    )
                }
                
                SettingsItem(
                    icon = Icons.Filled.PersonalVideo,
                    title = "个性化推荐",
                    subtitle = "基于使用习惯推荐内容"
                ) {
                    Switch(
                        checked = personalizationEnabled,
                        onCheckedChange = { viewModel.setPersonalizationEnabled(it) }
                    )
                }
            }
            
            // 其他设置
            SettingsSection(
                title = "其他设置",
                icon = Icons.Filled.MoreHoriz
            ) {
                SettingsItem(
                    icon = Icons.Filled.Language,
                    title = "语言设置",
                    subtitle = "简体中文"
                ) {
                    TextButton(onClick = { /* 打开语言选择 */ }) {
                        Text("中文")
                    }
                }
                
                SettingsItem(
                    icon = Icons.Filled.Update,
                    title = "检查更新",
                    subtitle = "当前版本 1.0.0"
                ) {
                    TextButton(
                        onClick = { 
                            // TODO: 实现更新检查功能
                        }
                    ) {
                        Text("检查", color = MaterialTheme.colorScheme.primary)
                    }
                }
                
                SettingsItem(
                    icon = Icons.Filled.Restore,
                    title = "恢复默认设置",
                    subtitle = "将所有设置恢复为默认值"
                ) {
                    TextButton(
                        onClick = { viewModel.restoreDefaultSettings() }
                    ) {
                        Text("恢复", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 底部说明
            Text(
                text = "设置将自动保存",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        action()
    }
} 