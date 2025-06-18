package com.sdu.novaglide.ui.features.profile

import android.webkit.WebView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    val privacyPolicyContent = """
# NovaGlide 隐私政策

*最后更新时间：2024年*

## 1. 信息收集

### 1.1 个人信息
我们可能收集以下类型的个人信息：
- **账户信息**：用户名、邮箱地址、头像等基本资料
- **使用数据**：浏览历史、搜索记录、收藏文章等应用使用情况
- **设备信息**：设备型号、操作系统版本、应用版本等技术信息

### 1.2 自动收集信息
- **日志信息**：应用崩溃日志、性能数据
- **使用统计**：功能使用频率、用户行为分析

## 2. 信息使用

我们使用收集的信息用于：

### 2.1 服务提供
- 提供个性化的资讯推荐
- 维护用户账户和偏好设置
- 改进搜索和问答功能的准确性

### 2.2 服务改进
- 分析用户使用模式以优化产品功能
- 修复技术问题和提升用户体验
- 开发新功能和服务

### 2.3 沟通联系
- 发送重要的服务通知
- 回应用户的问题和反馈
- 提供技术支持

## 3. 信息共享

我们**不会**出售、交易或转让您的个人信息给第三方，除非：

### 3.1 合法要求
- 遵守法律法规要求
- 配合政府部门的合法调查
- 维护我们的合法权益

### 3.2 服务提供商
- 与可信的第三方服务提供商合作（如云存储、数据分析）
- 这些合作伙伴仅能在我们授权范围内使用信息

## 4. 数据安全

### 4.1 安全措施
- 使用行业标准的加密技术保护数据传输
- 采用安全的服务器和数据库存储用户信息
- 定期进行安全审查和漏洞修复

### 4.2 访问控制
- 严格限制员工对用户数据的访问权限
- 实施多层身份验证和访问日志记录

## 5. 用户权利

您拥有以下权利：

### 5.1 访问权利
- 查看我们持有的关于您的个人信息
- 获取您的数据副本

### 5.2 更正权利
- 更新或修正不准确的个人信息
- 完善不完整的信息

### 5.3 删除权利
- 请求删除您的账户和相关数据
- 在某些情况下要求我们停止处理您的信息

### 5.4 数据便携性
- 以常用格式获取您的数据
- 将数据转移到其他服务

## 6. Cookie 和追踪技术

我们使用以下技术：
- **本地存储**：保存用户偏好和登录状态
- **分析工具**：了解应用使用情况（匿名化处理）
- **崩溃报告**：收集错误信息以改进稳定性

## 7. 第三方服务

本应用集成了以下第三方服务：
- **深度求索 (DeepSeek)**：AI问答服务
- **RAGFlow**：文档检索和问答服务

请注意这些服务有各自的隐私政策，我们建议您仔细阅读。

## 8. 儿童隐私

我们的服务主要面向大学生群体（18岁及以上）。

## 9. 国际用户

如果您位于中国境外，请注意您的信息可能会被传输到中国境内的服务器进行处理。

## 10. 政策更新

我们可能会不时更新本隐私政策。重大变更将通过以下方式通知：
- 在应用内发布通知
- 通过邮件告知注册用户
- 在官网和应用商店发布公告

## 11. 联系我们

如果您对本隐私政策有任何疑问或建议，请通过以下方式联系我们：

- **邮箱**：15086880628@163.com
- **地址**：山东省威海市环翠区文化西路180好 山东大学（威海）
- **反馈渠道**：应用内反馈功能

---

*本隐私政策的制定遵循《中华人民共和国网络安全法》、《中华人民共和国数据安全法》、《中华人民共和国个人信息保护法》等相关法律法规。*

我们承诺保护您的隐私权，感谢您对 NovaGlide 的信任与支持！
"""

    val htmlContent = remember(privacyPolicyContent, isDarkTheme) {
        val escapedContent = privacyPolicyContent
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "")

        val markdownCss = if (isDarkTheme) {
            "https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/5.8.1/github-markdown-dark.min.css"
        } else {
            "https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/5.8.1/github-markdown-light.min.css"
        }

        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <link rel="stylesheet" href="$markdownCss">
            <style>
                body {
                    margin: 0;
                    padding: 16px;
                    background-color: ${if (isDarkTheme) "#0d1117" else "#ffffff"} !important;
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                }
                .markdown-body {
                    box-sizing: border-box;
                    background-color: ${if (isDarkTheme) "#0d1117" else "#ffffff"} !important;
                    color: ${if (isDarkTheme) "#e6edf3" else "#24292f"} !important;
                    line-height: 1.6;
                }
                
                ${if (isDarkTheme) """
                * {
                    background-color: ${if (isDarkTheme) "#0d1117" else "#ffffff"} !important;
                    color: ${if (isDarkTheme) "#e6edf3" else "#24292f"} !important;
                }
                
                h1, h2, h3, h4, h5, h6 {
                    color: #f0f6fc !important;
                    border-bottom: 1px solid #30363d !important;
                }
                
                code {
                    background-color: #21262d !important;
                    color: #e6edf3 !important;
                    padding: 2px 4px !important;
                    border-radius: 3px !important;
                }
                
                blockquote {
                    background-color: #161b22 !important;
                    border-left: 4px solid #30363d !important;
                    color: #e6edf3 !important;
                }
                """ else ""}
            </style>
        </head>
        <body>
            <div id="content" class="markdown-body"></div>
            <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
            <script>
                document.getElementById('content').innerHTML = marked.parse('$escapedContent');
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "隐私政策",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    setBackgroundColor(if (isDarkTheme) 0xFF0d1117.toInt() else 0xFFffffff.toInt())
                    
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        @Suppress("DEPRECATION")
                        settings.forceDark = if (isDarkTheme) {
                            android.webkit.WebSettings.FORCE_DARK_ON
                        } else {
                            android.webkit.WebSettings.FORCE_DARK_OFF
                        }
                    }
                }
            },
            update = { webView ->
                webView.setBackgroundColor(if (isDarkTheme) 0xFF0d1117.toInt() else 0xFFffffff.toInt())
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    @Suppress("DEPRECATION")
                    webView.settings.forceDark = if (isDarkTheme) {
                        android.webkit.WebSettings.FORCE_DARK_ON
                    } else {
                        android.webkit.WebSettings.FORCE_DARK_OFF
                    }
                }
                
                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
} 