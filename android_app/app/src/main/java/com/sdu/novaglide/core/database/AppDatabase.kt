package com.sdu.novaglide.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sdu.novaglide.data.local.dao.UserDao
import com.sdu.novaglide.data.local.entity.UserEntity
import com.sdu.novaglide.data.local.converter.DateConverter
import kotlinx.coroutines.CoroutineScope

// !! 重要: 如果您当前的数据库版本不是1，请相应地调整 MIGRATION_X_Y 中的 X 和 Y，
// !! 以及 @Database 注解中的 version。
// !! 此处假设您之前的版本是 1。

@Database(
    entities = [UserEntity::class],
    version = 2, // <--- 将版本从 1 增加到 2
    exportSchema = true // 建议设置为 true 以便导出数据库模式
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "novaglide_database" // 确保与您现有数据库名称一致

        fun getDatabase(
            context: Context,
            scope: CoroutineScope // CoroutineScope 通常用于预填充数据等
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                // 添加迁移路径
                .addMigrations(MIGRATION_1_2)
                // 如果您不想处理迁移并且可以接受数据丢失（仅限开发！），
                // 可以使用 .fallbackToDestructiveMigration()
                // .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        // 从版本 1 迁移到版本 2
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 为 users 表添加 password 列。
                // 由于 UserEntity 中的 password 是 String (非 nullable),
                // 我们需要为现有行提供一个默认值。
                // 此处使用空字符串 '' 作为默认值。
                // 在实际应用中，您需要为现有用户制定一个密码策略。
                database.execSQL("ALTER TABLE users ADD COLUMN password TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
