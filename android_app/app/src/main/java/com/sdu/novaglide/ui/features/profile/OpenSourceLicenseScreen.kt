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
fun OpenSourceLicenseScreen(
    onNavigateBack: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    val openSourceLicenseContent = """
# NovaGlide 开源许可声明

NovaGlide 感谢以下开源项目的贡献，这些项目使我们的应用变得更加强大和稳定。

## 主要框架与库

### 1. Jetpack Compose
- **描述**：Android 现代声明式UI工具包
- **开发者**：Google
- **许可证**：Apache License 2.0
- **项目地址**：https://developer.android.com/jetpack/compose

### 2. Kotlin
- **描述**：现代编程语言，100%兼容Java
- **开发者**：JetBrains
- **许可证**：Apache License 2.0
- **项目地址**：https://kotlinlang.org/

### 3. Android Jetpack
- **描述**：Android 架构组件库
- **开发者**：Google
- **许可证**：Apache License 2.0
- **包含组件**：
  - Room Database
  - ViewModel
  - LiveData
  - Navigation
  - DataStore

### 4. Material Design 3
- **描述**：Google Material You 设计系统
- **开发者**：Google
- **许可证**：Apache License 2.0
- **项目地址**：https://m3.material.io/

## 网络与数据处理

### 5. OkHttp
- **描述**：高效HTTP客户端
- **开发者**：Square
- **许可证**：Apache License 2.0
- **项目地址**：https://square.github.io/okhttp/

### 6. Retrofit
- **描述**：类型安全的HTTP客户端
- **开发者**：Square
- **许可证**：Apache License 2.0
- **项目地址**：https://square.github.io/retrofit/

### 7. Gson
- **描述**：Java JSON序列化/反序列化库
- **开发者**：Google
- **许可证**：Apache License 2.0
- **项目地址**：https://github.com/google/gson

### 8. Kotlinx Serialization
- **描述**：Kotlin多平台序列化库
- **开发者**：JetBrains
- **许可证**：Apache License 2.0
- **项目地址**：https://github.com/Kotlin/kotlinx.serialization

## UI 组件与工具

### 9. Accompanist
- **描述**：Compose实验性功能补充库
- **开发者**：Google
- **许可证**：Apache License 2.0
- **项目地址**：https://google.github.io/accompanist/
- **使用组件**：
  - SwipeRefresh (下拉刷新)
  - System UI Controller

### 10. Coil
- **描述**：Kotlin图片加载库
- **开发者**：Coil Contributors
- **许可证**：Apache License 2.0
- **项目地址**：https://coil-kt.github.io/coil/

### 11. Lottie
- **描述**：动画渲染库
- **开发者**：Airbnb
- **许可证**：Apache License 2.0
- **项目地址**：https://airbnb.design/lottie/

## 依赖注入

### 12. Hilt
- **描述**：Android依赖注入库
- **开发者**：Google
- **许可证**：Apache License 2.0
- **项目地址**：https://dagger.dev/hilt/

### 13. Dagger
- **描述**：编译时依赖注入框架
- **开发者**：Google
- **许可证**：Apache License 2.0
- **项目地址**：https://dagger.dev/

## Web相关

### 14. WebView
- **描述**：Android系统WebView组件
- **开发者**：Google (Chromium项目)
- **许可证**：BSD License
- **项目地址**：https://chromium.googlesource.com/chromium/src/

### 15. Marked.js
- **描述**：Markdown解析器
- **开发者**：Marked.js团队
- **许可证**：MIT License
- **项目地址**：https://marked.js.org/

### 16. Highlight.js
- **描述**：语法高亮库
- **开发者**：Highlight.js团队
- **许可证**：BSD 3-Clause License
- **项目地址**：https://highlightjs.org/

### 17. GitHub Markdown CSS
- **描述**：GitHub风格Markdown样式
- **开发者**：GitHub
- **许可证**：MIT License
- **项目地址**：https://github.com/sindresorhus/github-markdown-css

## 工具与构建

### 18. Gradle
- **描述**：构建自动化工具
- **开发者**：Gradle Inc.
- **许可证**：Apache License 2.0
- **项目地址**：https://gradle.org/

### 19. Android Gradle Plugin
- **描述**：Android构建插件
- **开发者**：Google
- **许可证**：Apache License 2.0

### 20. Kotlin Symbol Processing (KSP)
- **描述**：Kotlin符号处理API
- **开发者**：Google
- **许可证**：Apache License 2.0
- **项目地址**：https://github.com/google/ksp

---

## 特别鸣谢

我们特别感谢：

- **Android 开发者社区** - 为我们提供了丰富的学习资源和最佳实践
- **Stack Overflow** - 帮助我们解决开发过程中遇到的技术难题
- **GitHub** - 为开源项目提供了优秀的协作平台
- **所有开源贡献者** - 您们的无私奉献让这个世界变得更美好

## 联系我们

如果您对我们使用的开源许可有任何疑问，请联系：

- **邮箱**：opensource@sdu.edu.cn
- **开源项目地址**：https://github.com/novaglide/android_app

---

**NovaGlide 致力于尊重和保护开源软件的知识产权，如有任何疏漏或错误，请及时联系我们进行更正。**
"""

    val htmlContent = remember(openSourceLicenseContent, isDarkTheme) {
        val escapedContent = openSourceLicenseContent
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
                
                pre {
                    background-color: #161b22 !important;
                    border: 1px solid #30363d !important;
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
                        text = "开源许可",
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