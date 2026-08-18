package com.example.chess.model

enum class PieceColor {
  WHITE,
  BLACK;

  fun opposite(): PieceColor = if (this == WHITE) BLACK else WHITE
  val displayName: String get() = if (this == WHITE) "White" else "Black"
}

enum class PieceType(
  val symbol: String,
  val notation: String,
  val baseValue: Int
) {
  PAWN("♙", "", 100),
  KNIGHT("♘", "N", 320),
  BISHOP("♗", "B", 330),
  ROOK("♖", "R", 500),
  QUEEN("♕", "Q", 900),
  KING("♔", "K", 20000)
}

data class Piece(
  val type: PieceType,
  val color: PieceColor
) {
  val unicodeSymbol: String
    get() = when (color) {
      PieceColor.WHITE -> when (type) {
        PieceType.PAWN -> "♙"
        PieceType.KNIGHT -> "♘"
        PieceType.BISHOP -> "♗"
        PieceType.ROOK -> "♖"
        PieceType.QUEEN -> "♕"
        PieceType.KING -> "♔"
      }
      PieceColor.BLACK -> when (type) {
        PieceType.PAWN -> "♟"
        PieceType.KNIGHT -> "♞"
        PieceType.BISHOP -> "♝"
        PieceType.ROOK -> "♜"
        PieceType.QUEEN -> "♛"
        PieceType.KING -> "♚"
      }
    }
}

data class Square(val row: Int, val col: Int) {
  init {
    require(row in 0..7 && col in 0..7) { "Square coordinates out of bounds: ($row, $col)" }
  }

  val algebraic: String
    get() {
      val file = ('a'.code + col).toChar()
      val rank = 8 - row
      return "$file$rank"
    }

  val isLightSquare: Boolean get() = (row + col) % 2 == 0

  companion object {
    fun fromAlgebraic(alg: String): Square {
      require(alg.length == 2) { "Invalid algebraic square notation: $alg" }
      val col = alg[0].lowercaseChar() - 'a'
      val row = 8 - (alg[1] - '0')
      return Square(row, col)
    }
  }
}

data class Move(
  val from: Square,
  val to: Square,
  val promotion: PieceType? = null,
  val isCapture: Boolean = false,
  val isCastling: Boolean = false,
  val isEnPassant: Boolean = false
) {
  fun toUci(): String {
    val promoStr = promotion?.notation?.lowercase() ?: ""
    return "${from.algebraic}${to.algebraic}$promoStr"
  }
}

data class MoveRecord(
  val move: Move,
  val piece: Piece,
  val capturedPiece: Piece? = null,
  val san: String,
  val boardBefore: Array<Array<Piece?>>,
  val checkState: Boolean = false,
  val checkmateState: Boolean = false
)

sealed class GameOutcome {
  data class Checkmate(val winner: PieceColor) : GameOutcome()
  data class Stalemate(val message: String = "Stalemate - No legal moves") : GameOutcome()
  data class Draw(val reason: String) : GameOutcome()
  data class Timeout(val winner: PieceColor) : GameOutcome()
  data class Resignation(val winner: PieceColor) : GameOutcome()
  data class PuzzleSolved(val ratingGain: Int = 15) : GameOutcome()

  val headline: String
    get() = when (this) {
      is Checkmate -> "${winner.displayName} Wins!"
      is Timeout -> "${winner.displayName} Wins on Time!"
      is Resignation -> "${winner.displayName} Wins by Resignation!"
      is Stalemate -> "Game Drawn!"
      is Draw -> "Game Drawn!"
      is PuzzleSolved -> "Puzzle Solved!"
    }

  val description: String
    get() = when (this) {
      is Checkmate -> "Checkmate — ${winner.displayName} has forced checkmate."
      is Timeout -> "Time forfeit — ${winner.opposite().displayName}'s clock expired."
      is Resignation -> "Resignation — ${winner.opposite().displayName} resigned."
      is Stalemate -> "Stalemate — The active player has no legal moves and is not in check."
      is Draw -> reason
      is PuzzleSolved -> "Brilliant tactical vision! Rating: +$ratingGain"
    }
}

enum class GameMode(val title: String) {
  AI_VS_HUMAN("vs Computer"),
  TWO_PLAYER("Pass & Play"),
  TACTICAL_PUZZLES("Tactical Puzzles"),
  BOARD_EDITOR("Board Setup")
}

enum class Difficulty(val title: String, val depth: Int, val elo: Int, val description: String) {
  NOVICE("Beginner", 1, 800, "Relaxed & friendly play"),
  CASUAL("Casual", 2, 1200, "Balanced club play"),
  INTERMEDIATE("Intermediate", 3, 1600, "Sharp tactical eye"),
  MASTER("Master", 4, 2000, "Deep positional play"),
  GRANDMASTER("Grandmaster", 5, 2400, "Full alpha-beta master depth")
}
