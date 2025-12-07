package com.assisment.newschatprofileapp.data.local.database

import com.assisment.newschatprofileapp.data.local.dao.ArticleDao
import com.assisment.newschatprofileapp.data.local.dao.MessageDao
import com.assisment.newschatprofileapp.data.local.entity.ArticleEntity
import com.assisment.newschatprofileapp.data.local.entity.MessageEntity




import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ArticleEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun messageDao(): MessageDao
}