package com.sdu.novaglide.data.repository

import com.sdu.novaglide.domain.model.News
import com.sdu.novaglide.domain.repository.NewsRepository
import kotlinx.coroutines.delay
import java.util.Date
import java.util.UUID
import kotlin.random.Random

/**
 * 模拟新闻仓库实现，用于在真实API接入前提供测试数据
 */
class FakeNewsRepositoryImpl : NewsRepository {
    
    private val newsCategories = listOf("保研", "考研", "留学", "考公", "推荐")
    private val newsSources = listOf("教育部", "人民日报", "中国教育在线", "山东大学", "考研指南")
    
    override suspend fun getNewsByCategory(
        category: String,
        page: Int,
        pageSize: Int
    ): Result<Pair<List<News>, Boolean>> {
        // 模拟网络延迟
        delay(800)
        
        // 当页码超过3时不再返回更多数据，模拟分页结束
        val hasMorePages = page < 3
        
        // 生成模拟新闻数据
        val newsList = if (page <= 3) {
            generateMockNewsItems(category, pageSize)
        } else {
            emptyList()
        }
        
        return Result.success(Pair(newsList, hasMorePages))
    }
    
    override suspend fun searchNews(
        query: String,
        category: String?,
        page: Int,
        pageSize: Int
    ): Result<Pair<List<News>, Boolean>> {
        // 模拟网络延迟
        delay(1000)
        
        // 生成模拟搜索结果
        val hasMorePages = page < 2
        
        // 过滤出包含搜索关键词的新闻
        val newsList = if (page <= 2) {
            val mockNews = generateMockNewsItems(category ?: "推荐", pageSize)
            mockNews.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.summary.contains(query, ignoreCase = true) 
            }
        } else {
            emptyList()
        }
        
