package com.example.chess.engine

import com.example.chess.model.GameOutcome
import com.example.chess.model.GameState
import com.example.chess.model.Move
import com.example.chess.model.MoveRecord
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Square

object ChessRules {

  private val KNIGHT_OFFSETS = arrayOf(
    intArrayOf(-2, -1), intArrayOf(-2, 1),
    intArrayOf(-1, -2), intArrayOf(-1, 2),
    intArrayOf(1, -2), intArrayOf(1, 2),
    intArrayOf(2, -1), intArrayOf(2, 1)
  )

  private val BISHOP_DIRECTIONS = arrayOf(
    intArrayOf(-1, -1), intArrayOf(-1, 1),
    intArrayOf(1, -1), intArrayOf(1, 1)
  )

  private val ROOK_DIRECTIONS = arrayOf(
    intArrayOf(-1, 0), intArrayOf(1, 0),
    intArrayOf(0, -1), intArrayOf(0, 1)
  )

  private val KING_DIRECTIONS = arrayOf(
    intArrayOf(-1, -1), intArrayOf(-1, 0), intArrayOf(-1, 1),
    intArrayOf(0, -1), intArrayOf(0, 1),
    intArrayOf(1, -1), intArrayOf(1, 0), intArrayOf(1, 1)
  )

  fun getLegalMoves(state: GameState): List<Move> {
    return generateLegalMoves(state, state.turn)
  }

  fun getLegalMovesForSquare(state: GameState, square: Square): List<Move> {
    val piece = state.pieceAt(square) ?: return emptyList()
    if (piece.color != state.turn) return emptyList()
    return getLegalMoves(state).filter { it.from == square }
  }

  fun generatePseudoLegalMoves(state: GameState, color: PieceColor): List<Move> {
    val moves = ArrayList<Move>(64)
    val board = state.board

    for (r in 0..7) {
      for (c in 0..7) {
        val piece = board[r][c] ?: continue
        if (piece.color != color) continue
        val from = Square(r, c)

        when (piece.type) {
          PieceType.PAWN -> generatePawnMoves(state, from, color, moves)
          PieceType.KNIGHT -> generateKnightMoves(board, from, color, moves)
          PieceType.BISHOP -> generateSlidingMoves(board, from, color, BISHOP_DIRECTIONS, moves)
          PieceType.ROOK -> generateSlidingMoves(board, from, color, ROOK_DIRECTIONS, moves)
          PieceType.QUEEN -> {
            generateSlidingMoves(board, from, color, BISHOP_DIRECTIONS, moves)
            generateSlidingMoves(board, from, color, ROOK_DIRECTIONS, moves)
          }
          PieceType.KING -> {
            generateKingMoves(board, from, color, moves)
            generateCastlingMoves(state, from, color, moves)
          }
        }
      }
    }
    return moves
  }

  fun generateLegalMoves(state: GameState, color: PieceColor): List<Move> {
    val pseudoMoves = generatePseudoLegalMoves(state, color)
    val legalMoves = ArrayList<Move>(pseudoMoves.size)

    for (move in pseudoMoves) {
      if (isMoveLegal(state, move, color)) {
        legalMoves.add(move)
      }
    }
    return legalMoves
  }

  private fun isMoveLegal(state: GameState, move: Move, color: PieceColor): Boolean {
    val board = state.board
    val piece = board[move.from.row][move.from.col] ?: return false

    // Simulate move on a cloned board
    val tempBoard = Array(8) { r -> Array(8) { c -> board[r][c] } }

    // Execute move
    tempBoard[move.to.row][move.to.col] = if (move.promotion != null) {
      Piece(move.promotion, color)
    } else {
      piece
    }
    tempBoard[move.from.row][move.from.col] = null

    // En passant capture removal
    if (move.isEnPassant) {
      val capturedPawnRow = move.from.row
      val capturedPawnCol = move.to.col
      tempBoard[capturedPawnRow][capturedPawnCol] = null
    }

    // Castling rook move
    if (move.isCastling) {
      if (move.to.col == 6) { // Kingside
        val rook = tempBoard[move.from.row][7]
        tempBoard[move.from.row][5] = rook
        tempBoard[move.from.row][7] = null
      } else if (move.to.col == 2) { // Queenside
        val rook = tempBoard[move.from.row][0]
        tempBoard[move.from.row][3] = rook
        tempBoard[move.from.row][0] = null
      }
    }

    // Check if King is in check after move
    val kingPos = findKing(tempBoard, color) ?: return false
    return !isSquareAttacked(tempBoard, kingPos.row, kingPos.col, color.opposite())
  }

