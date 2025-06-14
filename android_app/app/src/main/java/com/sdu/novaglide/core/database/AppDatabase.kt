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
import com.sdu.novaglide.data.local.entity.UserEntity
import com.sdu.novaglide.data.local.entity.BrowsingHistoryEntity
import com.sdu.novaglide.data.local.entity.FavoriteArticleEntity
import com.sdu.novaglide.core.database.util.DateConverter
import kotlinx.coroutines.CoroutineScope

/**
 * 应用数据库
 */
@Database(
    entities = [UserEntity::class, BrowsingHistoryEntity::class, FavoriteArticleEntity::class],
    version = 5, // 版本号增加 (例如从4到5)
    exportSchema = true // 建议在开发中设置为 true 以便导出 schema 供迁移测试
)
@TypeConverters(DateConverter::class) // 确保此注解存在且正确
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun browsingHistoryDao(): BrowsingHistoryDao
    abstract fun favoriteArticleDao(): FavoriteArticleDao // 添加新的DAO抽象方法

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        // 数据库名称，如果因为版本问题需要强制重建，可以更改此名称
        private const val DATABASE_NAME = "novaglide_database_v5" // 例如更新名称

        // 迁移：从版本 4 到版本 5
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 为 users 表添加 isLoggedIn 列，默认值为 0 (false)
                database.execSQL("ALTER TABLE users ADD COLUMN isLoggedIn INTEGER NOT NULL DEFAULT 0")
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
                .addMigrations(MIGRATION_4_5) // 添加迁移策略
                // 如果之前的版本没有正确的迁移策略，并且您希望在开发中清除数据，
                // 可以保留 fallbackToDestructiveMigration()。
                // 但对于生产应用，应该提供所有必要的迁移。
                // .fallbackToDestructiveMigration() // 如果需要，可以保留，但优先使用 addMigrations
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
