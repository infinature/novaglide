package com.sdu.novaglide.core.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sdu.novaglide.data.local.converters.DateConverters
import com.sdu.novaglide.data.local.dao.UserDao
import com.sdu.novaglide.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

private const val TAG = "AppDatabase"

@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    Log.d(TAG, "Creating database instance")
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "novaglide_database"
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                    INSTANCE = instance
                    Log.d(TAG, "Database instance created successfully")
                    instance
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating database: ${e.message}", e)
                    throw e
                }
            }
        }
        
        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d(TAG, "Database onCreate callback triggered")
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        try {
                            // 预先填充数据库
                            Log.d(TAG, "Prepopulating database with test data")
                            prepopulateDatabase(database.userDao())
                        } catch (e: Exception) {
                            Log.e(TAG, "Error prepopulating database: ${e.message}", e)
                        }
                    }
                }
            }
        }
        
        // 预先填充数据库
        suspend fun prepopulateDatabase(userDao: UserDao) {
            try {
                val currentTime = System.currentTimeMillis()
                val registrationTime = currentTime - (90 * 24 * 60 * 60 * 1000L)
                
                val demoUser = UserEntity(
                    userId = "U12345678",
                    username = "student2024",
                    nickname = "学习达人",
                    email = "student2024@example.com",
                    phone = "138****1234",
                    avatar = "",
                    bio = "热爱学习，备战考研",
                    registrationDate = Date(registrationTime),
                    lastLoginDate = Date(currentTime),
                    eduLevel = "本科",
                    institution = "山东大学",
                    graduationYear = 2025
                )
                
                userDao.insertUser(demoUser)
                Log.d(TAG, "Demo user inserted successfully: ${demoUser.userId}")
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting demo user: ${e.message}", e)
            }
        }
    }
}
