// (如果这个文件不存在，您可以创建一个类似的文件来存放新闻数据，或者从您的实际数据源获取)
package com.sdu.novaglide.ui.features.home

data class NewsArticle(
    val id: String,
    val title: String,
    val content: String,
    // 其他属性...
)

object NewsRepository { // 简单对象作为数据源示例
    val sampleNewsData: List<NewsArticle> = listOf(
        NewsArticle("news_0", "保研全攻略：从准备到上岸", "详细介绍保研的准备流程、关键时间节点、材料准备、面试技巧等..."),
        NewsArticle("news_1", "考研高分经验分享", "多位高分学长学姐分享他们的学习方法、时间规划、心态调整以及各科目备考策略..."),
        NewsArticle("news_2", "留学申请指南：圆梦海外名校", "涵盖选校定位、文书写作、推荐信、语言考试准备、签证申请等全流程指导..."),
        NewsArticle("news_3", "考公上岸经验：笔试面试技巧", "公务员考试笔试申论行测高分技巧，面试结构化与无领导小组讨论全方位指导..."),
        NewsArticle("news_4", "【推荐】近期热门职业发展趋势", "分析当前就业市场，解读人工智能、大数据、新能源等热门行业的发展前景与人才需求...")
        // ... 更多新闻
    )

    fun getNewsById(newsId: String?): NewsArticle? {
        return sampleNewsData.find { it.id == newsId }
    }
}
