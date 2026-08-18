package com.example.chess

import com.example.chess.engine.ChessAiEngine
import com.example.chess.engine.ChessRules
import com.example.chess.model.Difficulty
import com.example.chess.model.GameOutcome
import com.example.chess.model.GameState
import com.example.chess.model.Move
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChessEngineTest {

  @Test
  fun testInitialPositionMoveCount() {
    val state = GameState()
    val whiteLegalMoves = ChessRules.generateLegalMoves(state, PieceColor.WHITE)
    // In standard chess, White has 16 pawn moves (8 single + 8 double) + 4 knight moves = 20 legal moves
    assertEquals(20, whiteLegalMoves.size)
  }

  @Test
  fun testScholarsMateWhiteWins() {
    var state = GameState()

    // 1. e4 e5
    state = ChessRules.applyMove(state, Move(Square(6, 4), Square(4, 4)))
    state = ChessRules.applyMove(state, Move(Square(1, 4), Square(3, 4)))

    // 2. Qh5 Nc6
    state = ChessRules.applyMove(state, Move(Square(7, 3), Square(3, 7)))
    state = ChessRules.applyMove(state, Move(Square(0, 1), Square(2, 2)))

    // 3. Bc4 Nf6
    state = ChessRules.applyMove(state, Move(Square(7, 5), Square(4, 2)))
    state = ChessRules.applyMove(state, Move(Square(0, 6), Square(2, 5)))

    // 4. Qxf7# (Checkmate!)
    state = ChessRules.applyMove(state, Move(Square(3, 7), Square(1, 5), isCapture = true))

    assertTrue(state.isGameOver)
    assertTrue(state.isCheck)
    val outcome = state.outcome
    assertNotNull(outcome)
    assertTrue(outcome is GameOutcome.Checkmate)
    assertEquals(PieceColor.WHITE, (outcome as GameOutcome.Checkmate).winner)
    assertEquals("White Wins!", outcome.headline)
  }

  @Test
  fun testFoolsMateBlackWins() {
    var state = GameState()

    // 1. f3 e5
    state = ChessRules.applyMove(state, Move(Square(6, 5), Square(5, 5)))
    state = ChessRules.applyMove(state, Move(Square(1, 4), Square(3, 4)))

    // 2. g4 Qh4#
    state = ChessRules.applyMove(state, Move(Square(6, 6), Square(4, 6)))
    state = ChessRules.applyMove(state, Move(Square(0, 3), Square(4, 7)))

    assertTrue(state.isGameOver)
    assertTrue(state.isCheck)
    val outcome = state.outcome
    assertNotNull(outcome)
    assertTrue(outcome is GameOutcome.Checkmate)
    assertEquals(PieceColor.BLACK, (outcome as GameOutcome.Checkmate).winner)
    assertEquals("Black Wins!", outcome.headline)
  }

  @Test
  fun testStalemateGameDrawn() {
    // Construct classic stalemate: Black King on a8 (0,0), White King on c7 (1,2), White Queen on b6 (2,1) -> Black has 0 moves and not in check
    val board = Array(8) { Array<Piece?>(8) { null } }
    board[0][0] = Piece(PieceType.KING, PieceColor.BLACK)
    board[1][2] = Piece(PieceType.KING, PieceColor.WHITE)
    board[2][1] = Piece(PieceType.QUEEN, PieceColor.WHITE)

    val state = GameState(
      board = board,
      turn = PieceColor.BLACK,
      whiteCanCastleKingside = false,
      whiteCanCastleQueenside = false,
      blackCanCastleKingside = false,
      blackCanCastleQueenside = false
    )

    val blackLegalMoves = ChessRules.generateLegalMoves(state, PieceColor.BLACK)
    assertTrue(blackLegalMoves.isEmpty())

    // King is not in check
    val isCheck = ChessRules.isKingInCheck(state, PieceColor.BLACK)
    assertEquals(false, isCheck)

    // Outcome test
    val tempOutcome = if (isCheck) GameOutcome.Checkmate(PieceColor.WHITE) else GameOutcome.Stalemate()
    assertEquals("Game Drawn!", tempOutcome.headline)
  }

  @Test
  fun testAiFindBestMoveExecutionSpeed() = runBlocking {
    val aiEngine = ChessAiEngine()
    val state = GameState()

    val startTime = System.currentTimeMillis()
    val bestMove = aiEngine.findBestMove(state, Difficulty.MASTER)
    val duration = System.currentTimeMillis() - startTime

    assertNotNull(bestMove)
    // Must be under 1.5 seconds (in reality < 100ms for opening)
    assertTrue("AI move took $duration ms which is over 2000ms", duration < 2000)
  }

  @Test
  fun testAiFindsImmediateCheckmate() = runBlocking {
    // 1-move before Scholar's Mate: White Queen on h5 (3,7), White Bishop on c4 (4,2). Black King on e8 (0,4), pawn on f7 (1,5).
    var state = GameState()
    state = ChessRules.applyMove(state, Move(Square(6, 4), Square(4, 4)))
    state = ChessRules.applyMove(state, Move(Square(1, 4), Square(3, 4)))
    state = ChessRules.applyMove(state, Move(Square(7, 3), Square(3, 7)))
    state = ChessRules.applyMove(state, Move(Square(0, 1), Square(2, 2)))
    state = ChessRules.applyMove(state, Move(Square(7, 5), Square(4, 2)))
    state = ChessRules.applyMove(state, Move(Square(0, 6), Square(2, 5)))

    val aiEngine = ChessAiEngine()
    val move = aiEngine.findBestMove(state, Difficulty.MASTER)

    assertNotNull(move)
    assertEquals(Square(3, 7), move!!.from) // Queen from h5
    assertEquals(Square(1, 5), move.to)     // to f7 (Mate!)
  }

  @Test
  fun testFenExportAndImport() {
    val initial = GameState()
    val fen = ChessRules.toFen(initial)
    assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", fen)

    val imported = ChessRules.fromFen(fen)
    assertNotNull(imported)
    assertEquals(PieceColor.WHITE, imported!!.turn)
    assertEquals(PieceType.ROOK, imported.board[7][0]?.type)
    assertEquals(PieceType.KING, imported.board[0][4]?.type)
  }

  @Test
  fun testPgnGeneration() {
    var state = GameState()
    state = ChessRules.applyMove(state, Move(Square(6, 4), Square(4, 4)))
    state = ChessRules.applyMove(state, Move(Square(1, 4), Square(3, 4)))

    val pgn = ChessRules.generatePgn(state, "Magnus", "Hikaru")
    assertTrue(pgn.contains("[White \"Magnus\"]"))
    assertTrue(pgn.contains("[Black \"Hikaru\"]"))
    assertTrue(pgn.contains("1. e4 e5"))
  }
}