  fun isKingInCheck(board: Array<Array<Piece?>>, color: PieceColor): Boolean {
    val kingPos = findKing(board, color) ?: return false
    return isSquareAttacked(board, kingPos.row, kingPos.col, color.opposite())
  }

  fun isKingInCheck(state: GameState, color: PieceColor): Boolean {
    return isKingInCheck(state.board, color)
  }

  fun findKing(board: Array<Array<Piece?>>, color: PieceColor): Square? {
    for (r in 0..7) {
      for (c in 0..7) {
        val p = board[r][c]
        if (p != null && p.type == PieceType.KING && p.color == color) {
          return Square(r, c)
        }
      }
    }
    return null
  }

  fun isSquareAttacked(
    board: Array<Array<Piece?>>,
    targetRow: Int,
    targetCol: Int,
    attackerColor: PieceColor
  ): Boolean {
    // 1. Attacked by Pawns
    val pawnDirection = if (attackerColor == PieceColor.WHITE) 1 else -1 // Direction from pawn to target
    val pawnRow = targetRow + pawnDirection
    if (pawnRow in 0..7) {
      if (targetCol - 1 >= 0) {
        val p = board[pawnRow][targetCol - 1]
        if (p != null && p.color == attackerColor && p.type == PieceType.PAWN) return true
      }
      if (targetCol + 1 <= 7) {
        val p = board[pawnRow][targetCol + 1]
        if (p != null && p.color == attackerColor && p.type == PieceType.PAWN) return true
      }
    }

    // 2. Attacked by Knights
    for (offset in KNIGHT_OFFSETS) {
      val r = targetRow + offset[0]
      val c = targetCol + offset[1]
      if (r in 0..7 && c in 0..7) {
        val p = board[r][c]
        if (p != null && p.color == attackerColor && p.type == PieceType.KNIGHT) return true
      }
    }

    // 3. Attacked by Bishop / Queen (diagonals)
    for (dir in BISHOP_DIRECTIONS) {
      var r = targetRow + dir[0]
      var c = targetCol + dir[1]
      while (r in 0..7 && c in 0..7) {
        val p = board[r][c]
        if (p != null) {
          if (p.color == attackerColor && (p.type == PieceType.BISHOP || p.type == PieceType.QUEEN)) {
            return true
          }
          break
        }
        r += dir[0]
        c += dir[1]
      }
    }

    // 4. Attacked by Rook / Queen (orthogonals)
    for (dir in ROOK_DIRECTIONS) {
      var r = targetRow + dir[0]
      var c = targetCol + dir[1]
      while (r in 0..7 && c in 0..7) {
        val p = board[r][c]
        if (p != null) {
          if (p.color == attackerColor && (p.type == PieceType.ROOK || p.type == PieceType.QUEEN)) {
            return true
          }
          break
        }
        r += dir[0]
        c += dir[1]
      }
    }

    // 5. Attacked by King
    for (dir in KING_DIRECTIONS) {
      val r = targetRow + dir[0]
      val c = targetCol + dir[1]
      if (r in 0..7 && c in 0..7) {
        val p = board[r][c]
        if (p != null && p.color == attackerColor && p.type == PieceType.KING) return true
      }
    }

    return false
  }

