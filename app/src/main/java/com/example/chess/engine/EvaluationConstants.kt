package com.example.chess.engine

import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType

object EvaluationConstants {

  val MATERIAL_VALUES = mapOf(
    PieceType.PAWN to 100,
    PieceType.KNIGHT to 320,
    PieceType.BISHOP to 330,
    PieceType.ROOK to 500,
    PieceType.QUEEN to 900,
    PieceType.KING to 20000
  )

  // Piece-Square Tables (oriented for White, where row 0 = rank 8 and row 7 = rank 1)
  // For Black, row is mirrored: 7 - row

  val PAWN_TABLE = arrayOf(
    intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
    intArrayOf(50, 50, 50, 50, 50, 50, 50, 50),
    intArrayOf(10, 10, 20, 30, 30, 20, 10, 10),
    intArrayOf(5, 5, 10, 27, 27, 10, 5, 5),
    intArrayOf(0, 0, 0, 25, 25, 0, 0, 0),
    intArrayOf(5, -5, -10, 0, 0, -10, -5, 5),
    intArrayOf(5, 10, 10, -25, -25, 10, 10, 5),
    intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
  )

  val KNIGHT_TABLE = arrayOf(
    intArrayOf(-50, -40, -30, -30, -30, -30, -40, -50),
    intArrayOf(-40, -20, 0, 0, 0, 0, -20, -40),
    intArrayOf(-30, 0, 10, 15, 15, 10, 0, -30),
    intArrayOf(-30, 5, 15, 20, 20, 15, 5, -30),
    intArrayOf(-30, 0, 15, 20, 20, 15, 0, -30),
    intArrayOf(-30, 5, 10, 15, 15, 10, 5, -30),
    intArrayOf(-40, -20, 0, 5, 5, 0, -20, -40),
    intArrayOf(-50, -40, -30, -30, -30, -30, -40, -50)
  )

  val BISHOP_TABLE = arrayOf(
    intArrayOf(-20, -10, -10, -10, -10, -10, -10, -20),
    intArrayOf(-10, 0, 0, 0, 0, 0, 0, -10),
    intArrayOf(-10, 0, 5, 10, 10, 5, 0, -10),
    intArrayOf(-10, 5, 5, 10, 10, 5, 5, -10),
    intArrayOf(-10, 0, 10, 10, 10, 10, 0, -10),
    intArrayOf(-10, 10, 10, 10, 10, 10, 10, -10),
    intArrayOf(-10, 5, 0, 0, 0, 0, 5, -10),
    intArrayOf(-20, -10, -10, -10, -10, -10, -10, -20)
  )

  val ROOK_TABLE = arrayOf(
    intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
    intArrayOf(5, 10, 10, 10, 10, 10, 10, 5),
    intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
    intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
    intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
    intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
    intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
    intArrayOf(0, 0, 0, 5, 5, 0, 0, 0)
  )

  val QUEEN_TABLE = arrayOf(
    intArrayOf(-20, -10, -10, -5, -5, -10, -10, -20),
    intArrayOf(-10, 0, 0, 0, 0, 0, 0, -10),
    intArrayOf(-10, 0, 5, 5, 5, 5, 0, -10),
    intArrayOf(-5, 0, 5, 5, 5, 5, 0, -5),
    intArrayOf(0, 0, 5, 5, 5, 5, 0, -5),
    intArrayOf(-10, 5, 5, 5, 5, 5, 0, -10),
    intArrayOf(-10, 0, 5, 0, 0, 0, 0, -10),
    intArrayOf(-20, -10, -10, -5, -5, -10, -10, -20)
  )

  val KING_MIDDLEGAME_TABLE = arrayOf(
    intArrayOf(-30, -40, -40, -50, -50, -40, -40, -30),
    intArrayOf(-30, -40, -40, -50, -50, -40, -40, -30),
    intArrayOf(-30, -40, -40, -50, -50, -40, -40, -30),
    intArrayOf(-30, -40, -40, -50, -50, -40, -40, -30),
    intArrayOf(-20, -30, -30, -40, -40, -30, -30, -20),
    intArrayOf(-10, -20, -20, -20, -20, -20, -20, -10),
    intArrayOf(20, 20, 0, 0, 0, 0, 20, 20),
    intArrayOf(20, 30, 10, 0, 0, 10, 30, 20)
  )

