package com.example.chess.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchHistoryDao {

  @Query("SELECT * FROM match_history ORDER BY timestamp DESC")
  fun getAllMatches(): Flow<List<MatchRecordEntity>>

  @Query("SELECT * FROM match_history WHERE id = :id")
  suspend fun getMatchById(id: Long): MatchRecordEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMatch(match: MatchRecordEntity): Long

  @Query("DELETE FROM match_history WHERE id = :id")
  suspend fun deleteMatchById(id: Long)

  @Query("DELETE FROM match_history")
  suspend fun clearAllMatches()

  @Query("SELECT COUNT(*) FROM match_history")
  fun getMatchCount(): Flow<Int>
}
