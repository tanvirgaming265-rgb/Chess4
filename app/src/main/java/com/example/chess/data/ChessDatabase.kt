package com.example.chess.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MatchRecordEntity::class], version = 1, exportSchema = false)
abstract class ChessDatabase : RoomDatabase() {

  abstract fun matchHistoryDao(): MatchHistoryDao

  companion object {
    @Volatile
    private var INSTANCE: ChessDatabase? = null

    fun getDatabase(context: Context): ChessDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          ChessDatabase::class.java,
          "chess_master_database.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
