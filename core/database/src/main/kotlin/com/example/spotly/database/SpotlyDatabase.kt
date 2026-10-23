package com.example.spotly.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PostEntity::class, FeedEntity::class], version = 1, exportSchema = true)
abstract class SpotlyDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
    companion object {
        fun create(context: Context): SpotlyDatabase = Room.databaseBuilder(
            context.applicationContext, SpotlyDatabase::class.java, "spotly-feed.db"
        ).build()
    }
}
