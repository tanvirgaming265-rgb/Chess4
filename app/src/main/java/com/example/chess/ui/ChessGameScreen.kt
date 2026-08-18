package com.example.chess.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.BoardTheme
import com.example.chess.model.Difficulty
import com.example.chess.model.GameMode
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.TimeControl
import com.example.chess.viewmodel.ChessViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessGameScreen(
  viewModel: ChessViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val gameState by viewModel.gameState.collectAsState()
  val selectedSquare by viewModel.selectedSquare.collectAsState()
  val legalMoves by viewModel.legalMovesForSelected.collectAsState()
  val isAiThinking by viewModel.isAiThinking.collectAsState()
  val pendingPromotionMove by viewModel.pendingPromotionMove.collectAsState()
  val showGameOverDialog by viewModel.showGameOverDialog.collectAsState()
  val gameMode by viewModel.gameMode.collectAsState()
  val humanColor by viewModel.humanColor.collectAsState()
  val difficulty by viewModel.difficulty.collectAsState()
  val undoMode by viewModel.undoMode.collectAsState()
  val matchHistory by viewModel.matchHistory.collectAsState()
  val matchCount by viewModel.matchCount.collectAsState()
  val boardTheme by viewModel.boardTheme.collectAsState()
  val pieceStyle by viewModel.pieceStyle.collectAsState()
  val coordinatesDisplay by viewModel.coordinatesDisplay.collectAsState()
  val autoQueenEnabled by viewModel.autoQueenEnabled.collectAsState()
  val blindfoldMode by viewModel.blindfoldMode.collectAsState()
  val showDangerSquare by viewModel.showDangerSquare.collectAsState()
  val showLegalMoveDots by viewModel.showLegalMoveDots.collectAsState()
  val showLastMoveHighlight by viewModel.showLastMoveHighlight.collectAsState()
  val timeControl by viewModel.timeControl.collectAsState()
  val whiteTimeSeconds by viewModel.whiteTimeSeconds.collectAsState()
  val blackTimeSeconds by viewModel.blackTimeSeconds.collectAsState()
  val isBoardFlipped by viewModel.isBoardFlipped.collectAsState()
  val lastMove by viewModel.lastMove.collectAsState()
  val hintMove by viewModel.hintMove.collectAsState()
  val isCalculatingHint by viewModel.isCalculatingHint.collectAsState()
  val evalCentipawns by viewModel.evalCentipawns.collectAsState()
  val showEvalBar by viewModel.showEvalBar.collectAsState()
  val soundEnabled by viewModel.soundEnabled.collectAsState()
  val viewingPlyIndex by viewModel.viewingPlyIndex.collectAsState()
  val currentOpening by viewModel.currentOpening.collectAsState()
  val analysisReport by viewModel.analysisReport.collectAsState()
  val isAnalyzing by viewModel.isAnalyzing.collectAsState()
  val currentPuzzle by viewModel.currentPuzzle.collectAsState()
  val puzzleIndex by viewModel.puzzleIndex.collectAsState()
  val puzzleFeedback by viewModel.puzzleFeedback.collectAsState()
  val drawOfferFeedback by viewModel.drawOfferFeedback.collectAsState()
  val editorSelectedPiece by viewModel.editorSelectedPiece.collectAsState()

  var showSettingsSheet by remember { mutableStateOf(false) }
  var showMatchHistorySheet by remember { mutableStateOf(false) }
  var showPgnDialog by remember { mutableStateOf(false) }
  var showFenDialog by remember { mutableStateOf(false) }
  var showAnalysisSheet by remember { mutableStateOf(false) }
  var showOpeningExplorer by remember { mutableStateOf(false) }

  val displayState = if (viewingPlyIndex != null) viewModel.getDisplayState() else gameState

  // Show draw feedback as toast
  LaunchedEffect(drawOfferFeedback) {
    drawOfferFeedback?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      viewModel.clearDrawOfferFeedback()
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Grandmaster Chess",
              fontWeight = FontWeight.Black,
              style = MaterialTheme.typography.titleLarge,
              letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer,
              modifier = Modifier.padding(horizontal = 2.dp)
            ) {
              Text(
                text = when (gameMode) {
                  GameMode.AI_VS_HUMAN -> "AI: ${difficulty.title}"
                  GameMode.TWO_PLAYER -> "2-Player"
                  GameMode.TACTICAL_PUZZLES -> "Puzzles"
                  GameMode.BOARD_EDITOR -> "Editor"
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        },
        actions = {
          // Tactical Hint Button
          IconButton(
            onClick = { viewModel.requestHint() },
            enabled = !gameState.isGameOver && !isAiThinking,
            modifier = Modifier.testTag("hint_button")
          ) {
            if (isCalculatingHint) {
              CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
              Icon(
                Icons.Default.Lightbulb,
                contentDescription = "Tactical Hint",
                tint = if (hintMove != null) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface
              )
            }
          }

          // Match History Button
          IconButton(
            onClick = { showMatchHistorySheet = true },
            modifier = Modifier.testTag("match_history_top_button")
          ) {
            if (matchCount > 0) {
              BadgedBox(
                badge = {
                  Badge {
                    Text(
                      text = if (matchCount > 99) "99+" else "$matchCount",
                      fontSize = 9.sp
                    )
                  }
                }
              ) {
                Icon(Icons.Default.History, contentDescription = "Match History")
              }
            } else {
              Icon(Icons.Default.History, contentDescription = "Match History")
            }
          }

          // Restart / New Game
          IconButton(
            onClick = { viewModel.newGame() },
            modifier = Modifier.testTag("new_game_top_button")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = "New Game")
          }

          // Full Settings Bottom Sheet
          IconButton(
            onClick = { showSettingsSheet = true },
            modifier = Modifier.testTag("settings_button")
          ) {
            Icon(Icons.Default.Tune, contentDescription = "Settings & Themes")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .widthIn(max = 600.dp)
          .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // Mode Selection Strip
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChip(
            selected = gameMode == GameMode.AI_VS_HUMAN,
            onClick = { viewModel.setGameMode(GameMode.AI_VS_HUMAN) },
            leadingIcon = { Icon(Icons.Default.Computer, contentDescription = null, modifier = Modifier.size(14.dp)) },
            label = { Text("vs Computer", fontSize = 11.sp) }
          )
          FilterChip(
            selected = gameMode == GameMode.TWO_PLAYER,
            onClick = { viewModel.setGameMode(GameMode.TWO_PLAYER) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp)) },
            label = { Text("Pass & Play", fontSize = 11.sp) }
          )
          FilterChip(
            selected = gameMode == GameMode.TACTICAL_PUZZLES,
            onClick = { viewModel.startPuzzlesMode() },
            leadingIcon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(14.dp)) },
            label = { Text("Tactics", fontSize = 11.sp) }
          )
          FilterChip(
            selected = gameMode == GameMode.BOARD_EDITOR,
            onClick = { viewModel.startBoardEditor() },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp)) },
            label = { Text("Setup", fontSize = 11.sp) }
          )
        }

        // Live Opening Banner
        if (currentOpening != null && gameMode != GameMode.TACTICAL_PUZZLES && gameMode != GameMode.BOARD_EDITOR) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showOpeningExplorer = true }
              .padding(vertical = 2.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = MaterialTheme.colorScheme.primary
                ) {
                  Text(
                    text = currentOpening!!.eco,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                  )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = currentOpening!!.fullName,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
              Icon(
                Icons.Default.Book,
                contentDescription = "Explore Opening",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        // Tactical Puzzle Information Card
        if (gameMode == GameMode.TACTICAL_PUZZLES && currentPuzzle != null) {
          val puzzle = currentPuzzle!!
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(text = puzzle.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.tertiary) {
                    Text(
                      text = "${puzzle.rating} Elo",
                      fontSize = 10.sp,
                      color = MaterialTheme.colorScheme.onTertiary,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                  }
                }
                Text(
                  text = puzzleFeedback ?: puzzle.description,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onTertiaryContainer
                )
              }
              OutlinedButton(
                onClick = { viewModel.nextPuzzle() },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(start = 6.dp)
              ) {
                Text("Next", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        // Top Player Card with Clock
        val topPlayerColor = if (isBoardFlipped) PieceColor.WHITE else PieceColor.BLACK
        val bottomPlayerColor = topPlayerColor.opposite()

        val isTopPlayerAi = gameMode == GameMode.AI_VS_HUMAN && topPlayerColor != humanColor
        val isBottomPlayerAi = gameMode == GameMode.AI_VS_HUMAN && bottomPlayerColor != humanColor

        val topPlayerCaptured = if (topPlayerColor == PieceColor.WHITE) displayState.capturedByWhite else displayState.capturedByBlack
        val bottomPlayerCaptured = if (bottomPlayerColor == PieceColor.WHITE) displayState.capturedByWhite else displayState.capturedByBlack

        val topAdvantage = if (topPlayerColor == PieceColor.WHITE) displayState.materialAdvantageWhite else -displayState.materialAdvantageWhite
        val bottomAdvantage = -topAdvantage

        val topTimeSeconds = if (topPlayerColor == PieceColor.WHITE) whiteTimeSeconds else blackTimeSeconds
        val bottomTimeSeconds = if (bottomPlayerColor == PieceColor.WHITE) whiteTimeSeconds else blackTimeSeconds

        if (gameMode != GameMode.BOARD_EDITOR) {
          PlayerHeaderCard(
            name = if (isTopPlayerAi) "AI ${difficulty.title} (${difficulty.elo})" else "${topPlayerColor.displayName} Player",
            color = topPlayerColor,
            isTurn = displayState.turn == topPlayerColor && !displayState.isGameOver,
            isAi = isTopPlayerAi,
            isThinking = isAiThinking && gameState.turn == topPlayerColor,
            timeControl = timeControl,
            remainingSeconds = topTimeSeconds,
            capturedPieces = topPlayerCaptured,
            materialAdvantage = topAdvantage,
            icon = if (isTopPlayerAi) Icons.Default.Computer else Icons.Default.Person,
            modifier = Modifier.fillMaxWidth()
          )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Grandmaster Chess Board & Live Eval Bar
        ChessBoardView(
          gameState = displayState,
          selectedSquare = selectedSquare,
          legalMoves = legalMoves,
          lastMove = lastMove,
          hintMove = hintMove,
          theme = boardTheme,
          pieceStyle = pieceStyle,
          coordinatesDisplay = coordinatesDisplay,
          isBlindfoldMode = blindfoldMode,
          showDangerSquare = showDangerSquare,
          showLegalMoveDots = showLegalMoveDots,
          showLastMoveHighlight = showLastMoveHighlight,
          showEvalBar = showEvalBar && gameMode != GameMode.BOARD_EDITOR,
          evalCentipawns = evalCentipawns,
          isFlipped = isBoardFlipped,
          onSquareClicked = { square -> viewModel.onSquareClicked(square) },
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        if (gameMode == GameMode.BOARD_EDITOR) {
          // Board Editor Piece Tray
          BoardEditorPalette(
            selectedPiece = editorSelectedPiece,
            onSelectPiece = { viewModel.selectEditorPiece(it) },
            onClearBoard = { viewModel.clearEditorBoard() },
            onResetStandard = { viewModel.resetEditorToStandard() },
            onPlayWhite = { viewModel.playFromEditorPosition(PieceColor.WHITE) },
            onPlayBlack = { viewModel.playFromEditorPosition(PieceColor.BLACK) },
            modifier = Modifier.fillMaxWidth()
          )
        } else {
          // Bottom Player Card with Clock
          PlayerHeaderCard(
            name = if (isBottomPlayerAi) "AI ${difficulty.title} (${difficulty.elo})" else if (gameMode == GameMode.AI_VS_HUMAN) "You (${bottomPlayerColor.displayName})" else "${bottomPlayerColor.displayName} Player",
            color = bottomPlayerColor,
            isTurn = displayState.turn == bottomPlayerColor && !displayState.isGameOver,
            isAi = isBottomPlayerAi,
            isThinking = isAiThinking && gameState.turn == bottomPlayerColor,
            timeControl = timeControl,
            remainingSeconds = bottomTimeSeconds,
            capturedPieces = bottomPlayerCaptured,
            materialAdvantage = bottomAdvantage,
            icon = if (isBottomPlayerAi) Icons.Default.Computer else Icons.Default.Person,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(2.dp))

          // Move History Strip & Interactive Replay Stepper
          MoveHistoryTicker(
            moves = gameState.moveHistory.map { it.san },
            viewingPlyIndex = viewingPlyIndex,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(2.dp))

          // Professional Control Bar
          GrandmasterControlBar(
            canUndo = gameState.moveHistory.isNotEmpty() && !isAiThinking,
            hasMoveHistory = gameState.moveHistory.isNotEmpty(),
            isViewingHistory = viewingPlyIndex != null,
            onUndo = { viewModel.undoMove() },
            onFlip = { viewModel.toggleFlipBoard() },
            onJumpToStart = { viewModel.jumpToStart() },
            onStepBack = { viewModel.stepBack() },
            onStepForward = { viewModel.stepForward() },
            onJumpToEnd = { viewModel.jumpToEnd() },
            onOpenReview = {
              viewModel.runGameAnalysis()
              showAnalysisSheet = true
            },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    // Pawn Promotion Dialog
    if (pendingPromotionMove != null) {
      PromotionDialog(
        color = gameState.turn,
        onPieceSelected = { promoType -> viewModel.onPromotionSelected(promoType) },
        onDismiss = { viewModel.dismissPromotionDialog() }
      )
    }

    // Accurate Game Over Dialog
    if (showGameOverDialog && gameState.outcome != null) {
      GameOverDialog(
        gameState = gameState,
        onPlayAgain = {
          if (gameMode == GameMode.TACTICAL_PUZZLES) viewModel.nextPuzzle() else viewModel.newGame()
        },
        onReviewBoard = { viewModel.dismissGameOverDialog() },
        onUndoLastMove = { viewModel.undoLastMoveAndResume() },
        onDismiss = { viewModel.dismissGameOverDialog() }
      )
    }

    // Match History Bottom Sheet
    if (showMatchHistorySheet) {
      MatchHistorySheet(
        matches = matchHistory,
        onReplayMatch = { match ->
          viewModel.replayMatchFromHistory(match)
        },
        onDeleteMatch = { matchId ->
          viewModel.deleteMatch(matchId)
        },
        onClearAllHistory = {
          viewModel.clearAllMatchHistory()
        },
        onDismiss = { showMatchHistorySheet = false }
      )
    }

    // Settings Bottom Sheet
    if (showSettingsSheet) {
      SettingsBottomSheet(
        viewModel = viewModel,
        currentTheme = boardTheme,
        currentPieceStyle = pieceStyle,
        currentCoordinatesDisplay = coordinatesDisplay,
        currentTimeControl = timeControl,
        currentDifficulty = difficulty,
        currentGameMode = gameMode,
        currentHumanColor = humanColor,
        currentUndoMode = undoMode,
        matchCount = matchCount,
        soundEnabled = soundEnabled,
        showEvalBar = showEvalBar,
        autoQueenEnabled = autoQueenEnabled,
        blindfoldMode = blindfoldMode,
        showDangerSquare = showDangerSquare,
        showLegalMoveDots = showLegalMoveDots,
        showLastMoveHighlight = showLastMoveHighlight,
        onDismiss = { showSettingsSheet = false },
        onOpenMatchHistory = { showMatchHistorySheet = true },
        onOpenPgnDialog = { showPgnDialog = true },
        onOpenFenDialog = { showFenDialog = true },
        onOpenAnalysis = {
          viewModel.runGameAnalysis()
          showAnalysisSheet = true
        },
        onOpenOpeningExplorer = { showOpeningExplorer = true }
      )
    }

    // Game Review Sheet
    if (showAnalysisSheet) {
      GameAnalysisSheet(
        report = analysisReport,
        isAnalyzing = isAnalyzing,
        onAnalyzeGame = { viewModel.runGameAnalysis() },
        onDismiss = { showAnalysisSheet = false }
      )
    }

    // Opening Explorer Sheet
    if (showOpeningExplorer) {
      OpeningExplorerSheet(
        currentOpening = currentOpening,
        onLoadOpeningMoves = { },
        onDismiss = { showOpeningExplorer = false }
      )
    }

    // PGN Dialog
    if (showPgnDialog) {
      PgnExportDialog(
        pgnText = viewModel.getPgn(),
        onDismiss = { showPgnDialog = false }
      )
    }

    // FEN Dialog
    if (showFenDialog) {
      FenDialog(
        currentFen = viewModel.getFen(),
        onLoadFen = { fen -> viewModel.loadCustomFen(fen) },
        onDismiss = { showFenDialog = false }
      )
    }
  }
}

@Composable
fun PlayerHeaderCard(
  name: String,
  color: PieceColor,
  isTurn: Boolean,
  isAi: Boolean,
  isThinking: Boolean,
  timeControl: TimeControl,
  remainingSeconds: Int,
  capturedPieces: List<Piece>,
  materialAdvantage: Int,
  icon: ImageVector,
  modifier: Modifier = Modifier
) {
  val borderColor by animateColorAsState(
    targetValue = if (isTurn) MaterialTheme.colorScheme.primary else Color.Transparent,
    label = "cardBorder"
  )

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(600),
      repeatMode = RepeatMode.Reverse
    ),
    label = "aiThinkingPulse"
  )

  val isLowTime = timeControl != TimeControl.UNLIMITED && remainingSeconds in 1..20 && isTurn

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isTurn) 0.85f else 0.45f),
    border = if (isTurn) androidx.compose.foundation.BorderStroke(2.dp, borderColor) else null,
    modifier = modifier
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 5.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left: Avatar & Player Details
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .background(
              if (color == PieceColor.WHITE) Color(0xFFF8FAFC) else Color(0xFF1E293B),
              CircleShape
            )
            .border(1.dp, Color(0x33000000), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (color == PieceColor.WHITE) Color(0xFF334155) else Color(0xFFE2E8F0),
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = name,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            if (isThinking) {
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Thinking...",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(pulseAlpha)
              )
            }
          }

          // Captured pieces tray & Advantage
          if (capturedPieces.isNotEmpty() || materialAdvantage > 0) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(top = 1.dp)
            ) {
              val grouped = capturedPieces.groupBy { it.type }
              grouped.forEach { (type, list) ->
                Text(
                  text = "${list.first().unicodeSymbol}${if (list.size > 1) "x${list.size}" else ""}",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(end = 2.dp)
                )
              }

              if (materialAdvantage > 0) {
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                  modifier = Modifier.padding(start = 2.dp)
                ) {
                  Text(
                    text = "+$materialAdvantage",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.dp)
                  )
                }
              }
            }
          }
        }
      }

      // Right: Precision Game Clock
      if (timeControl != TimeControl.UNLIMITED) {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeString = "%02d:%02d".format(minutes, seconds)

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isLowTime) Color(0xFFEF4444) else if (isTurn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
          tonalElevation = 2.dp
        ) {
          Text(
            text = timeString,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = if (isLowTime || isTurn) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }
    }
  }
}