  private fun generatePawnMoves(
    state: GameState,
    from: Square,
    color: PieceColor,
    moves: MutableList<Move>
  ) {
    val board = state.board
    val dir = if (color == PieceColor.WHITE) -1 else 1
    val startRow = if (color == PieceColor.WHITE) 6 else 1
    val promoRow = if (color == PieceColor.WHITE) 0 else 7

    val nextRow = from.row + dir
    if (nextRow in 0..7) {
      // 1 step forward
      if (board[nextRow][from.col] == null) {
        val to = Square(nextRow, from.col)
        if (nextRow == promoRow) {
          addPromotions(from, to, moves, false)
        } else {
          moves.add(Move(from, to, isCapture = false))

          // 2 steps forward
          val doubleRow = from.row + 2 * dir
          if (from.row == startRow && board[doubleRow][from.col] == null) {
            moves.add(Move(from, Square(doubleRow, from.col), isCapture = false))
          }
        }
      }

      // Diagonal captures
      for (dc in intArrayOf(-1, 1)) {
        val targetCol = from.col + dc
        if (targetCol in 0..7) {
          val targetPiece = board[nextRow][targetCol]
          val to = Square(nextRow, targetCol)
          if (targetPiece != null && targetPiece.color != color) {
            if (nextRow == promoRow) {
              addPromotions(from, to, moves, true)
            } else {
              moves.add(Move(from, to, isCapture = true))
            }
          } else if (targetPiece == null && state.enPassantTarget == to) {
            moves.add(Move(from, to, isCapture = true, isEnPassant = true))
          }
        }
      }
    }
  }

  private fun addPromotions(from: Square, to: Square, moves: MutableList<Move>, isCapture: Boolean) {
    moves.add(Move(from, to, promotion = PieceType.QUEEN, isCapture = isCapture))
    moves.add(Move(from, to, promotion = PieceType.KNIGHT, isCapture = isCapture))
    moves.add(Move(from, to, promotion = PieceType.ROOK, isCapture = isCapture))
    moves.add(Move(from, to, promotion = PieceType.BISHOP, isCapture = isCapture))
  }

  private fun generateKnightMoves(
    board: Array<Array<Piece?>>,
    from: Square,
    color: PieceColor,
    moves: MutableList<Move>
  ) {
    for (offset in KNIGHT_OFFSETS) {
      val r = from.row + offset[0]
      val c = from.col + offset[1]
      if (r in 0..7 && c in 0..7) {
        val target = board[r][c]
        if (target == null) {
          moves.add(Move(from, Square(r, c), isCapture = false))
        } else if (target.color != color) {
          moves.add(Move(from, Square(r, c), isCapture = true))
        }
      }
    }
  }

  private fun generateSlidingMoves(
    board: Array<Array<Piece?>>,
    from: Square,
    color: PieceColor,
    directions: Array<IntArray>,
    moves: MutableList<Move>
  ) {
    for (dir in directions) {
      var r = from.row + dir[0]
      var c = from.col + dir[1]
      while (r in 0..7 && c in 0..7) {
        val target = board[r][c]
        if (target == null) {
          moves.add(Move(from, Square(r, c), isCapture = false))
        } else {
          if (target.color != color) {
            moves.add(Move(from, Square(r, c), isCapture = true))
          }
          break
        }
        r += dir[0]
        c += dir[1]
      }
    }
  }

  private fun generateKingMoves(
    board: Array<Array<Piece?>>,
    from: Square,
    color: PieceColor,
    moves: MutableList<Move>
  ) {
    for (dir in KING_DIRECTIONS) {
      val r = from.row + dir[0]
      val c = from.col + dir[1]
      if (r in 0..7 && c in 0..7) {
        val target = board[r][c]
        if (target == null) {
          moves.add(Move(from, Square(r, c), isCapture = false))
        } else if (target.color != color) {
          moves.add(Move(from, Square(r, c), isCapture = true))
        }
      }
    }
  }