  val KING_ENDGAME_TABLE = arrayOf(
    intArrayOf(-50, -40, -30, -20, -20, -30, -40, -50),
    intArrayOf(-30, -20, -10, 0, 0, -10, -20, -30),
    intArrayOf(-30, -10, 20, 30, 30, 20, -10, -30),
    intArrayOf(-30, -10, 30, 40, 40, 30, -10, -30),
    intArrayOf(-30, -10, 30, 40, 40, 30, -10, -30),
    intArrayOf(-30, -10, 20, 30, 30, 20, -10, -30),
    intArrayOf(-30, -30, 0, 0, 0, 0, -30, -30),
    intArrayOf(-50, -30, -30, -30, -30, -30, -30, -50)
  )

  fun getPieceSquareValue(type: PieceType, color: PieceColor, row: Int, col: Int, isEndgame: Boolean): Int {
    val tableRow = if (color == PieceColor.WHITE) row else 7 - row
    val tableCol = col

    return when (type) {
      PieceType.PAWN -> PAWN_TABLE[tableRow][tableCol]
      PieceType.KNIGHT -> KNIGHT_TABLE[tableRow][tableCol]
      PieceType.BISHOP -> BISHOP_TABLE[tableRow][tableCol]
      PieceType.ROOK -> ROOK_TABLE[tableRow][tableCol]
      PieceType.QUEEN -> QUEEN_TABLE[tableRow][tableCol]
      PieceType.KING -> if (isEndgame) KING_ENDGAME_TABLE[tableRow][tableCol] else KING_MIDDLEGAME_TABLE[tableRow][tableCol]
    }
  }

  // Common high-quality opening book sequences (UCI format strings)
  val OPENING_BOOK = mapOf(
    // Initial moves for White
    "" to listOf("e2e4", "d2d4", "g1f3", "c2c4"),

    // Replies to 1. e4
    "e2e4" to listOf("e7e5", "c7c5", "e7e6", "c7c6"),
    // 1. e4 e5
    "e2e4 e7e5" to listOf("g1f3", "f1c4", "b1c3"),
    "e2e4 e7e5 g1f3" to listOf("b8c6", "g8f6", "d7d6"),
    "e2e4 e7e5 g1f3 b8c6" to listOf("f1b5", "f1c4", "d2d4", "b1c3"),
    "e2e4 e7e5 g1f3 b8c6 f1b5" to listOf("a7a6", "g8f6", "d7d6"),
    "e2e4 e7e5 g1f3 b8c6 f1b5 a7a6" to listOf("b5a4", "b5c6"),
    "e2e4 e7e5 g1f3 b8c6 f1c4" to listOf("f8c5", "g8f6"),
    "e2e4 e7e5 g1f3 b8c6 f1c4 f8c5" to listOf("c2c3", "d2d3", "b1c3"),

    // Replies to Sicilian 1. e4 c5
    "e2e4 c7c5" to listOf("g1f3", "b1c3", "c2c3"),
    "e2e4 c7c5 g1f3" to listOf("d7d6", "b8c6", "e7e6", "g7g6"),
    "e2e4 c7c5 g1f3 d7d6" to listOf("d2d4"),
    "e2e4 c7c5 g1f3 d7d6 d2d4" to listOf("c5d4"),
    "e2e4 c7c5 g1f3 d7d6 d2d4 c5d4" to listOf("f3d4"),

    // Replies to 1. d4
    "d2d4" to listOf("d7d5", "g8f6", "e7e6"),
    "d2d4 d7d5" to listOf("c2c4", "g1f3", "c1f4"),
    "d2d4 d7d5 c2c4" to listOf("e7e6", "c7c6", "d5c4"),
    "d2d4 d7d5 c2c4 e7e6" to listOf("b1c3", "g1f3"),
    "d2d4 g8f6" to listOf("c2c4", "g1f3", "c1f4"),
    "d2d4 g8f6 c2c4" to listOf("e7e6", "g7g6", "c7c5"),

    // French Defense
    "e2e4 e7e6" to listOf("d2d4"),
    "e2e4 e7e6 d2d4" to listOf("d7d5"),
    "e2e4 e7e6 d2d4 d7d5" to listOf("b1c3", "e4e5", "e4d5"),

    // Caro-Kann
    "e2e4 c7c6" to listOf("d2d4"),
    "e2e4 c7c6 d2d4" to listOf("d7d5"),
    "e2e4 c7c6 d2d4 d7d5" to listOf("b1c3", "e4e5", "e4d5")
  )
}