@Composable
fun MoveHistoryTicker(
  moves: List<String>,
  viewingPlyIndex: Int?,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  LaunchedEffect(moves.size) {
    if (moves.isNotEmpty()) {
      scrollState.animateScrollTo(scrollState.maxValue)
    }
  }

  Surface(
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    modifier = modifier.height(34.dp)
  ) {
    if (moves.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
          text = "White to move · Ready to begin",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
      }
    } else {
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 8.dp)
          .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically
      ) {
        for (i in moves.indices step 2) {
          val moveNum = (i / 2) + 1
          val whiteMove = moves[i]
          val blackMove = moves.getOrNull(i + 1)

          val isWhiteInspected = viewingPlyIndex == i + 1
          val isBlackInspected = blackMove != null && viewingPlyIndex == i + 2

          Text(
            text = "$moveNum.",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 3.dp)
          )

          Text(
            text = whiteMove,
            fontSize = 11.sp,
            fontWeight = if (isWhiteInspected) FontWeight.Black else FontWeight.Medium,
            color = if (isWhiteInspected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
              .background(
                if (isWhiteInspected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                RoundedCornerShape(4.dp)
              )
              .padding(horizontal = 3.dp)
          )

          if (blackMove != null) {
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = blackMove,
              fontSize = 11.sp,
              fontWeight = if (isBlackInspected) FontWeight.Black else FontWeight.Medium,
              color = if (isBlackInspected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier
                .background(
                  if (isBlackInspected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                  RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 3.dp)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))
        }
      }
    }
  }
}

