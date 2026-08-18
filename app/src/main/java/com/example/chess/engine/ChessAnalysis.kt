package com.example.chess.engine

import androidx.compose.ui.graphics.Color
import com.example.chess.model.GameState
import com.example.chess.model.Move
import com.example.chess.model.MoveRecord
import com.example.chess.model.PieceColor

enum class MoveQuality(
  val title: String,
  val symbol: String,
  val badgeColor: Color,
  val description: String
) {
  BRILLIANT("Brilliant", "!!", Color(0xFF06B6D4), "A spectacular, game-changing move!"),
  GREAT("Great Move", "!", Color(0xFF10B981), "A very strong and precise move!"),
  BEST("Best Move", "★", Color(0xFF22C55E), "The top engine choice."),
  EXCELLENT("Excellent", "✓", Color(0xFF84CC16), "Nearly as good as the best move."),
  GOOD("Good", "", Color(0xFF94A3B8), "A solid, playable move."),
  INACCURACY("Inaccuracy", "?!", Color(0xFFF59E0B), "A slight slip that hands over some initiative."),
  MISTAKE("Mistake", "?", Color(0xFFEA580C), "A notable error giving up an advantage."),
  BLUNDER("Blunder", "??", Color(0xFFEF4444), "A serious mistake that loses significant material or advantage.")
}

data class AnalyzedMove(
  val plyIndex: Int,
  val moveNumber: Int,
  val color: PieceColor,
  val san: String,
  val evalBeforeCentipawns: Int,
  val evalAfterCentipawns: Int,
  val quality: MoveQuality,
  val bestAlternativeSan: String? = null
)

data class GameAnalysisReport(
  val whiteAccuracy: Float,
  val blackAccuracy: Float,
  val whiteMoveCounts: Map<MoveQuality, Int>,
  val blackMoveCounts: Map<MoveQuality, Int>,
  val analyzedMoves: List<AnalyzedMove>,
  val evalGraphPoints: List<Float> // Centipawn evals mapped to -10..+10 range
)

object ChessAnalysisEngine {

  fun analyzeGame(gameState: GameState, aiEngine: ChessAiEngine): GameAnalysisReport {
    val moves = gameState.moveHistory
    if (moves.isEmpty()) {
      return GameAnalysisReport(
        whiteAccuracy = 100f,
        blackAccuracy = 100f,
        whiteMoveCounts = emptyMap(),
        blackMoveCounts = emptyMap(),
        analyzedMoves = emptyList(),
        evalGraphPoints = listOf(0f)
      )
    }

    val analyzedMoves = mutableListOf<AnalyzedMove>()
    val evalGraph = mutableListOf<Float>()
    evalGraph.add(0f)

    var currentState = GameState()
    var whiteAccPoints = 0.0
    var whiteMoveCount = 0
    var blackAccPoints = 0.0
    var blackMoveCount = 0

    val whiteCounts = mutableMapOf<MoveQuality, Int>().withDefault { 0 }
    val blackCounts = mutableMapOf<MoveQuality, Int>().withDefault { 0 }

    for (i in moves.indices) {
      val record = moves[i]
      val player = record.piece.color
      val moveNumber = (i / 2) + 1

      val evalBefore = aiEngine.evaluateBoard(currentState, PieceColor.WHITE)
      val nextState = ChessRules.applyMove(currentState, record.move)
      val evalAfter = aiEngine.evaluateBoard(nextState, PieceColor.WHITE)

      // Measure eval swing from perspective of active mover
      val swing = if (player == PieceColor.WHITE) (evalAfter - evalBefore) else (evalBefore - evalAfter)

      val quality = when {
        swing >= 150 && record.move.isCapture -> MoveQuality.BRILLIANT
        swing >= 60 -> MoveQuality.GREAT
        swing >= -15 -> MoveQuality.BEST
        swing >= -45 -> MoveQuality.EXCELLENT
        swing >= -90 -> MoveQuality.GOOD
        swing >= -160 -> MoveQuality.INACCURACY
        swing >= -280 -> MoveQuality.MISTAKE
        else -> MoveQuality.BLUNDER
      }

      // Accuracy point scaling
      val points = when (quality) {
        MoveQuality.BRILLIANT, MoveQuality.GREAT, MoveQuality.BEST -> 100.0
        MoveQuality.EXCELLENT -> 90.0
        MoveQuality.GOOD -> 75.0
        MoveQuality.INACCURACY -> 50.0
        MoveQuality.MISTAKE -> 20.0
        MoveQuality.BLUNDER -> 0.0
      }

      if (player == PieceColor.WHITE) {
        whiteAccPoints += points
        whiteMoveCount++
        whiteCounts[quality] = (whiteCounts[quality] ?: 0) + 1
      } else {
        blackAccPoints += points
        blackMoveCount++
        blackCounts[quality] = (blackCounts[quality] ?: 0) + 1
      }

      analyzedMoves.add(
        AnalyzedMove(
          plyIndex = i,
          moveNumber = moveNumber,
          color = player,
          san = record.san,
          evalBeforeCentipawns = evalBefore,
          evalAfterCentipawns = evalAfter,
          quality = quality
        )
      )

      val clampedEval = (evalAfter / 100f).coerceIn(-10f, 10f)
      evalGraph.add(clampedEval)

      currentState = nextState
    }

    val whiteAccuracy = if (whiteMoveCount > 0) (whiteAccPoints / whiteMoveCount).toFloat() else 100f
    val blackAccuracy = if (blackMoveCount > 0) (blackAccPoints / blackMoveCount).toFloat() else 100f

    return GameAnalysisReport(
      whiteAccuracy = whiteAccuracy,
      blackAccuracy = blackAccuracy,
      whiteMoveCounts = whiteCounts,
      blackMoveCounts = blackCounts,
      analyzedMoves = analyzedMoves,
      evalGraphPoints = evalGraph
    )
  }
}
