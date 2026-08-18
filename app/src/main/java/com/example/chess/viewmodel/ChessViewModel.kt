package com.example.chess.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.audio.ChessSoundManager
import com.example.chess.data.ChessDatabase
import com.example.chess.data.MatchHistoryRepository
import com.example.chess.data.MatchRecordEntity
import com.example.chess.engine.ChessAiEngine
import com.example.chess.engine.ChessAnalysisEngine
import com.example.chess.engine.ChessOpening
import com.example.chess.engine.ChessRules
import com.example.chess.engine.GameAnalysisReport
import com.example.chess.engine.OpeningBook
import com.example.chess.model.BoardTheme
import com.example.chess.model.CoordinatesDisplay
import com.example.chess.model.Difficulty
import com.example.chess.model.GameMode
import com.example.chess.model.GameOutcome
import com.example.chess.model.GameState
import com.example.chess.model.Move
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceStyle
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import com.example.chess.model.TacticalPuzzle
import com.example.chess.model.TacticalPuzzleDatabase
import com.example.chess.model.TimeControl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class UndoMode(val title: String, val description: String) {
  FULL_TURN("Full Turn (2 Plies)", "Reverts both the AI's response and your move in vs AI mode"),
  SINGLE_STEP("Single Move (1 Ply)", "Reverts one move at a time across all matches")
}

