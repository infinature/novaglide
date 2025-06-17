package com.sdu.novaglide.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sdu.novaglide.data.local.dao.UserDao
import com.sdu.novaglide.data.local.dao.BrowsingHistoryDao
import com.sdu.novaglide.data.local.dao.FavoriteArticleDao
import com.sdu.novaglide.data.local.dao.NewsArticleDao // Import new DAO
import com.sdu.novaglide.data.local.entity.UserEntity
import com.sdu.novaglide.data.local.entity.BrowsingHistoryEntity
import com.sdu.novaglide.data.local.entity.FavoriteArticleEntity
import com.sdu.novaglide.data.local.entity.NewsArticleEntity // Import new Entity
import com.sdu.novaglide.core.database.util.DateConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull // Ensure this import is present and correct
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.Executors

/**
 * 应用数据库
 */
@Database(
    entities = [UserEntity::class, BrowsingHistoryEntity::class, FavoriteArticleEntity::class, NewsArticleEntity::class], // Add NewsArticleEntity
    version = 6, // Incremented version
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun browsingHistoryDao(): BrowsingHistoryDao
    abstract fun favoriteArticleDao(): FavoriteArticleDao
    abstract fun newsArticleDao(): NewsArticleDao // Add new DAO abstract method

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "novaglide_database_v6" // Updated database name for clarity

        // Migration from version 4 to 5 (already exists)
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN isLoggedIn INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migration from version 5 to 6
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `news_articles` (
                        `articleId` TEXT NOT NULL, 
                        `title` TEXT NOT NULL, 
                        `contentSnippet` TEXT NOT NULL, 
                        `category` TEXT NOT NULL, 
                        `publishDate` INTEGER NOT NULL, 
                        `imageUrl` TEXT, 
                        `source` TEXT, 
                        `recommendationScore` REAL NOT NULL, 
                        PRIMARY KEY(`articleId`)
                    )
                """)
            }
        }

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6) // Add new migration
                .addCallback(object : Callback() { // Add callback to populate data
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // If the DB is created for the first time (no migrations run)
                        // This is also a good place if you want to ensure data exists even after migrations
                        // For simplicity, we'll use a one-time executor here.
                        // In a real app, you might inject a dispatcher.
                        Executors.newSingleThreadExecutor().execute {
                            INSTANCE?.let { database ->
                                val newsDao = database.newsArticleDao()
                                // Populate initial news articles
                                scope.launch { // Use the provided scope
                                    newsDao.insertAllArticles(getSampleNewsArticles())
                                }
                            }
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // This callback is invoked every time the database is opened.
                        // We can check if the news table is empty and populate if needed.
                        // This ensures data is present even if the app was updated and migrations ran.
                        INSTANCE?.let { database ->
                             scope.launch { // Use the provided scope
                                val newsDao = database.newsArticleDao()
                                // Correctly import and use firstOrNull
                                val articles: List<NewsArticleEntity>? = newsDao.getAllArticlesSortedByScore().firstOrNull()
                                if (articles.isNullOrEmpty()) {
                                    newsDao.insertAllArticles(getSampleNewsArticles())
                                }
                            }
                        }
                    }
                })
                // .fallbackToDestructiveMigration() // Avoid if migrations are set up
                .build()
                INSTANCE = instance
                instance
            }
        }

        // Helper function to get sample news articles
        fun getSampleNewsArticles(): List<NewsArticleEntity> {
            val currentTime = System.currentTimeMillis()
            return listOf(
                NewsArticleEntity("N001", "探索宇宙的奥秘：韦伯望远镜的新发现", "韦伯望远镜最近捕捉到了早期宇宙的惊人图像，揭示了前所未见的星系结构。", "科技", currentTime - 86400000L * 1, "url_to_image1.jpg", "天文在线", 0.9),
                NewsArticleEntity("N002", "AI医疗的突破：早期癌症诊断成为可能", "一种新型AI算法在医学影像分析中展现出高精度，有望大幅提高癌症早期诊断率。", "健康", currentTime - 86400000L * 2, "url_to_image2.jpg", "健康时报", 0.85),
                NewsArticleEntity("N003", "全球经济展望：挑战与机遇并存", "分析师指出，尽管面临通胀压力，全球经济在新能源和数字转型领域仍有增长潜力。", "财经", currentTime - 86400000L * 3, "url_to_image3.jpg", "财经观察", 0.7),
                NewsArticleEntity("N004", "体育盛事：某某联赛总决赛精彩回顾", "在激动人心的总决赛中，某某队凭借关键球员的出色发挥夺得冠军。", "体育", currentTime - 86400000L * 1, null, "体育快讯", 0.92),
                NewsArticleEntity("N005", "环保进行时：可再生能源的未来", "随着技术进步和政策支持，太阳能和风能等可再生能源在全球能源结构中的比重持续上升。", "科技", currentTime - 86400000L * 4, "url_to_image5.jpg", "绿色地球", 0.78),
                NewsArticleEntity("N006", "文化之旅：探寻失落的古代文明", "考古学家在某地有了重大发现，为研究一个鲜为人知的古代文明提供了新的线索。", "文化", currentTime - 86400000L * 5, "url_to_image6.jpg", "历史探索", 0.65),
                NewsArticleEntity("N007", "美食推荐：城市角落的隐藏菜单", "这家不起眼的小店凭借其独特的创意菜品和温馨的氛围，在本地食客中赢得了口碑。", "生活", currentTime - 86400000L * 2, "url_to_image7.jpg", "美食指南", 0.88),
                NewsArticleEntity("N008", "教育新动向：个性化学习平台的崛起", "越来越多的教育机构开始采用AI驱动的个性化学习平台，以满足不同学生的学习需求。", "教育", currentTime - 86400000L * 6, null, "教育前沿", 0.75),
                NewsArticleEntity("N009", "深度解读：量子计算将如何改变世界", "专家访谈，深入探讨量子计算的原理、当前进展及其在各行各业的潜在应用。", "科技", currentTime - 86400000L * 3, "url_to_image9.jpg", "科技先锋", 0.95),
                NewsArticleEntity("N010", "艺术展览：当代艺术家的视觉盛宴", "本月，一场汇集了多位知名当代艺术家作品的展览在市美术馆开幕，吸引了众多艺术爱好者。", "艺术", currentTime - 86400000L * 1, "url_to_image10.jpg", "艺术评论", 0.82)
            )
        }
    }
}