        return Result.success(Pair(newsList, hasMorePages))
    }
    
    private fun generateMockNewsItems(category: String, count: Int): List<News> {
        val currentTime = Date()
        return List(count) { index ->
            val randomHours = Random.nextInt(1, 48)
            val publishDate = Date(currentTime.time - randomHours * 3600 * 1000)
            
            val newsCategory = if (category == "推荐") newsCategories.random() else category
            val titlePrefix = when (newsCategory) {
                "保研" -> listOf("保研政策更新:", "2025年保研指南:", "名校保研经验分享:", "导师青睐什么样的保研生?", "保研面试全攻略:").random()
                "考研" -> listOf("考研备考规划:", "考研英语高分技巧:", "专业课复习方法:", "考研调剂指南:", "名校考研经验:").random()
                "留学" -> listOf("留学申请全攻略:", "海外名校录取条件:", "雅思备考指南:", "留学生活经验分享:", "海外奖学金申请:").random()
                "考公" -> listOf("公务员考试大纲发布:", "国考省考报名时间:", "公考面试技巧:", "基层公务员工作体验:", "事业单位考试:").random()
                else -> listOf("高校招生政策:", "就业形势分析:", "求职简历制作:", "考证指南:", "学科竞赛:").random()
            }
            
            News(
                id = UUID.randomUUID().toString(),
                title = "$titlePrefix ${generateRandomTitle(newsCategory)}",
                summary = generateRandomSummary(newsCategory),
                content = generateRandomContent(newsCategory),
                source = newsSources.random(),
                publishDate = publishDate,
                category = newsCategory,
                imageUrl = null
            )
        }
    }
    
    private fun generateRandomTitle(category: String): String {
        return when (category) {
            "保研" -> listOf(
                "清北复交强基计划2025年招生简章公布",
                "高校研究生推免新政策解读与分析",
                "保研夏令营申请技巧全面分析",
                "各高校保研率大揭秘",
                "CS专业保研难度分析与建议"
            ).random()
            "考研" -> listOf(
                "2026考研时间表出炉，这些关键节点要记牢",
                "考研数学备考技巧与常见误区分析",
                "考研英语词汇记忆法大全",
                "408专业课复习指南与真题解析",
                "考研复试准备攻略与注意事项"
            ).random()
            "留学" -> listOf(
                "2025年美国大学申请指南与录取趋势",
                "英国G5名校申请条件与录取率分析",
                "留学文书写作关键技巧",
                "海外高校奖学金申请全攻略",
                "留学生活成本与打工政策解读"
            ).random()
            "考公" -> listOf(
                "2025年国考职位分析与备考建议",
                "公务员面试高分技巧与实战模拟",
                "省考与国考的区别与备考策略",
                "基层公务员工作体验与晋升路径",
                "公考行测逻辑题型破解方法"
            ).random()
            else -> listOf(
                "2025年就业形势分析与热门行业预测",
                "考证指南：哪些证书最具含金量?",
                "研究生与就业的关系：学历真的越高越好吗？",
                "实习经历对求职的重要性与获取途径",
                "跨专业发展指南：如何成功转行"
            ).random()
        }
    }
    
    private fun generateRandomSummary(category: String): String {
        return when (category) {
            "保研" -> listOf(
                "本文详细介绍了2025年保研政策的最新变化及应对策略，帮助考生了解保研流程与注意事项。",
                "名校保研经验分享，教你如何在激烈的竞争中脱颖而出，赢得导师青睐。",
                "保研夏令营是重要的敲门砖，本文分析各高校夏令营的特点及备战技巧。",
                "学科竞赛对保研的重要性及如何有效准备各类学科竞赛，提高保研竞争力。",
                "保研面试常见问题及回答技巧，让你在面试中表现出色，赢得导师好感。"
            ).random()
            "考研" -> listOf(
                "考研数学复习分三阶段，本文详解各阶段学习重点与复习技巧，助你攻克数学难关。",
                "考研英语词汇是基础，文章分享科学高效的记忆方法与背诵策略，助你突破词汇瓶颈。",
                "专业课复习是考研的重中之重，文章分析各专业复习方法与资料选择，帮你制定有效复习计划。",
                "考研政治复习技巧与热点分析，教你如何在有限时间内高效备考政治科目。",
                "考研调剂全攻略，从调剂流程、院校选择到面试准备，助你顺利完成调剂。"
            ).random()
            "留学" -> listOf(
                "美国研究生申请全流程解析，从选校、准备材料到面试技巧，助你赢得名校录取。",
                "英国留学费用全面分析，包括学费、生活费及奖学金申请途径，帮你做好留学预算。",
                "雅思备考指南，分享听说读写各部分的备考策略与提分技巧，助你达到理想分数。",
                "留学文书写作技巧，教你如何撰写打动招生官的个人陈述与研究计划，提高申请成功率。",
                "海外实习与就业指南，介绍留学生如何在海外获取实习机会并实现成功就业。"
            ).random()
            "考公" -> listOf(
                "国考行测备考技巧，分享各类题型的解题方法与时间分配策略，助你提高行测得分。",
                "公务员面试高分技巧，包括结构化面试与无领导小组讨论的应对策略，助你在面试中脱颖而出。",
                "2025年国考职位分析，详解各部门职位特点与竞争情况，帮助考生合理选择报考职位。",
                "公务员考试时间规划，从备考到面试的全流程时间安排，助你高效备考。",
                "基层公务员工作体验分享，解析基层工作特点与晋升路径，帮助考生了解公务员职业发展。"
            ).random()
            else -> listOf(
                "大学生求职指南，分享简历制作、面试技巧及就业形势分析，助你顺利就业。",
                "考证攻略：分析各类证书的含金量与考取难度，帮你选择最适合的证书提升竞争力。",
                "实习对就业的重要性及如何获取优质实习机会，提前积累职场经验。",
                "跨专业发展指南，分享不同专业的转行经验与技能培养方法，助你成功转型。",
                "互联网行业就业分析，解读各细分领域的人才需求与薪资水平，为IT专业学生提供就业参考。"
            ).random()
        }
    }
    
    private fun generateRandomContent(category: String): String {
        val base = generateRandomSummary(category)
        // 扩展内容，模拟长文章
        return """
            $base
            
            一、背景介绍
            
            近年来，随着高等教育的普及和就业市场的变化，${category}成为了大学生关注的热点话题。无论是为了继续深造还是寻求更好的就业机会，合理规划和准备都至关重要。
            
            二、现状分析
            
            目前${category}领域面临的主要挑战包括竞争激烈、政策变化频繁以及疫情带来的不确定性。据统计，2024年全国${category}的报名人数比去年增长了15%，竞争更加激烈。
            
            三、应对策略
            
            1. 提前规划：至少提前一年开始准备，制定详细的学习计划和目标。
            
            2. 资源整合：充分利用学校资源、网络平台和前辈经验，多方面获取信息。
            
            3. 能力提升：除了专业知识外，还要注重综合能力的培养，如沟通能力、领导力和创新思维。
            
            4. 心态调整：保持积极乐观的态度，做好应对挫折的心理准备。
            
            四、未来展望
            
            随着国家政策的调整和社会需求的变化，${category}领域将呈现出新的趋势和机遇。预计未来几年，跨学科人才和具有国际视野的复合型人才将更受青睐。
            
            五、专家建议
            
            著名教育专家王教授建议，学生在准备${category}的过程中，应该"既要仰望星空，也要脚踏实地"，既要有远大的目标，也要脚踏实地做好每一步准备工作。
            
            总之，${category}是一条充满挑战但也充满机遇的道路。只要有明确的目标、科学的方法和持之以恒的努力，相信每一位学子都能在这条道路上取得成功。
        """.trimIndent()
    }
}