class ChessViewModel(
  application: Application,
  private val aiEngine: ChessAiEngine
) : AndroidViewModel(application) {

  // Primary Android ViewModel constructor for AndroidViewModelFactory
  constructor(application: Application) : this(application, ChessAiEngine())

  // Secondary testing / fallback constructor
  constructor() : this(Application(), ChessAiEngine())

  private val matchRepository: MatchHistoryRepository? = try {
    MatchHistoryRepository(ChessDatabase.getDatabase(application).matchHistoryDao())
  } catch (_: Exception) {
    null
  }

  private val _gameState = MutableStateFlow(GameState())
  val gameState: StateFlow<GameState> = _gameState.asStateFlow()

  // Selected Square & Legal Moves
  private val _selectedSquare = MutableStateFlow<Square?>(null)
  val selectedSquare: StateFlow<Square?> = _selectedSquare.asStateFlow()

  private val _legalMovesForSelected = MutableStateFlow<List<Move>>(emptyList())
  val legalMovesForSelected: StateFlow<List<Move>> = _legalMovesForSelected.asStateFlow()

  private val _isAiThinking = MutableStateFlow(false)
  val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

  private val _pendingPromotionMove = MutableStateFlow<Pair<Square, Square>?>(null)
  val pendingPromotionMove: StateFlow<Pair<Square, Square>?> = _pendingPromotionMove.asStateFlow()

  private val _showGameOverDialog = MutableStateFlow(false)
  val showGameOverDialog: StateFlow<Boolean> = _showGameOverDialog.asStateFlow()

  // Game Mode & Rules
  private val _gameMode = MutableStateFlow(GameMode.AI_VS_HUMAN)
  val gameMode: StateFlow<GameMode> = _gameMode.asStateFlow()

  private val _humanColor = MutableStateFlow(PieceColor.WHITE)
  val humanColor: StateFlow<PieceColor> = _humanColor.asStateFlow()

  private val _difficulty = MutableStateFlow(Difficulty.MASTER)
  val difficulty: StateFlow<Difficulty> = _difficulty.asStateFlow()

  // Undo Mode Setting
  private val _undoMode = MutableStateFlow(UndoMode.FULL_TURN)
  val undoMode: StateFlow<UndoMode> = _undoMode.asStateFlow()

  // Match History Flow from Room Database
  private val _matchHistory = MutableStateFlow<List<MatchRecordEntity>>(emptyList())
  val matchHistory: StateFlow<List<MatchRecordEntity>> = _matchHistory.asStateFlow()

  private val _matchCount = MutableStateFlow(0)
  val matchCount: StateFlow<Int> = _matchCount.asStateFlow()

  // Visual Customization & Preferences
  private val _boardTheme = MutableStateFlow(BoardTheme.CLASSIC_TOURNAMENT)
  val boardTheme: StateFlow<BoardTheme> = _boardTheme.asStateFlow()

  private val _pieceStyle = MutableStateFlow(PieceStyle.STAUNTON_CLASSIC)
  val pieceStyle: StateFlow<PieceStyle> = _pieceStyle.asStateFlow()

  private val _coordinatesDisplay = MutableStateFlow(CoordinatesDisplay.INSIDE)
  val coordinatesDisplay: StateFlow<CoordinatesDisplay> = _coordinatesDisplay.asStateFlow()

  private val _autoQueenEnabled = MutableStateFlow(false)
  val autoQueenEnabled: StateFlow<Boolean> = _autoQueenEnabled.asStateFlow()

  private val _blindfoldMode = MutableStateFlow(false)
  val blindfoldMode: StateFlow<Boolean> = _blindfoldMode.asStateFlow()

  private val _showDangerSquare = MutableStateFlow(true)
  val showDangerSquare: StateFlow<Boolean> = _showDangerSquare.asStateFlow()

  private val _showLegalMoveDots = MutableStateFlow(true)
  val showLegalMoveDots: StateFlow<Boolean> = _showLegalMoveDots.asStateFlow()

  private val _showLastMoveHighlight = MutableStateFlow(true)
  val showLastMoveHighlight: StateFlow<Boolean> = _showLastMoveHighlight.asStateFlow()

  // Time & Clocks
  private val _timeControl = MutableStateFlow(TimeControl.UNLIMITED)
  val timeControl: StateFlow<TimeControl> = _timeControl.asStateFlow()

  private val _whiteTimeSeconds = MutableStateFlow(0)
  val whiteTimeSeconds: StateFlow<Int> = _whiteTimeSeconds.asStateFlow()

  private val _blackTimeSeconds = MutableStateFlow(0)
  val blackTimeSeconds: StateFlow<Int> = _blackTimeSeconds.asStateFlow()

  private val _isBoardFlipped = MutableStateFlow(false)
  val isBoardFlipped: StateFlow<Boolean> = _isBoardFlipped.asStateFlow()

  private val _lastMove = MutableStateFlow<Move?>(null)
  val lastMove: StateFlow<Move?> = _lastMove.asStateFlow()

  // Tactical Hint & Live Evaluation
  private val _hintMove = MutableStateFlow<Move?>(null)
  val hintMove: StateFlow<Move?> = _hintMove.asStateFlow()

  private val _isCalculatingHint = MutableStateFlow(false)
  val isCalculatingHint: StateFlow<Boolean> = _isCalculatingHint.asStateFlow()

  private val _evalCentipawns = MutableStateFlow(0)
  val evalCentipawns: StateFlow<Int> = _evalCentipawns.asStateFlow()

  private val _showEvalBar = MutableStateFlow(true)
  val showEvalBar: StateFlow<Boolean> = _showEvalBar.asStateFlow()

  private val _soundEnabled = MutableStateFlow(true)
  val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

  // Opening Recognition
  private val _currentOpening = MutableStateFlow<ChessOpening?>(null)
  val currentOpening: StateFlow<ChessOpening?> = _currentOpening.asStateFlow()

  // Interactive Replay
  private val _viewingPlyIndex = MutableStateFlow<Int?>(null)
  val viewingPlyIndex: StateFlow<Int?> = _viewingPlyIndex.asStateFlow()

  // Game Review / Analysis Report
  private val _analysisReport = MutableStateFlow<GameAnalysisReport?>(null)
  val analysisReport: StateFlow<GameAnalysisReport?> = _analysisReport.asStateFlow()

  private val _isAnalyzing = MutableStateFlow(false)
  val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

  // Tactical Puzzles
  private val _puzzleIndex = MutableStateFlow(0)
  val puzzleIndex: StateFlow<Int> = _puzzleIndex.asStateFlow()

  private val _currentPuzzle = MutableStateFlow<TacticalPuzzle?>(null)
  val currentPuzzle: StateFlow<TacticalPuzzle?> = _currentPuzzle.asStateFlow()

  private val _puzzleMoveIndex = MutableStateFlow(0)
  val puzzleMoveIndex: StateFlow<Int> = _puzzleMoveIndex.asStateFlow()

  private val _puzzleFeedback = MutableStateFlow<String?>(null)
  val puzzleFeedback: StateFlow<String?> = _puzzleFeedback.asStateFlow()

  // Draw Offer Feedback Toast / Message
  private val _drawOfferFeedback = MutableStateFlow<String?>(null)
  val drawOfferFeedback: StateFlow<String?> = _drawOfferFeedback.asStateFlow()

  // Board Editor Palette State
  private val _editorSelectedPiece = MutableStateFlow<Piece?>(null)
  val editorSelectedPiece: StateFlow<Piece?> = _editorSelectedPiece.asStateFlow()

  private var initialGameState: GameState = GameState()
  private var aiJob: Job? = null
  private var clockJob: Job? = null
  private var matchStartTime: Long = System.currentTimeMillis()
  private var isCurrentMatchSaved: Boolean = false

  init {
    initClockTimes()
    startClockTicker()
    updateLiveEvaluation()
    checkAndTriggerAiMove()
    observeMatchHistory()
  }

  private fun observeMatchHistory() {
    viewModelScope.launch {
      matchRepository?.allMatches?.collect { list ->
        _matchHistory.value = list
        _matchCount.value = list.size
      }
    }
  }

  private fun initClockTimes() {
    val sec = _timeControl.value.initialSeconds
    _whiteTimeSeconds.value = sec
    _blackTimeSeconds.value = sec
  }

  private fun startClockTicker() {
    clockJob?.cancel()
    if (_timeControl.value == TimeControl.UNLIMITED) return

    clockJob = viewModelScope.launch {
      while (isActive) {
        delay(1000)
        val state = _gameState.value
        if (!state.isGameOver && state.moveHistory.isNotEmpty() && _gameMode.value != GameMode.TACTICAL_PUZZLES && _gameMode.value != GameMode.BOARD_EDITOR) {
          if (state.turn == PieceColor.WHITE) {
            if (_whiteTimeSeconds.value > 0) {
              _whiteTimeSeconds.value -= 1
              if (_whiteTimeSeconds.value == 0) {
                handleTimeout(PieceColor.WHITE)
              }
            }
          } else {
            if (_blackTimeSeconds.value > 0) {
              _blackTimeSeconds.value -= 1
              if (_blackTimeSeconds.value == 0) {
                handleTimeout(PieceColor.BLACK)
              }
            }
          }
        }
      }
    }
  }

  private fun handleTimeout(loser: PieceColor) {
    val state = _gameState.value
    val winner = loser.opposite()
    val finalState = state.copy(
      isGameOver = true,
      outcome = GameOutcome.Timeout(winner)
    )
    _gameState.value = finalState
    _showGameOverDialog.value = true
    ChessSoundManager.playGameOverSound()
    saveMatchRecordIfNeeded(finalState)
  }

  fun onSquareClicked(square: Square) {
    if (_gameMode.value == GameMode.BOARD_EDITOR) {
      handleBoardEditorSquareClick(square)
      return
    }

    // If user is currently reviewing history, return to live board on click
    if (_viewingPlyIndex.value != null) {
      _viewingPlyIndex.value = null
    }

    val state = _gameState.value
    if (state.isGameOver) return

    // In AI vs Human mode, block human clicks if it's AI's turn or AI is currently computing
    if (_gameMode.value == GameMode.AI_VS_HUMAN && (state.turn != _humanColor.value || _isAiThinking.value)) {
      return
    }

    val currentSelected = _selectedSquare.value

    if (currentSelected == null) {
      val piece = state.pieceAt(square)
      if (piece != null && piece.color == state.turn) {
        _selectedSquare.value = square
        _legalMovesForSelected.value = ChessRules.getLegalMovesForSquare(state, square)
      }
    } else {
      if (currentSelected == square) {
        clearSelection()
      } else {
        val targetMove = _legalMovesForSelected.value.firstOrNull { it.to == square }
        if (targetMove != null) {
          val movingPiece = state.pieceAt(currentSelected)
          val isPawnPromotion = movingPiece?.type == PieceType.PAWN &&
            ((movingPiece.color == PieceColor.WHITE && square.row == 0) ||
              (movingPiece.color == PieceColor.BLACK && square.row == 7))

          if (isPawnPromotion) {
            if (_autoQueenEnabled.value) {
              val promoMove = targetMove.copy(promotion = PieceType.QUEEN)
              executeMove(promoMove)
            } else {
              _pendingPromotionMove.value = Pair(currentSelected, square)
            }
          } else {
            executeMove(targetMove)
          }
        } else {
          val piece = state.pieceAt(square)
          if (piece != null && piece.color == state.turn) {
            _selectedSquare.value = square
            _legalMovesForSelected.value = ChessRules.getLegalMovesForSquare(state, square)
          } else {
            clearSelection()
          }
        }
      }
    }
  }

  fun onPromotionSelected(promoType: PieceType) {
    val pending = _pendingPromotionMove.value ?: return
    _pendingPromotionMove.value = null
    val targetMove = _legalMovesForSelected.value.firstOrNull { it.from == pending.first && it.to == pending.second }
    val moveWithPromo = (targetMove ?: Move(pending.first, pending.second)).copy(promotion = promoType)
    executeMove(moveWithPromo)
  }

  fun dismissPromotionDialog() {
    _pendingPromotionMove.value = null
    clearSelection()
  }

  private fun executeMove(move: Move) {
    val oldState = _gameState.value
    val newState = ChessRules.applyMove(oldState, move)

    // Add clock increment if any
    val inc = _timeControl.value.incrementSeconds
    if (inc > 0 && _timeControl.value != TimeControl.UNLIMITED) {
      if (oldState.turn == PieceColor.WHITE) {
        _whiteTimeSeconds.value += inc
      } else {
        _blackTimeSeconds.value += inc
      }
    }

    _gameState.value = newState
    _lastMove.value = move
    _hintMove.value = null
    _viewingPlyIndex.value = null
    clearSelection()

    // Sound Playback
    when {
      newState.isGameOver -> {
        if (newState.outcome is GameOutcome.Checkmate || newState.outcome is GameOutcome.PuzzleSolved) {
          ChessSoundManager.playVictorySound()
        } else {
          ChessSoundManager.playGameOverSound()
        }
      }
      newState.isCheck -> ChessSoundManager.playCheckSound()
      move.isCastling -> ChessSoundManager.playCastleSound()
      move.isCapture -> ChessSoundManager.playCaptureSound()
      else -> ChessSoundManager.playMoveSound()
    }

    // Detect live opening
    val uciList = newState.moveHistory.map { it.move.toUci() }
    _currentOpening.value = OpeningBook.detectOpening(uciList)

    updateLiveEvaluation()

    // Handle Puzzle Move Evaluation
    if (_gameMode.value == GameMode.TACTICAL_PUZZLES) {
      handlePuzzleMove(move)
      return
    }

    if (newState.isGameOver) {
      _showGameOverDialog.value = true
      saveMatchRecordIfNeeded(newState)
    } else {
      checkAndTriggerAiMove()
    }
  }

  private fun handlePuzzleMove(move: Move) {
    val puzzle = _currentPuzzle.value ?: return
    val expectedUci = puzzle.targetMovesUci.getOrNull(_puzzleMoveIndex.value)

    if (move.toUci() == expectedUci) {
      val nextIndex = _puzzleMoveIndex.value + 1
      _puzzleMoveIndex.value = nextIndex

      if (nextIndex >= puzzle.targetMovesUci.size) {
        // Puzzle Completed!
        _puzzleFeedback.value = "Solved! Excellent tactical calculation."
        val solvedState = _gameState.value.copy(
          isGameOver = true,
          outcome = GameOutcome.PuzzleSolved(ratingGain = 15)
        )
        _gameState.value = solvedState
        _showGameOverDialog.value = true
        ChessSoundManager.playVictorySound()
        saveMatchRecordIfNeeded(solvedState)
      } else {
        // Play opponent's response move in puzzle
        val opponentUci = puzzle.targetMovesUci.getOrNull(nextIndex)
        if (opponentUci != null) {
          _puzzleMoveIndex.value = nextIndex + 1
          viewModelScope.launch {
            delay(500)
            val oppFrom = Square.fromAlgebraic(opponentUci.substring(0, 2))
            val oppTo = Square.fromAlgebraic(opponentUci.substring(2, 4))
            val oppMove = ChessRules.getLegalMovesForSquare(_gameState.value, oppFrom)
              .firstOrNull { it.to == oppTo } ?: Move(oppFrom, oppTo)
            val stateAfterOpponent = ChessRules.applyMove(_gameState.value, oppMove)
            _gameState.value = stateAfterOpponent
            _lastMove.value = oppMove
            if (oppMove.isCapture) ChessSoundManager.playCaptureSound() else ChessSoundManager.playMoveSound()
            updateLiveEvaluation()
          }
        }
      }
    } else {
      _puzzleFeedback.value = "Incorrect move. Use Undo to retry or check your calculation."
      ChessSoundManager.playGameOverSound()
    }
  }

  private fun updateLiveEvaluation() {
    val state = _gameState.value
    val eval = aiEngine.evaluateState(state)
    _evalCentipawns.value = eval
  }

  fun requestHint() {
    val state = _gameState.value
    if (state.isGameOver || _isAiThinking.value || _isCalculatingHint.value) return

    viewModelScope.launch {
      _isCalculatingHint.value = true
      val hint = aiEngine.getHintMove(state)
      _hintMove.value = hint
      _isCalculatingHint.value = false
    }
  }

  private fun checkAndTriggerAiMove() {
    val state = _gameState.value
    if (state.isGameOver || _gameMode.value == GameMode.TACTICAL_PUZZLES || _gameMode.value == GameMode.BOARD_EDITOR) return

    if (_gameMode.value == GameMode.AI_VS_HUMAN && state.turn != _humanColor.value) {
      aiJob?.cancel()
      aiJob = viewModelScope.launch {
        _isAiThinking.value = true
        val startTime = System.currentTimeMillis()

        val bestMove = aiEngine.findBestMove(state, _difficulty.value)

        val elapsed = System.currentTimeMillis() - startTime
        if (elapsed < 350) {
          delay(350 - elapsed)
        }

        _isAiThinking.value = false
        if (bestMove != null) {
          executeMove(bestMove)
        }
      }
    }
  }

  // Enhanced Undo in ALL matches
  fun undoMove() {
    aiJob?.cancel()
    _isAiThinking.value = false
    _showGameOverDialog.value = false
    _hintMove.value = null
    _viewingPlyIndex.value = null
    clearSelection()

    val state = _gameState.value
    if (state.moveHistory.isEmpty()) {
      if (_gameMode.value == GameMode.TACTICAL_PUZZLES) {
        resetPuzzle()
      }
      return
    }

    when (_gameMode.value) {
      GameMode.AI_VS_HUMAN -> {
        if (_undoMode.value == UndoMode.SINGLE_STEP) {
          val targetIndex = (state.moveHistory.size - 1).coerceAtLeast(0)
          replayMovesTo(targetIndex)
        } else {
          // Full turn undo: If game is over or it's human turn, revert 2 plies
          if (state.isGameOver || state.turn == _humanColor.value) {
            if (state.moveHistory.size >= 2) {
              val targetIndex = state.moveHistory.size - 2
              replayMovesTo(targetIndex)
            } else {
              newGame()
            }
          } else {
            // It's currently AI's turn (user made last move)
            val targetIndex = state.moveHistory.size - 1
            replayMovesTo(targetIndex)
          }
        }
      }
      GameMode.TACTICAL_PUZZLES -> {
        // Undo move in puzzle mode: reset to puzzle start so user can try again
        _puzzleFeedback.value = "Move undone. Find the winning continuation!"
        _puzzleMoveIndex.value = 0
        val pz = _currentPuzzle.value
        if (pz != null) {
          val loadedState = ChessRules.fromFen(pz.fen) ?: GameState()
          _gameState.value = loadedState
          _lastMove.value = null
          _showGameOverDialog.value = false
          updateLiveEvaluation()
        }
      }
      GameMode.TWO_PLAYER, GameMode.BOARD_EDITOR -> {
        val targetIndex = state.moveHistory.size - 1
        replayMovesTo(targetIndex)
      }
    }
  }

  // Dedicated Undo & Continue action for GameOver Dialog
  fun undoLastMoveAndResume() {
    _showGameOverDialog.value = false
    isCurrentMatchSaved = false
    undoMove()
  }

  fun setUndoMode(mode: UndoMode) {
    _undoMode.value = mode
  }

  private fun replayMovesTo(count: Int) {
    val allMoves = _gameState.value.moveHistory.map { it.move }
    var state = initialGameState.copy(
      moveHistory = emptyList(),
      capturedByWhite = emptyList(),
      capturedByBlack = emptyList(),
      isCheck = false,
      isGameOver = false,
      outcome = null
    )
    for (i in 0 until count) {
      if (i < allMoves.size) {
        state = ChessRules.applyMove(state, allMoves[i])
      }
    }
    _gameState.value = state
    _lastMove.value = if (count > 0 && count - 1 < allMoves.size) allMoves[count - 1] else null
    _showGameOverDialog.value = state.isGameOver
    val uciList = state.moveHistory.map { it.move.toUci() }
    _currentOpening.value = OpeningBook.detectOpening(uciList)
    updateLiveEvaluation()

    // If it's now AI's turn after undo, trigger AI move
    if (!state.isGameOver && _gameMode.value == GameMode.AI_VS_HUMAN && state.turn != _humanColor.value) {
      checkAndTriggerAiMove()
    }
  }

  // Interactive Replay Controls
  fun jumpToStart() {
    if (_gameState.value.moveHistory.isEmpty()) return
    _viewingPlyIndex.value = 0
  }

  fun stepBack() {
    val current = _viewingPlyIndex.value ?: _gameState.value.moveHistory.size
    if (current > 0) {
      _viewingPlyIndex.value = current - 1
    }
  }

  fun stepForward() {
    val current = _viewingPlyIndex.value ?: return
    val maxPlies = _gameState.value.moveHistory.size
    if (current < maxPlies - 1) {
      _viewingPlyIndex.value = current + 1
    } else {
      _viewingPlyIndex.value = null // Back to live board
    }
  }

  fun jumpToEnd() {
    _viewingPlyIndex.value = null
  }

  fun getDisplayState(): GameState {
    val plyIndex = _viewingPlyIndex.value ?: return _gameState.value
    val allMoves = _gameState.value.moveHistory.map { it.move }
    var state = initialGameState.copy(
      moveHistory = emptyList(),
      capturedByWhite = emptyList(),
      capturedByBlack = emptyList(),
      isCheck = false,
      isGameOver = false,
      outcome = null
    )
    for (i in 0 until plyIndex) {
      if (i < allMoves.size) {
        state = ChessRules.applyMove(state, allMoves[i])
      }
    }
    return state
  }

  fun newGame() {
    aiJob?.cancel()
    _isAiThinking.value = false
    _showGameOverDialog.value = false
    _pendingPromotionMove.value = null
    _hintMove.value = null
    _viewingPlyIndex.value = null
    _analysisReport.value = null
    _currentOpening.value = null
    _drawOfferFeedback.value = null
    clearSelection()
    _lastMove.value = null
    initialGameState = GameState()
    _gameState.value = initialGameState
    _isBoardFlipped.value = (_humanColor.value == PieceColor.BLACK && _gameMode.value == GameMode.AI_VS_HUMAN)
    matchStartTime = System.currentTimeMillis()
    isCurrentMatchSaved = false
    initClockTimes()
    startClockTicker()
    updateLiveEvaluation()
    checkAndTriggerAiMove()
  }

  fun resignGame(player: PieceColor) {
    if (_gameState.value.isGameOver) return
    val winner = player.opposite()
    val finalState = _gameState.value.copy(
      isGameOver = true,
      outcome = GameOutcome.Resignation(winner)
    )
    _gameState.value = finalState
    _showGameOverDialog.value = true
    ChessSoundManager.playGameOverSound()
    saveMatchRecordIfNeeded(finalState)
  }

  fun offerDraw() {
    if (_gameState.value.isGameOver) return
    if (_gameMode.value == GameMode.AI_VS_HUMAN) {
      val eval = _evalCentipawns.value
      val movesCount = _gameState.value.moveHistory.size
      if (movesCount >= 15 && abs(eval) <= 60) {
        _drawOfferFeedback.value = "AI accepted the draw proposal."
        val finalState = _gameState.value.copy(
          isGameOver = true,
          outcome = GameOutcome.Draw("Draw agreed by mutual consent.")
        )
        _gameState.value = finalState
        _showGameOverDialog.value = true
        saveMatchRecordIfNeeded(finalState)
      } else {
        _drawOfferFeedback.value = "AI declined the draw offer and continues playing."
      }
    } else {
      _drawOfferFeedback.value = "Draw agreed by mutual agreement."
      val finalState = _gameState.value.copy(
        isGameOver = true,
        outcome = GameOutcome.Draw("Draw agreed by mutual consent.")
      )
      _gameState.value = finalState
      _showGameOverDialog.value = true
      saveMatchRecordIfNeeded(finalState)
    }
  }

  fun clearDrawOfferFeedback() {
    _drawOfferFeedback.value = null
  }

  // Room Persistence Auto-Save
  private fun saveMatchRecordIfNeeded(state: GameState) {
    if (isCurrentMatchSaved) return
    if (state.moveHistory.isEmpty() && _gameMode.value != GameMode.TACTICAL_PUZZLES) return

    val repo = matchRepository ?: return
    isCurrentMatchSaved = true

    val modeTitle = when (_gameMode.value) {
      GameMode.AI_VS_HUMAN -> "vs Computer"
      GameMode.TWO_PLAYER -> "Pass & Play"
      GameMode.TACTICAL_PUZZLES -> "Tactical Puzzle"
      GameMode.BOARD_EDITOR -> "Board Setup"
    }

    val diffTitle = if (_gameMode.value == GameMode.AI_VS_HUMAN) _difficulty.value.title else null
    val pColor = if (_gameMode.value == GameMode.AI_VS_HUMAN) _humanColor.value.displayName else "White / Black"
    val tControl = _timeControl.value.title
    val outcome = state.outcome

    val headline = outcome?.headline ?: "Match Concluded"
    val reason = outcome?.description ?: "Game ended"
    val winner = when (outcome) {
      is GameOutcome.Checkmate -> outcome.winner.name
      is GameOutcome.Timeout -> outcome.winner.name
      is GameOutcome.Resignation -> outcome.winner.name
      is GameOutcome.PuzzleSolved -> "WHITE"
      is GameOutcome.Stalemate, is GameOutcome.Draw -> "DRAW"
      null -> null
    }

    val opening = _currentOpening.value
    val pgn = getPgn()
    val startFen = ChessRules.toFen(initialGameState)
    val finalFen = ChessRules.toFen(state)
    val duration = maxOf(1L, (System.currentTimeMillis() - matchStartTime) / 1000)

    viewModelScope.launch {
      val record = MatchRecordEntity(
        timestamp = System.currentTimeMillis(),
        gameMode = modeTitle,
        difficulty = diffTitle,
        playerColor = pColor,
        timeControl = tControl,
        resultHeadline = headline,
        resultReason = reason,
        winnerColor = winner,
        totalMoves = state.fullmoveNumber,
        pgn = pgn,
        openingName = opening?.fullName,
        openingEco = opening?.eco,
        startFen = startFen,
        finalFen = finalFen,
        durationSeconds = duration,
        whiteCapturedCount = state.capturedByWhite.size,
        blackCapturedCount = state.capturedByBlack.size
      )
      repo.insertMatch(record)
    }
  }

  // Replay Saved Match from Database History
  fun replayMatchFromHistory(match: MatchRecordEntity) {
    aiJob?.cancel()
    _isAiThinking.value = false
    _showGameOverDialog.value = false
    _pendingPromotionMove.value = null
    _hintMove.value = null
    clearSelection()

    val startState = ChessRules.fromFen(match.startFen) ?: GameState()
    initialGameState = startState
    _gameMode.value = GameMode.TWO_PLAYER

    // Replay moves into state
    var state = startState
    val uciTokens = extractUciTokens(match.pgn)
    for (token in uciTokens) {
      val next = ChessRules.applyUciMove(state, token)
      if (next != null) {
        state = next
      }
    }

    _gameState.value = state
    _lastMove.value = state.moveHistory.lastOrNull()?.move
    _currentOpening.value = if (match.openingName != null) {
      ChessOpening(match.openingEco ?: "", match.openingName, match.openingName, emptyList())
    } else null
    _viewingPlyIndex.value = 0 // start from move 1 in stepper
    updateLiveEvaluation()
  }

  private fun extractUciTokens(pgn: String): List<String> {
    // Extract SAN or UCI tokens from move history PGN
    val moveTokens = mutableListOf<String>()
    val lines = pgn.lines().filter { !it.startsWith("[") && it.isNotBlank() }
    val fullText = lines.joinToString(" ")
    val words = fullText.split("\\s+".toRegex())
    for (word in words) {
      val clean = word.replace("\\d+\\.".toRegex(), "").trim()
      if (clean.isNotBlank() && clean != "1-0" && clean != "0-1" && clean != "1/2-1/2" && clean != "*") {
        moveTokens.add(clean)
      }
    }
    return moveTokens
  }

  fun deleteMatch(id: Long) {
    viewModelScope.launch {
      matchRepository?.deleteMatchById(id)
    }
  }

  fun clearAllMatchHistory() {
    viewModelScope.launch {
      matchRepository?.clearAllMatches()
    }
  }

  // Tactical Puzzle Mode Controls
  fun startPuzzlesMode() {
    _gameMode.value = GameMode.TACTICAL_PUZZLES
    loadPuzzle(0)
  }

  fun loadPuzzle(index: Int) {
    val puzzles = TacticalPuzzleDatabase.puzzles
    if (puzzles.isEmpty()) return
    val safeIndex = index.coerceIn(0, puzzles.size - 1)
    _puzzleIndex.value = safeIndex
    val puzzle = puzzles[safeIndex]
    _currentPuzzle.value = puzzle
    _puzzleMoveIndex.value = 0
    _puzzleFeedback.value = null

    val loadedState = ChessRules.fromFen(puzzle.fen) ?: GameState()
    initialGameState = loadedState
    _gameState.value = loadedState
    _humanColor.value = puzzle.playerColor
    _isBoardFlipped.value = (puzzle.playerColor == PieceColor.BLACK)
    _lastMove.value = null
    _hintMove.value = null
    _showGameOverDialog.value = false
    matchStartTime = System.currentTimeMillis()
    isCurrentMatchSaved = false
    clearSelection()
    updateLiveEvaluation()
  }

  fun nextPuzzle() {
    val nextIdx = (_puzzleIndex.value + 1) % TacticalPuzzleDatabase.puzzles.size
    loadPuzzle(nextIdx)
  }

  fun resetPuzzle() {
    loadPuzzle(_puzzleIndex.value)
  }

  // Board Editor Controls
  fun startBoardEditor() {
    _gameMode.value = GameMode.BOARD_EDITOR
    aiJob?.cancel()
    _isAiThinking.value = false
    _showGameOverDialog.value = false
    _editorSelectedPiece.value = Piece(PieceType.QUEEN, PieceColor.WHITE)
    _lastMove.value = null
    _hintMove.value = null
    clearSelection()
  }

  fun selectEditorPiece(piece: Piece?) {
    _editorSelectedPiece.value = piece
  }

  private fun handleBoardEditorSquareClick(square: Square) {
    val currentBoard = _gameState.value.board.map { it.clone() }.toTypedArray()
    val pieceToPlace = _editorSelectedPiece.value
    currentBoard[square.row][square.col] = pieceToPlace
    _gameState.value = _gameState.value.copy(board = currentBoard)
  }

  fun clearEditorBoard() {
    val emptyBoard = Array(8) { arrayOfNulls<Piece>(8) }
    _gameState.value = _gameState.value.copy(board = emptyBoard)
  }

  fun resetEditorToStandard() {
    initialGameState = GameState()
    _gameState.value = initialGameState
  }

  fun playFromEditorPosition(sideToMove: PieceColor) {
    val customState = _gameState.value.copy(turn = sideToMove, isGameOver = false, outcome = null, moveHistory = emptyList())
    initialGameState = customState
    _gameState.value = customState
    _gameMode.value = GameMode.AI_VS_HUMAN
    _humanColor.value = sideToMove
    _isBoardFlipped.value = (sideToMove == PieceColor.BLACK)
    matchStartTime = System.currentTimeMillis()
    isCurrentMatchSaved = false
    updateLiveEvaluation()
    checkAndTriggerAiMove()
  }

  // Full Game Review / Analysis
  fun runGameAnalysis() {
    if (_gameState.value.moveHistory.isEmpty()) return
    viewModelScope.launch {
      _isAnalyzing.value = true
      val report = ChessAnalysisEngine.analyzeGame(_gameState.value, aiEngine)
      _analysisReport.value = report
      _isAnalyzing.value = false
    }
  }

  fun setDifficulty(diff: Difficulty) {
    _difficulty.value = diff
  }

  fun setGameMode(mode: GameMode) {
    _gameMode.value = mode
    newGame()
  }

  fun setHumanColor(color: PieceColor) {
    _humanColor.value = color
    _isBoardFlipped.value = (color == PieceColor.BLACK)
    newGame()
  }

  fun setTimeControl(control: TimeControl) {
    _timeControl.value = control
    newGame()
  }

  fun setBoardTheme(theme: BoardTheme) {
    _boardTheme.value = theme
  }

  fun setPieceStyle(style: PieceStyle) {
    _pieceStyle.value = style
  }

  fun setCoordinatesDisplay(display: CoordinatesDisplay) {
    _coordinatesDisplay.value = display
  }

  fun toggleAutoQueen() {
    _autoQueenEnabled.value = !_autoQueenEnabled.value
  }

  fun toggleBlindfoldMode() {
    _blindfoldMode.value = !_blindfoldMode.value
  }

  fun toggleDangerSquare() {
    _showDangerSquare.value = !_showDangerSquare.value
  }

  fun toggleLegalMoveDots() {
    _showLegalMoveDots.value = !_showLegalMoveDots.value
  }

  fun toggleLastMoveHighlight() {
    _showLastMoveHighlight.value = !_showLastMoveHighlight.value
  }

  fun toggleSound() {
    _soundEnabled.value = !_soundEnabled.value
    ChessSoundManager.isSoundEnabled = _soundEnabled.value
  }

  fun toggleEvalBar() {
    _showEvalBar.value = !_showEvalBar.value
  }

  fun toggleFlipBoard() {
    _isBoardFlipped.value = !_isBoardFlipped.value
  }

  fun dismissGameOverDialog() {
    _showGameOverDialog.value = false
  }

  fun getPgn(): String {
    val whiteName = if (_gameMode.value == GameMode.AI_VS_HUMAN) {
      if (_humanColor.value == PieceColor.WHITE) "You (Player)" else "AI (${_difficulty.value.title})"
    } else "White Player"

    val blackName = if (_gameMode.value == GameMode.AI_VS_HUMAN) {
      if (_humanColor.value == PieceColor.BLACK) "You (Player)" else "AI (${_difficulty.value.title})"
    } else "Black Player"

    return ChessRules.generatePgn(_gameState.value, whiteName, blackName)
  }

  fun getFen(): String {
    return ChessRules.toFen(_gameState.value)
  }

  fun loadCustomFen(fen: String): Boolean {
    val loadedState = ChessRules.fromFen(fen) ?: return false
    aiJob?.cancel()
    _isAiThinking.value = false
    _showGameOverDialog.value = false
    _pendingPromotionMove.value = null
    _hintMove.value = null
    _viewingPlyIndex.value = null
    clearSelection()
    _lastMove.value = null
    initialGameState = loadedState
    _gameState.value = loadedState
    matchStartTime = System.currentTimeMillis()
    isCurrentMatchSaved = false
    updateLiveEvaluation()
    checkAndTriggerAiMove()
    return true
  }

  private fun clearSelection() {
    _selectedSquare.value = null
    _legalMovesForSelected.value = emptyList()
  }
}