  private fun generateCastlingMoves(
    state: GameState,
    from: Square,
    color: PieceColor,
    moves: MutableList<Move>
  ) {
    val board = state.board
    val row = if (color == PieceColor.WHITE) 7 else 0
    val enemyColor = color.opposite()

    if (from.row != row || from.col != 4) return
    if (isSquareAttacked(board, row, 4, enemyColor)) return // King in check cannot castle

    // Kingside
    val canCastleKingside = if (color == PieceColor.WHITE) state.whiteCanCastleKingside else state.blackCanCastleKingside
    if (canCastleKingside &&
      board[row][5] == null && board[row][6] == null &&
      board[row][7]?.type == PieceType.ROOK && board[row][7]?.color == color &&
      !isSquareAttacked(board, row, 5, enemyColor) &&
      !isSquareAttacked(board, row, 6, enemyColor)
    ) {
      moves.add(Move(from, Square(row, 6), isCastling = true))
    }

    // Queenside
    val canCastleQueenside = if (color == PieceColor.WHITE) state.whiteCanCastleQueenside else state.blackCanCastleQueenside
    if (canCastleQueenside &&
      board[row][1] == null && board[row][2] == null && board[row][3] == null &&
      board[row][0]?.type == PieceType.ROOK && board[row][0]?.color == color &&
      !isSquareAttacked(board, row, 3, enemyColor) &&
      !isSquareAttacked(board, row, 2, enemyColor)
    ) {
      moves.add(Move(from, Square(row, 2), isCastling = true))
    }
  }

