package com.sdu.novaglide.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    version = 4, // 版本号再次增加 (例如从3到4)
    exportSchema = false
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
        private const val DATABASE_NAME = "novaglide_database_v4" // 例如更新名称

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
                // 由于数据库版本已从1（假设）升级到2（因为添加了BrowsingHistoryEntity），
                // 您需要一个迁移策略或允许破坏性迁移。
                // fallbackToDestructiveMigration 会在版本不匹配且没有迁移路径时删除并重建数据库。
                // 这对于开发阶段很方便，但会清除所有现有数据。
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
