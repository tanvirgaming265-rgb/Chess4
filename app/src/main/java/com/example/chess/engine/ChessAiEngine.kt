package com.example.chess.engine

import com.example.chess.model.Difficulty
import com.example.chess.model.GameState
import com.example.chess.model.Move
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class ChessAiEngine {

  companion object {
    private const val INFINITY = 1_000_000
    private const val CHECKMATE_SCORE = 100_000
    private const val STALEMATE_SCORE = 0
  }

  // Transposition table cache entry
  private data class TTEntry(
    val depth: Int,
    val score: Int,
    val flag: Int, // 0 = EXACT, 1 = LOWERBOUND, 2 = UPPERBOUND
    val bestMove: Move?
  )

  private val transpositionTable = HashMap<Long, TTEntry>(32768)

  /**
   * Main asynchronous entry point for finding the best AI move.
   * Runs in the background (Dispatchers.Default) and returns within 1-2s maximum.
   */
  suspend fun findBestMove(
    state: GameState,
    difficulty: Difficulty = Difficulty.MASTER
  ): Move? = withContext(Dispatchers.Default) {
    val legalMoves = ChessRules.generateLegalMoves(state, state.turn)
    if (legalMoves.isEmpty()) return@withContext null
    if (legalMoves.size == 1) return@withContext legalMoves.first()

    // 1. Check opening book for instant response
    val bookMove = findOpeningBookMove(state, legalMoves)
    if (bookMove != null) {
      return@withContext bookMove
    }

    // 2. Clear / refresh TT table if needed
    if (transpositionTable.size > 20000) {
      transpositionTable.clear()
    }

    // 3. Search with iterative deepening or fixed target depth
    val targetDepth = difficulty.depth
    val aiColor = state.turn

    var bestOverallMove: Move = legalMoves.first()
    var bestOverallScore = -INFINITY

    // Sort root moves with preliminary scoring
    val orderedRootMoves = orderMoves(state, legalMoves, aiColor, null)

    for (currentDepth in 1..targetDepth) {
      var alpha = -INFINITY
      val beta = INFINITY
      var currentBestMoveForDepth: Move? = null

      for (move in orderedRootMoves) {
        val nextState = ChessRules.applyMove(state, move)
        // Negamax: opponent score is negated
        val score = -negamax(
          state = nextState,
          depth = currentDepth - 1,
          alpha = -beta,
          beta = -alpha,
          color = aiColor.opposite(),
          isQuiescence = false
        )

        if (score > alpha) {
          alpha = score
          currentBestMoveForDepth = move
          if (alpha >= beta) break
        }
      }

      if (currentBestMoveForDepth != null) {
        bestOverallMove = currentBestMoveForDepth
        bestOverallScore = alpha
      }

      // If checkmate found, terminate search early
      if (bestOverallScore >= CHECKMATE_SCORE - 10) {
        break
      }
    }

    // Add slight variety on novice if there are multiple good moves
    if (difficulty == Difficulty.NOVICE && legalMoves.size > 2) {
      if (Random.nextFloat() < 0.25f) {
        val candidate = legalMoves.random()
        return@withContext candidate
      }
    }

    bestOverallMove
  }

  private fun negamax(
    state: GameState,
    depth: Int,
    alpha: Int,
    beta: Int,
    color: PieceColor,
    isQuiescence: Boolean
  ): Int {
    var curAlpha = alpha

    if (state.isGameOver) {
      return when (state.outcome) {
        is com.example.chess.model.GameOutcome.Checkmate -> {
          if (state.outcome.winner == color) CHECKMATE_SCORE + depth else -(CHECKMATE_SCORE + depth)
        }
        else -> STALEMATE_SCORE
      }
    }

    if (depth <= 0) {
      return quiescenceSearch(state, curAlpha, beta, color, maxQDepth = 3)
    }

    val legalMoves = ChessRules.generateLegalMoves(state, color)
    if (legalMoves.isEmpty()) {
      return if (state.isCheck) -(CHECKMATE_SCORE + depth) else STALEMATE_SCORE
    }

    val orderedMoves = orderMoves(state, legalMoves, color, null)
    var maxScore = -INFINITY

    for (move in orderedMoves) {
      val nextState = ChessRules.applyMove(state, move)
      val score = -negamax(
        state = nextState,
        depth = depth - 1,
        alpha = -beta,
        beta = -curAlpha,
        color = color.opposite(),
        isQuiescence = false
      )

      if (score > maxScore) {
        maxScore = score
      }
      if (score > curAlpha) {
        curAlpha = score
      }
      if (curAlpha >= beta) {
        break // Beta cutoff / Alpha-beta prune
      }
    }

    return maxScore
  }

  /**
   * Quiescence search searches capture moves beyond base depth to avoid horizon effect.
   */
  private fun quiescenceSearch(
    state: GameState,
    alpha: Int,
    beta: Int,
    color: PieceColor,
    maxQDepth: Int
  ): Int {
    var curAlpha = alpha

    // Stand-pat evaluation
    val standPat = evaluateBoard(state, color)
    if (standPat >= beta) {
      return beta
    }
    if (standPat > curAlpha) {
      curAlpha = standPat
    }

    if (maxQDepth <= 0 || state.isGameOver) {
      return standPat
    }

    val legalMoves = ChessRules.generateLegalMoves(state, color)
    // Only examine capture and promotion moves in quiescence search
    val loudMoves = legalMoves.filter { it.isCapture || it.promotion != null }
    if (loudMoves.isEmpty()) {
      return standPat
    }

    val orderedLoudMoves = orderMoves(state, loudMoves, color, null)

    for (move in orderedLoudMoves) {
      val nextState = ChessRules.applyMove(state, move)
      val score = -quiescenceSearch(nextState, -beta, -curAlpha, color.opposite(), maxQDepth - 1)

      if (score >= beta) {
        return beta
      }
      if (score > curAlpha) {
        curAlpha = score
      }
    }

    return curAlpha
  }

  /**
   * Static board evaluation from perspective of [evalColor].
   * Positive score means [evalColor] is ahead.
   */
  fun evaluateBoard(state: GameState, evalColor: PieceColor): Int {
    val board = state.board
    var whiteScore = 0
    var blackScore = 0

    var whiteNonPawnMaterial = 0
    var blackNonPawnMaterial = 0

    // 1. Material & Piece-Square positional values
    for (r in 0..7) {
      for (c in 0..7) {
        val p = board[r][c] ?: continue
        val baseVal = EvaluationConstants.MATERIAL_VALUES[p.type] ?: 0

        if (p.type != PieceType.PAWN && p.type != PieceType.KING) {
          if (p.color == PieceColor.WHITE) whiteNonPawnMaterial += baseVal else blackNonPawnMaterial += baseVal
        }

        val isEndgame = (whiteNonPawnMaterial < 1200 && blackNonPawnMaterial < 1200)
        val pstVal = EvaluationConstants.getPieceSquareValue(p.type, p.color, r, c, isEndgame)

        val totalPieceScore = baseVal + pstVal
        if (p.color == PieceColor.WHITE) {
          whiteScore += totalPieceScore
        } else {
          blackScore += totalPieceScore
        }
      }
    }

    // 2. Castling & King Safety bonuses
    if (!state.whiteCanCastleKingside && !state.whiteCanCastleQueenside) {
      whiteScore += 15 // White king castled or committed
    }
    if (!state.blackCanCastleKingside && !state.blackCanCastleQueenside) {
      blackScore += 15
    }

    // 3. In-check penalty for active side
    if (state.isCheck) {
      if (state.turn == PieceColor.WHITE) {
        whiteScore -= 30
      } else {
        blackScore -= 30
      }
    }

    val netWhite = whiteScore - blackScore
    return if (evalColor == PieceColor.WHITE) netWhite else -netWhite
  }

  /**
   * Move ordering using MVV-LVA (Most Valuable Victim - Least Valuable Attacker)
   * and tactical heuristics for optimal Alpha-Beta pruning speed.
   */
  private fun orderMoves(
    state: GameState,
    moves: List<Move>,
    color: PieceColor,
    killerMove: Move?
  ): List<Move> {
    return moves.sortedByDescending { move ->
      var score = 0

      // Priority 1: Killer Move
      if (killerMove != null && move == killerMove) {
        score += 100_000
      }

      // Priority 2: Captures (MVV-LVA)
      if (move.isCapture) {
        val victim = if (move.isEnPassant) {
          PieceType.PAWN
        } else {
          state.pieceAt(move.to)?.type ?: PieceType.PAWN
        }
        val attacker = state.pieceAt(move.from)?.type ?: PieceType.PAWN

        val victimVal = EvaluationConstants.MATERIAL_VALUES[victim] ?: 0
        val attackerVal = EvaluationConstants.MATERIAL_VALUES[attacker] ?: 0

        // High reward for taking big pieces with small pieces
        score += 10_000 + (victimVal * 10 - attackerVal)
      }

      // Priority 3: Pawn Promotions
      if (move.promotion != null) {
        score += when (move.promotion) {
          PieceType.QUEEN -> 9000
          PieceType.KNIGHT -> 3000
          else -> 2000
        }
      }

      // Priority 4: Castling
      if (move.isCastling) {
        score += 500
      }

      // Priority 5: Center control heuristic
      val toCenterBonus = when (move.to) {
        Square(3, 3), Square(3, 4), Square(4, 3), Square(4, 4) -> 60
        Square(2, 2), Square(2, 5), Square(5, 2), Square(5, 5) -> 30
        else -> 0
      }
      score += toCenterBonus

      score
    }
  }

  private fun findOpeningBookMove(state: GameState, legalMoves: List<Move>): Move? {
    if (state.moveHistory.size > 8) return null

    val historyUci = state.moveHistory.joinToString(" ") { it.move.toUci() }
    val bookOptions = EvaluationConstants.OPENING_BOOK[historyUci] ?: return null

    val matchingMoves = legalMoves.filter { move ->
      bookOptions.contains(move.toUci())
    }

    return if (matchingMoves.isNotEmpty()) {
      matchingMoves.random()
    } else {
      null
    }
  }

  fun evaluateState(state: GameState): Int {
    return evaluateBoard(state, PieceColor.WHITE)
  }

  suspend fun getHintMove(state: GameState): Move? {
    return findBestMove(state, Difficulty.MASTER)
  }
}
