package com.sdu.novaglide.ui.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

// 示例数据结构
data class NewsArticle(val title: String, val content: String)

// 示例资讯内容
private val sampleNewsData = mapOf(
    "news_0" to NewsArticle(
        title = "保研全攻略：从准备到上岸",
        content = """这里是关于【保研】的详细资讯内容。

        一、什么是保研？
        保研，全称"推荐优秀应届本科毕业生免试攻读硕士学位研究生"，是指在普通高等院校中，对一部分学习成绩优异、综合素质较高的应届本科毕业生，通过一定的选拔程序，推荐其免去全国硕士研究生统一招生考试初试，直接参加复试并被录取为硕士研究生的一种招生方式。

        二、保研的条件：
        1. 纳入国家普通本科招生计划录取的应届毕业生（不含专升本、第二学士学位、独立学院学生）。
        2.具有高尚的爱国主义情操和集体主义精神，社会主义信念坚定，社会责任感强，遵纪守法，积极向上，身心健康。
        3.勤奋学习，刻苦钻研，成绩优秀；学术研究兴趣浓厚，有较强的创新意识、创新能力和专业能力潜质。
        4.诚实守信，学风端正，无任何考试作弊和剽窃他人学术成果记录。
        5.品行表现优良，无任何违法违纪受处分记录。
        6.原则上要求全国大学英语四级考试成绩不低于425分（或托福、雅思等同等水平）。
        （具体条件以各高校当年发布的推免政策为准）

        三、保研流程：
        - 夏令营/预推免（6-9月）
        - 校内推免资格获取（8-9月）
        - 全国推免系统填报（9-10月）
        - 参加复试，等待录取通知
        """
    ),
    "news_1" to NewsArticle(
        title = "考研高分经验分享",
        content = """这里是关于【考研】的详细资讯内容。

        一、制定合理的复习计划：
        考研复习是一个长期且艰苦的过程，制定一个详细且可行的复习计划至关重要。要明确各科的复习重点和时间分配，并根据自身情况进行调整。

        二、选择合适的参考资料：
        市面上的考研资料琳琅满目，选择适合自己的、权威的参考书和习题集非常重要。可以参考学长学姐的建议，或者查看目标院校指定的参考书目。

        三、注重基础，循序渐进：
        万丈高楼平地起，考研复习尤其要重视基础知识的掌握。不要急于求成，要一步一个脚印，把每个知识点都学扎实。

        四、高效利用时间，劳逸结合：
        考研期间时间非常宝贵，要学会高效利用碎片化时间。同时，也要注意劳逸结合，保证充足的睡眠和适当的放松，以保持良好的学习状态。
        """
    ),
    "news_2" to NewsArticle(
        title = "留学申请指南：圆梦海外名校",
        content = """这里是关于【留学】的详细资讯内容。

        一、明确留学目标与规划：
        在决定留学之前，首先要明确自己的留学目的（提升学历、专业深造、体验文化等）、目标国家和院校、以及预算等。

        二、语言准备与标化考试：
        根据目标国家和院校的要求，准备相应的语言考试（如托福、雅思）和标准化入学考试（如GRE、GMAT）。

        三、准备申请材料：
        常见的申请材料包括：成绩单、推荐信、个人陈述（PS）、简历（CV）、语言成绩证明、作品集（艺术设计类专业）等。

        四、递交申请与等待结果：
        在目标院校的申请截止日期前，通过其官方申请系统递交所有材料。之后就是耐心等待录取结果。
        """
    ),
    "news_3" to NewsArticle(
        title = "考公上岸经验：笔试面试技巧",
        content = """这里是关于【考公】的详细资讯内容。

        一、了解公务员考试：
        公务员考试通常包括笔试和面试两个环节。笔试主要考察行政职业能力测验（行测）和申论。

        二、笔试备考策略：
        行测：题量大、时间紧，需要通过大量练习提高做题速度和准确率，掌握各类题型的解题技巧。
        申论：注重阅读理解、归纳概括、综合分析、提出和解决问题、文字表达能力的考察。多看新闻评论，多写多练。

        三、面试准备要点：
        了解常见的面试形式（结构化面试、无领导小组讨论等），针对性地进行模拟练习。注意仪表仪态、语言表达和逻辑思维。

        四、关注招考信息，积极备考：
        及时关注国家和地方发布的招考公告，了解岗位需求和报考条件。保持积极心态，坚持不懈地学习。
        """
    ),
    "news_4" to NewsArticle(
        title = "【推荐】近期热门职业发展趋势",
        content = """这里是关于【推荐】的详细资讯内容。

        当前，随着科技的飞速发展和产业结构的深刻变革，一些新兴职业和热门领域持续涌现，为求职者提供了广阔的发展空间。

        1. 人工智能与大数据：
        AI算法工程师、数据科学家、机器学习工程师等岗位需求旺盛，薪资待遇也相对较高。

        2. 新能源与碳中和：
        随着全球对气候变化的关注，新能源技术研发、碳排放管理等领域人才缺口较大。

        3. 数字营销与内容创作：
        短视频运营、直播带货、新媒体编辑等岗位，在互联网时代依然保持着较高的热度。

        4. 大健康产业：
        健康管理师、在线问诊医生、康复治疗师等，随着人们健康意识的提高，需求日益增长。

        选择职业时，除了关注热门趋势，还应结合自身兴趣和优势，做出最适合自己的选择。
        """
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    newsId: String?,
    onNavigateBack: () -> Unit
) {
    // 从 newsId 中提取分类键，例如 "news_0_1" -> "news_0"
    val categoryKey = newsId?.substringBeforeLast('_', missingDelimiterValue = newsId ?: "")
    val article = sampleNewsData[categoryKey] ?: sampleNewsData[newsId] ?: NewsArticle("资讯不存在", "抱歉，无法找到对应的资讯内容。ID: ${newsId ?: "未知"}")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(article.title, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()) // 使内容可滚动
        ) {
            Text(
                text = article.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = article.content,
                fontSize = 16.sp,
                lineHeight = 24.sp // 增加行高以提高可读性
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "资讯ID: ${newsId ?: "未知"}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
} 