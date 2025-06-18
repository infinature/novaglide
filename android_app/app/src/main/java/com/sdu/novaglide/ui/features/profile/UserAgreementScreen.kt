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
fun UserAgreementScreen(
    onNavigateBack: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    val userAgreementContent = """
# NovaGlide 用户服务协议

*生效日期：2024年*

**欢迎使用 NovaGlide！**

请您仔细阅读本用户服务协议（以下简称"本协议"）。您的使用行为将视为对本协议的接受。

## 1. 协议接受

### 1.1 协议生效
- 当您下载、安装、注册或使用 NovaGlide 应用时，即表示您同意接受本协议的全部条款
- 如果您不同意本协议的任何条款，请立即停止使用本服务

### 1.2 协议更新
- 我们保留随时修改本协议的权利
- 修改后的协议将在应用内公布，您继续使用服务即表示接受修改后的条款

## 2. 服务内容

### 2.1 服务范围
NovaGlide 为用户提供以下服务：
- **资讯服务**：职业规划、保研、考研、留学、考公等相关资讯
- **智能问答**：基于AI技术的个性化问题解答
- **个人中心**：收藏、浏览历史、个人设置等功能
- **搜索服务**：智能搜索和内容推荐

### 2.2 服务特点
- 免费基础服务，部分高级功能可能需要付费
- 7x24小时在线服务，但不保证服务绝对不中断
- 持续优化和更新功能

## 3. 用户义务

### 3.1 账户管理
- 您有义务保管好自己的账户信息和密码
- 因您保管不善造成的损失由您自行承担
- 发现账户被盗用应立即通知我们

### 3.2 合法使用
您同意：
- **不传播违法信息**：不发布违反国家法律法规的内容
- **不恶意使用**：不进行刷量、作弊、恶意攻击等行为
- **不侵犯他人权益**：不发布侵犯他人隐私、知识产权的内容
- **遵守学术诚信**：正确使用AI问答功能，不用于作弊

### 3.3 禁止行为
以下行为被严格禁止：
- 破解、逆向工程或试图获取应用源代码
- 使用技术手段干扰应用正常运行
- 传播病毒、恶意代码或有害信息
- 商业化使用我们的内容而未获得授权

## 4. 知识产权

### 4.1 平台权利
- NovaGlide 的商标、图标、界面设计等知识产权归我们所有
- 应用中的原创内容、算法、数据库等受知识产权法保护
- 未经授权，您不得复制、修改、传播这些内容

### 4.2 用户内容
- 您对自己发布的内容拥有著作权
- 您授权我们在提供服务过程中使用您的内容
- 您保证发布的内容不侵犯第三方权益

### 4.3 第三方内容
- 应用中包含的第三方内容仅供学习参考
- 相关版权归原作者所有
- 如有侵权请及时联系我们处理

## 5. 隐私保护

### 5.1 隐私承诺
- 我们严格按照《隐私政策》保护您的个人信息
- 不会未经授权向第三方提供您的个人信息
- 采用行业标准的安全措施保护数据

### 5.2 数据使用
- 您的使用数据将用于改进服务质量
- 个人信息处理遵循最小必要原则
- 您有权查询、更正、删除个人信息

## 6. 免责声明

### 6.1 内容准确性
- 我们努力确保信息准确，但不保证内容绝对正确
- 用户应当独立判断信息的可靠性
- 因信息使用不当造成的损失我们不承担责任

### 6.2 服务可用性
- 因技术维护、网络故障等原因可能导致服务中断
- 因不可抗力因素影响服务的情况我们不承担责任
- 我们将尽力保证服务稳定，但不保证绝对无故障

### 6.3 第三方服务
- 应用中集成的第三方服务（如AI问答）由第三方提供
- 第三方服务的质量和安全性由第三方负责
- 相关问题请直接联系第三方服务提供商

## 7. 违约处理

### 7.1 违约认定
出现以下情况视为违约：
- 违反本协议的任何条款
- 从事危害平台安全的行为
- 发布违法违规内容

### 7.2 处理措施
根据违约严重程度，我们可能采取：
- **警告提醒**：首次轻微违约
- **功能限制**：限制部分功能使用
- **账户冻结**：暂停账户服务
- **永久封禁**：严重违约永久禁止使用
- **法律追责**：涉及违法行为将报告相关部门

## 8. 服务变更和终止

### 8.1 服务变更
- 我们保留调整、升级或终止部分服务的权利
- 重大变更将提前30天通知用户
- 您可以选择接受变更或停止使用服务

### 8.2 服务终止
以下情况可能导致服务终止：
- 您主动删除账户或卸载应用
- 您严重违反本协议被永久封禁
- 我们因业务调整停止运营（提前60天通知）

## 9. 争议解决

### 9.1 协商解决
- 双方应首先通过友好协商解决争议
- 可通过应用内反馈或邮件联系我们

### 9.2 法律途径
- 协商不成的争议应提交至济南市历城区人民法院
- 本协议适用中华人民共和国法律
- 如部分条款无效不影响其他条款效力

## 10. 其他条款

### 10.1 完整协议
- 本协议构成您与我们之间的完整协议
- 如与其他文件冲突，以本协议为准

### 10.2 联系方式
如对本协议有疑问，请联系：
- **客服邮箱**：support@sdu.edu.cn
- **法务邮箱**：legal@sdu.edu.cn
- **通讯地址**：山东省济南市历城区山大南路27号 山东大学

---

**感谢您选择 NovaGlide，祝您学习进步，前程似锦！**

*本协议最终解释权归 NovaGlide 开发团队所有*
"""

    val htmlContent = remember(userAgreementContent, isDarkTheme) {
        val escapedContent = userAgreementContent
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
                        text = "用户协议",
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