  fun applyMove(state: GameState, move: Move): GameState {
    val newBoard = state.copyBoard()
    val movingPiece = newBoard[move.from.row][move.from.col] ?: return state

    val capturedPiece = if (move.isEnPassant) {
      newBoard[move.from.row][move.to.col]
    } else {
      newBoard[move.to.row][move.to.col]
    }

    // Move piece on board
    val finalPiece = if (move.promotion != null) {
      Piece(move.promotion, movingPiece.color)
    } else {
      movingPiece
    }
    newBoard[move.to.row][move.to.col] = finalPiece
    newBoard[move.from.row][move.from.col] = null

    // En passant capture cleanup
    if (move.isEnPassant) {
      newBoard[move.from.row][move.to.col] = null
    }

    // Castling rook update
    if (move.isCastling) {
      val row = move.from.row
      if (move.to.col == 6) { // Kingside
        val rook = newBoard[row][7]
        newBoard[row][5] = rook
        newBoard[row][7] = null
      } else if (move.to.col == 2) { // Queenside
        val rook = newBoard[row][0]
        newBoard[row][3] = rook
        newBoard[row][0] = null
      }
    }

    // Update castling rights
    var wck = state.whiteCanCastleKingside
    var wcq = state.whiteCanCastleQueenside
    var bck = state.blackCanCastleKingside
    var bcq = state.blackCanCastleQueenside

    if (movingPiece.type == PieceType.KING) {
      if (movingPiece.color == PieceColor.WHITE) {
        wck = false
        wcq = false
      } else {
        bck = false
        bcq = false
      }
    } else if (movingPiece.type == PieceType.ROOK) {
      if (move.from == Square(7, 7)) wck = false
      if (move.from == Square(7, 0)) wcq = false
      if (move.from == Square(0, 7)) bck = false
      if (move.from == Square(0, 0)) bcq = false
    }

    // If opponent rook is captured in starting corner, lose that castling right
    if (move.to == Square(7, 7)) wck = false
    if (move.to == Square(7, 0)) wcq = false
    if (move.to == Square(0, 7)) bck = false
    if (move.to == Square(0, 0)) bcq = false

    // Update en passant target
    val newEnPassant = if (movingPiece.type == PieceType.PAWN && Math.abs(move.to.row - move.from.row) == 2) {
      Square((move.from.row + move.to.row) / 2, move.from.col)
    } else {
      null
    }

    // Update captured list
    val capturedByWhite = ArrayList(state.capturedByWhite)
    val capturedByBlack = ArrayList(state.capturedByBlack)
    if (capturedPiece != null) {
      if (movingPiece.color == PieceColor.WHITE) {
        capturedByWhite.add(capturedPiece)
      } else {
        capturedByBlack.add(capturedPiece)
      }
    }

    // Halfmove clock (50-move rule counter)
    val newHalfmove = if (movingPiece.type == PieceType.PAWN || capturedPiece != null) 0 else state.halfmoveClock + 1
    val newFullmove = if (state.turn == PieceColor.BLACK) state.fullmoveNumber + 1 else state.fullmoveNumber

    val nextTurn = state.turn.opposite()
    val nextKingInCheck = isKingInCheck(newBoard, nextTurn)

    // Check game over state
    val tempState = GameState(
      board = newBoard,
      turn = nextTurn,
      whiteCanCastleKingside = wck,
      whiteCanCastleQueenside = wcq,
      blackCanCastleKingside = bck,
      blackCanCastleQueenside = bcq,
      enPassantTarget = newEnPassant,
      halfmoveClock = newHalfmove,
      fullmoveNumber = newFullmove,
      capturedByWhite = capturedByWhite,
      capturedByBlack = capturedByBlack,
      isCheck = nextKingInCheck
    )

    val nextLegalMoves = generateLegalMoves(tempState, nextTurn)
    var isGameOver = false
    var outcome: GameOutcome? = null

    if (nextLegalMoves.isEmpty()) {
      isGameOver = true
      outcome = if (nextKingInCheck) {
        // Active turn is checkmated, so the player who just moved won!
        GameOutcome.Checkmate(winner = state.turn)
      } else {
        GameOutcome.Stalemate()
      }
    } else if (newHalfmove >= 100) {
      isGameOver = true
      outcome = GameOutcome.Draw("Draw by 50-move rule (no pawn move or capture for 50 moves).")
    } else if (isInsufficientMaterial(newBoard)) {
      isGameOver = true
      outcome = GameOutcome.Draw("Draw by insufficient material to force checkmate.")
    }

    // Generate SAN notation for the move record
    val san = generateSan(state, move, movingPiece, capturedPiece != null, nextKingInCheck, outcome is GameOutcome.Checkmate)

    val record = MoveRecord(
      move = move,
      piece = movingPiece,
      capturedPiece = capturedPiece,
      san = san,
      boardBefore = state.board,
      checkState = nextKingInCheck,
      checkmateState = outcome is GameOutcome.Checkmate
    )

    return tempState.copy(
      moveHistory = state.moveHistory + record,
      isGameOver = isGameOver,
      outcome = outcome
    )
  }

  fun isInsufficientMaterial(board: Array<Array<Piece?>>): Boolean {
    var whitePieces = 0
    var blackPieces = 0
    var whiteBishops = 0
    var blackBishops = 0
    var whiteKnights = 0
    var blackKnights = 0

    for (r in 0..7) {
      for (c in 0..7) {
        val p = board[r][c] ?: continue
        when (p.type) {
          PieceType.PAWN, PieceType.ROOK, PieceType.QUEEN -> return false
          PieceType.KING -> {}
          PieceType.BISHOP -> {
            if (p.color == PieceColor.WHITE) {
              whitePieces++
              whiteBishops++
            } else {
              blackPieces++
              blackBishops++
            }
          }
          PieceType.KNIGHT -> {
            if (p.color == PieceColor.WHITE) {
              whitePieces++
              whiteKnights++
            } else {
              blackPieces++
              blackKnights++
            }
          }
        }
      }
    }

    // K vs K
    if (whitePieces == 0 && blackPieces == 0) return true
    // K+B vs K or K+N vs K
    if ((whitePieces == 1 && blackPieces == 0) || (blackPieces == 1 && whitePieces == 0)) return true
    // K+N vs K+N
    if (whiteKnights == 1 && whitePieces == 1 && blackKnights == 1 && blackPieces == 1) return true

    return false
  }

