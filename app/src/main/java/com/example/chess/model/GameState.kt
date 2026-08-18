package com.example.chess.model

data class GameState(
  val board: Array<Array<Piece?>> = createInitialBoard(),
  val turn: PieceColor = PieceColor.WHITE,
  val whiteCanCastleKingside: Boolean = true,
  val whiteCanCastleQueenside: Boolean = true,
  val blackCanCastleKingside: Boolean = true,
  val blackCanCastleQueenside: Boolean = true,
  val enPassantTarget: Square? = null,
  val halfmoveClock: Int = 0,
  val fullmoveNumber: Int = 1,
  val moveHistory: List<MoveRecord> = emptyList(),
  val capturedByWhite: List<Piece> = emptyList(),
  val capturedByBlack: List<Piece> = emptyList(),
  val isCheck: Boolean = false,
  val isGameOver: Boolean = false,
  val outcome: GameOutcome? = null
) {
  fun pieceAt(square: Square): Piece? = board[square.row][square.col]
  fun pieceAt(row: Int, col: Int): Piece? = board[row][col]

  fun copyBoard(): Array<Array<Piece?>> {
    return Array(8) { r ->
      Array(8) { c ->
        board[r][c]
      }
    }
  }

  val materialAdvantageWhite: Int
    get() {
      var whiteVal = 0
      var blackVal = 0
      for (r in 0..7) {
        for (c in 0..7) {
          val p = board[r][c] ?: continue
          when (p.color) {
            PieceColor.WHITE -> whiteVal += p.type.baseValue
            PieceColor.BLACK -> blackVal += p.type.baseValue
          }
        }
      }
      return (whiteVal - blackVal) / 100
    }

  companion object {
    fun createInitialBoard(): Array<Array<Piece?>> {
      val b = Array(8) { Array<Piece?>(8) { null } }

      // Black pieces (row 0: rank 8, row 1: rank 7)
      b[0][0] = Piece(PieceType.ROOK, PieceColor.BLACK)
      b[0][1] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
      b[0][2] = Piece(PieceType.BISHOP, PieceColor.BLACK)
      b[0][3] = Piece(PieceType.QUEEN, PieceColor.BLACK)
      b[0][4] = Piece(PieceType.KING, PieceColor.BLACK)
      b[0][5] = Piece(PieceType.BISHOP, PieceColor.BLACK)
      b[0][6] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
      b[0][7] = Piece(PieceType.ROOK, PieceColor.BLACK)
      for (c in 0..7) {
        b[1][c] = Piece(PieceType.PAWN, PieceColor.BLACK)
      }

      // White pieces (row 6: rank 2, row 7: rank 1)
      for (c in 0..7) {
        b[6][c] = Piece(PieceType.PAWN, PieceColor.WHITE)
      }
      b[7][0] = Piece(PieceType.ROOK, PieceColor.WHITE)
      b[7][1] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
      b[7][2] = Piece(PieceType.BISHOP, PieceColor.WHITE)
      b[7][3] = Piece(PieceType.QUEEN, PieceColor.WHITE)
      b[7][4] = Piece(PieceType.KING, PieceColor.WHITE)
      b[7][5] = Piece(PieceType.BISHOP, PieceColor.WHITE)
      b[7][6] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
      b[7][7] = Piece(PieceType.ROOK, PieceColor.WHITE)

      return b
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is GameState) return false

    if (turn != other.turn) return false
    if (whiteCanCastleKingside != other.whiteCanCastleKingside) return false
    if (whiteCanCastleQueenside != other.whiteCanCastleQueenside) return false
    if (blackCanCastleKingside != other.blackCanCastleKingside) return false
    if (blackCanCastleQueenside != other.blackCanCastleQueenside) return false
    if (enPassantTarget != other.enPassantTarget) return false
    if (halfmoveClock != other.halfmoveClock) return false
    if (fullmoveNumber != other.fullmoveNumber) return false
    if (isCheck != other.isCheck) return false
    if (isGameOver != other.isGameOver) return false
    if (outcome != other.outcome) return false

    for (r in 0..7) {
      for (c in 0..7) {
        if (board[r][c] != other.board[r][c]) return false
      }
    }
    return true
  }

  override fun hashCode(): Int {
    var result = turn.hashCode()
    result = 31 * result + isCheck.hashCode()
    result = 31 * result + isGameOver.hashCode()
    return result
  }
}
