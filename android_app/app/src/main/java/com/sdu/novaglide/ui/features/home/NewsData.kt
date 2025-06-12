// (如果这个文件不存在，您可以创建一个类似的文件来存放新闻数据，或者从您的实际数据源获取)
package com.sdu.novaglide.ui.features.home

data class NewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val source: String,
    val publishTime: Long,
    val category: String
)

object NewsRepository {
    // 后续将通过网络获取真实数据
    fun getNewsById(newsId: String?, newsList: List<NewsArticle>): NewsArticle? {
        return newsList.find { it.id == newsId }
    }
}