  fun generateSan(
    state: GameState,
    move: Move,
    piece: Piece,
    isCapture: Boolean,
    isCheck: Boolean,
    isCheckmate: Boolean
  ): String {
    val suffix = when {
      isCheckmate -> "#"
      isCheck -> "+"
      else -> ""
    }

    if (move.isCastling) {
      return (if (move.to.col == 6) "O-O" else "O-O-O") + suffix
    }

    val promoStr = if (move.promotion != null) "=${move.promotion.notation}" else ""

    if (piece.type == PieceType.PAWN) {
      return if (isCapture) {
        "${move.from.algebraic[0]}x${move.to.algebraic}$promoStr$suffix"
      } else {
        "${move.to.algebraic}$promoStr$suffix"
      }
    }

    // Check for ambiguity among same piece types capable of moving to the target
    val disambiguation = getDisambiguation(state, move, piece)
    val captureMark = if (isCapture) "x" else ""

    return "${piece.type.notation}$disambiguation$captureMark${move.to.algebraic}$suffix"
  }

  private fun getDisambiguation(state: GameState, move: Move, piece: Piece): String {
    val legalMoves = generateLegalMoves(state, piece.color)
    val candidates = legalMoves.filter {
      it.to == move.to && it.from != move.from && state.pieceAt(it.from)?.type == piece.type
    }

    if (candidates.isEmpty()) return ""

    val sameFile = candidates.any { it.from.col == move.from.col }
    val sameRank = candidates.any { it.from.row == move.from.row }

    return when {
      !sameFile -> move.from.algebraic[0].toString()
      !sameRank -> move.from.algebraic[1].toString()
      else -> move.from.algebraic
    }
  }

  fun toFen(state: GameState): String {
    val sb = StringBuilder()
    // 1. Piece placement
    for (r in 0..7) {
      var emptyCount = 0
      for (c in 0..7) {
        val piece = state.board[r][c]
        if (piece == null) {
          emptyCount++
        } else {
          if (emptyCount > 0) {
            sb.append(emptyCount)
            emptyCount = 0
          }
          val char = when (piece.type) {
            PieceType.PAWN -> 'p'
            PieceType.KNIGHT -> 'n'
            PieceType.BISHOP -> 'b'
            PieceType.ROOK -> 'r'
            PieceType.QUEEN -> 'q'
            PieceType.KING -> 'k'
          }
          sb.append(if (piece.color == PieceColor.WHITE) char.uppercaseChar() else char)
        }
      }
      if (emptyCount > 0) sb.append(emptyCount)
      if (r < 7) sb.append('/')
    }

    // 2. Active color
    sb.append(if (state.turn == PieceColor.WHITE) " w " else " b ")

    // 3. Castling
    val castling = StringBuilder()
    if (state.whiteCanCastleKingside) castling.append('K')
    if (state.whiteCanCastleQueenside) castling.append('Q')
    if (state.blackCanCastleKingside) castling.append('k')
    if (state.blackCanCastleQueenside) castling.append('q')
    sb.append(if (castling.isEmpty()) "-" else castling.toString())
    sb.append(' ')

    // 4. En passant
    sb.append(state.enPassantTarget?.algebraic ?: "-")
    sb.append(' ')

    // 5. Halfmove clock & 6. Fullmove number
    sb.append(state.halfmoveClock).append(' ').append(state.fullmoveNumber)

    return sb.toString()
  }