@Composable
fun GrandmasterControlBar(
  canUndo: Boolean,
  hasMoveHistory: Boolean,
  isViewingHistory: Boolean,
  onUndo: () -> Unit,
  onFlip: () -> Unit,
  onJumpToStart: () -> Unit,
  onStepBack: () -> Unit,
  onStepForward: () -> Unit,
  onJumpToEnd: () -> Unit,
  onOpenReview: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    modifier = modifier
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 6.dp, vertical = 2.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Undo
      IconButton(
        onClick = onUndo,
        enabled = canUndo,
        modifier = Modifier.testTag("undo_button")
      ) {
        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo Move", modifier = Modifier.size(20.dp))
      }

      // Replay Navigation
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onJumpToStart,
          enabled = hasMoveHistory,
          modifier = Modifier.testTag("jump_start_button")
        ) {
          Icon(Icons.Default.FastRewind, contentDescription = "Jump to start", modifier = Modifier.size(18.dp))
        }

        IconButton(
          onClick = onStepBack,
          enabled = hasMoveHistory,
          modifier = Modifier.testTag("step_back_button")
        ) {
          Icon(Icons.Default.NavigateBefore, contentDescription = "Step back", modifier = Modifier.size(20.dp))
        }

        IconButton(
          onClick = onStepForward,
          enabled = isViewingHistory,
          modifier = Modifier.testTag("step_forward_button")
        ) {
          Icon(Icons.Default.NavigateNext, contentDescription = "Step forward", modifier = Modifier.size(20.dp))
        }

        IconButton(
          onClick = onJumpToEnd,
          enabled = isViewingHistory,
          modifier = Modifier.testTag("jump_end_button")
        ) {
          Icon(Icons.Default.FastForward, contentDescription = "Jump to live", modifier = Modifier.size(18.dp))
        }
      }

      // Flip Board
      IconButton(
        onClick = onFlip,
        modifier = Modifier.testTag("flip_board_button")
      ) {
        Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip Board", modifier = Modifier.size(20.dp))
      }

      // Review
      IconButton(
        onClick = onOpenReview,
        enabled = hasMoveHistory,
        modifier = Modifier.testTag("review_game_button")
      ) {
        Icon(Icons.Default.QueryStats, contentDescription = "Game Review", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
      }
    }
  }
}
