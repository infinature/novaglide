package com.sdu.novaglide.ui.features.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.sdu.novaglide.core.util.AgreementManager
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    onAgreementRequired: () -> Unit
) {
    val context = LocalContext.current
    val agreementManager = remember { AgreementManager(context) }
    val infiniteTransition = rememberInfiniteTransition(label = "splash_animation")
    
    // 图标缩放动画
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )
    
    // 文字透明度动画
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_alpha"
    )
    
    // 加载点动画
    val loadingDots by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading_dots"
    )
    
    // 渐变背景动画
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )
    
    // 1秒后检查协议状态并跳转
    LaunchedEffect(Unit) {
        delay(1000)
        // 检查是否需要显示协议同意页面
        val shouldShowAgreement = agreementManager.shouldShowAgreementScreen()
        if (shouldShowAgreement) {
            // 需要显示协议同意页面
            onAgreementRequired()
        } else {
            // 已同意协议，直接进入主页
            onSplashFinished()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.surface
                    ),
                    startY = gradientOffset * 1000f,
                    endY = (gradientOffset + 1f) * 1000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 应用图标
            Icon(
                imageVector = Icons.Filled.School,
                contentDescription = "NovaGlide Logo",
                modifier = Modifier
                    .size(100.dp)
                    .scale(iconScale),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 应用名称
            Text(
                text = "NovaGlide",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(textAlpha)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 欢迎标语
            Text(
                text = "职业生涯规划助手",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(textAlpha)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // 欢迎文字
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎓 欢迎来到 NovaGlide",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(textAlpha)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "这里有最新的升学资讯\n智能的问答服务\n助您规划美好未来",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.alpha(textAlpha * 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // 加载指示器
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "正在加载",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(0.7f)
                )
                
                // 动态加载点
                Text(
                    text = when (loadingDots.toInt()) {
                        0 -> ""
                        1 -> "."
                        2 -> ".."
                        else -> "..."
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 线性进度指示器
            LinearProgressIndicator(
                modifier = Modifier
                    .width(200.dp)
                    .height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        
        // 底部版权信息
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "山东大学（威海）",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = "© 2024 NovaGlide Team",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
} 