  fun fromFen(fen: String): GameState? {
    try {
      val parts = fen.trim().split("\\s+".toRegex())
      if (parts.isEmpty()) return null
      val rows = parts[0].split('/')
      if (rows.size != 8) return null

      val board = Array(8) { Array<Piece?>(8) { null } }
      for (r in 0..7) {
        val rowStr = rows[r]
        var c = 0
        for (ch in rowStr) {
          if (ch.isDigit()) {
            c += (ch - '0')
          } else {
            val color = if (ch.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK
            val type = when (ch.lowercaseChar()) {
              'p' -> PieceType.PAWN
              'n' -> PieceType.KNIGHT
              'b' -> PieceType.BISHOP
              'r' -> PieceType.ROOK
              'q' -> PieceType.QUEEN
              'k' -> PieceType.KING
              else -> return null
            }
            if (c in 0..7) {
              board[r][c] = Piece(type, color)
              c++
            }
          }
        }
      }

      val turn = if (parts.size > 1 && parts[1] == "b") PieceColor.BLACK else PieceColor.WHITE
      val castling = if (parts.size > 2) parts[2] else "KQkq"
      val epSquare = if (parts.size > 3 && parts[3] != "-") {
        try { Square.fromAlgebraic(parts[3]) } catch (_: Exception) { null }
      } else null

      val halfmove = if (parts.size > 4) parts[4].toIntOrNull() ?: 0 else 0
      val fullmove = if (parts.size > 5) parts[5].toIntOrNull() ?: 1 else 1

      val isCheck = isKingInCheck(board, turn)

      return GameState(
        board = board,
        turn = turn,
        whiteCanCastleKingside = castling.contains('K'),
        whiteCanCastleQueenside = castling.contains('Q'),
        blackCanCastleKingside = castling.contains('k'),
        blackCanCastleQueenside = castling.contains('q'),
        enPassantTarget = epSquare,
        halfmoveClock = halfmove,
        fullmoveNumber = fullmove,
        isCheck = isCheck
      )
    } catch (_: Exception) {
      return null
    }
  }

  fun applyUciMove(state: GameState, uci: String): GameState? {
    if (uci.length < 4) return null
    try {
      val from = Square.fromAlgebraic(uci.substring(0, 2))
      val to = Square.fromAlgebraic(uci.substring(2, 4))
      val promoType = if (uci.length > 4) {
        when (uci[4].lowercaseChar()) {
          'q' -> PieceType.QUEEN
          'r' -> PieceType.ROOK
          'b' -> PieceType.BISHOP
          'n' -> PieceType.KNIGHT
          else -> null
        }
      } else null

      val legalMoves = getLegalMovesForSquare(state, from)
      val matchedMove = legalMoves.firstOrNull {
        it.to == to && (promoType == null || it.promotion == promoType)
      } ?: Move(from = from, to = to, promotion = promoType)

      return applyMove(state, matchedMove)
    } catch (_: Exception) {
      return null
    }
  }

  fun generatePgn(
    state: GameState,
    whitePlayer: String = "White Player",
    blackPlayer: String = "Black Player",
    event: String = "Grandmaster Match"
  ): String {
    val sb = StringBuilder()
    sb.appendLine("[Event \"$event\"]")
    sb.appendLine("[Site \"Android Chess Master\"]")
    sb.appendLine("[White \"$whitePlayer\"]")
    sb.appendLine("[Black \"$blackPlayer\"]")

    val resultStr = when (val out = state.outcome) {
      is GameOutcome.Checkmate -> if (out.winner == PieceColor.WHITE) "1-0" else "0-1"
      is GameOutcome.Timeout -> if (out.winner == PieceColor.WHITE) "1-0" else "0-1"
      is GameOutcome.Resignation -> if (out.winner == PieceColor.WHITE) "1-0" else "0-1"
      is GameOutcome.Stalemate, is GameOutcome.Draw -> "1/2-1/2"
      is GameOutcome.PuzzleSolved -> "1-0"
      null -> "*"
    }
    sb.appendLine("[Result \"$resultStr\"]")
    sb.appendLine()

    val moves = state.moveHistory
    moves.chunked(2).forEachIndexed { i, turnMoves ->
      val moveNum = i + 1
      sb.append("$moveNum. ${turnMoves[0].san} ")
      if (turnMoves.size > 1) {
        sb.append("${turnMoves[1].san} ")
      }
    }
    sb.append(resultStr)
    return sb.toString()
  }
}

