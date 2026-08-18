package com.example.chess.data

import kotlinx.coroutines.flow.Flow

class MatchHistoryRepository(private val dao: MatchHistoryDao) {

  val allMatches: Flow<List<MatchRecordEntity>> = dao.getAllMatches()
  val matchCount: Flow<Int> = dao.getMatchCount()

  suspend fun insertMatch(match: MatchRecordEntity): Long {
    return dao.insertMatch(match)
  }

  suspend fun deleteMatchById(id: Long) {
    dao.deleteMatchById(id)
  }

  suspend fun clearAllMatches() {
    dao.clearAllMatches()
  }

  suspend fun getMatchById(id: Long): MatchRecordEntity? {
    return dao.getMatchById(id)
  }
}
