package com.example.chess.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_history")
data class MatchRecordEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val gameMode: String,
  val difficulty: String? = null,
  val playerColor: String,
  val timeControl: String,
  val resultHeadline: String,
  val resultReason: String,
  val winnerColor: String? = null, // "WHITE", "BLACK", or "DRAW"
  val totalMoves: Int,
  val pgn: String,
  val openingName: String? = null,
  val openingEco: String? = null,
  val startFen: String,
  val finalFen: String,
  val durationSeconds: Long = 0,
  val whiteCapturedCount: Int = 0,
  val blackCapturedCount: Int = 0
)
