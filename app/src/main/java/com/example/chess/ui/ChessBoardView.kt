package com.example.chess.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.BoardTheme
import com.example.chess.model.CoordinatesDisplay
import com.example.chess.model.GameState
import com.example.chess.model.Move
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceStyle
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import kotlin.math.abs
import kotlin.math.tanh

@Composable
fun ChessBoardView(
  gameState: GameState,
  selectedSquare: Square?,
  legalMoves: List<Move>,
  lastMove: Move?,
  hintMove: Move? = null,
  theme: BoardTheme = BoardTheme.CLASSIC_TOURNAMENT,
  pieceStyle: PieceStyle = PieceStyle.STAUNTON_CLASSIC,
  coordinatesDisplay: CoordinatesDisplay = CoordinatesDisplay.INSIDE,
  isBlindfoldMode: Boolean = false,
  showDangerSquare: Boolean = true,
  showLegalMoveDots: Boolean = true,
  showLastMoveHighlight: Boolean = true,
  showEvalBar: Boolean = true,
  evalCentipawns: Int = 0,
  isFlipped: Boolean = false,
  onSquareClicked: (Square) -> Unit,
  modifier: Modifier = Modifier
) {
  val legalTargets = remember(legalMoves) {
    legalMoves.associateBy { it.to }
  }

  // Find checked king's position
  val checkedKingSquare = remember(gameState.isCheck, gameState.turn, gameState.board) {
    if (gameState.isCheck && showDangerSquare) {
      for (r in 0..7) {
        for (c in 0..7) {
          val p = gameState.board[r][c]
          if (p != null && p.type == PieceType.KING && p.color == gameState.turn) {
            return@remember Square(r, c)
          }
        }
      }
    }
    null
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // 1. Horizontal Live Evaluation Bar at the Top
    if (showEvalBar) {
      LiveEvaluationBar(
        evalCentipawns = evalCentipawns,
        isFlipped = isFlipped,
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 6.dp)
      )
    }

    // 2. The Big Full-Width Chess Board
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .shadow(16.dp, RoundedCornerShape(14.dp))
        .clip(RoundedCornerShape(14.dp))
        .border(3.dp, Color(0xFF1E293B).copy(alpha = 0.8f), RoundedCornerShape(14.dp))
        .background(Color(0xFF0F172A))
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        for (displayRow in 0..7) {
          val actualRow = if (isFlipped) 7 - displayRow else displayRow

          Row(modifier = Modifier.weight(1f)) {
            for (displayCol in 0..7) {
              val actualCol = if (isFlipped) 7 - displayCol else displayCol
              val currentSquare = Square(actualRow, actualCol)
              val piece = gameState.pieceAt(currentSquare)

              val isSelected = selectedSquare == currentSquare
              val isLastMove = showLastMoveHighlight && lastMove != null && (lastMove.from == currentSquare || lastMove.to == currentSquare)
              val isCheckedKing = checkedKingSquare == currentSquare
              val isHintFrom = hintMove != null && hintMove.from == currentSquare
              val isHintTo = hintMove != null && hintMove.to == currentSquare
              val legalMove = if (showLegalMoveDots) legalTargets[currentSquare] else null

              val showRank = coordinatesDisplay == CoordinatesDisplay.INSIDE && displayCol == 0
              val showFile = coordinatesDisplay == CoordinatesDisplay.INSIDE && displayRow == 7

              BoardSquare(
                square = currentSquare,
                piece = piece,
                theme = theme,
                pieceStyle = pieceStyle,
                isBlindfoldMode = isBlindfoldMode,
                isLight = currentSquare.isLightSquare,
                isSelected = isSelected,
                isLastMove = isLastMove,
                isCheckedKing = isCheckedKing,
                isHintFrom = isHintFrom,
                isHintTo = isHintTo,
                legalMove = legalMove,
                showRankLabel = showRank,
                showFileLabel = showFile,
                rankLabel = "${8 - actualRow}",
                fileLabel = "${('a'.code + actualCol).toChar()}",
                onClick = { onSquareClicked(currentSquare) },
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun LiveEvaluationBar(
  evalCentipawns: Int,
  isFlipped: Boolean,
  modifier: Modifier = Modifier
) {
  val whiteFraction = (0.5f + (0.5f * tanh(evalCentipawns / 400.0).toFloat())).coerceIn(0.04f, 0.96f)

  val animatedFraction by animateFloatAsState(
    targetValue = if (isFlipped) 1f - whiteFraction else whiteFraction,
    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
    label = "evalBarFraction"
  )

  val evalLabel = when {
    abs(evalCentipawns) >= 10000 -> if (evalCentipawns > 0) "+M" else "-M"
    evalCentipawns > 0 -> "+%.1f".format(evalCentipawns / 100.0)
    evalCentipawns < 0 -> "%.1f".format(evalCentipawns / 100.0)
    else -> "0.0"
  }

  val evalAdvantage = when {
    evalCentipawns > 30 -> "White is winning"
    evalCentipawns < -30 -> "Black is winning"
    else -> "Equal position"
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .height(18.dp)
      .shadow(4.dp, RoundedCornerShape(9.dp))
      .clip(RoundedCornerShape(9.dp))
      .background(Color(0xFF1E293B))
      .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(9.dp))
  ) {
    val totalWidth = maxWidth

    // Horizontal split bar
    Row(modifier = Modifier.fillMaxSize()) {
      // Left section (White if not flipped, Black if flipped)
      val leftSectionColor = if (!isFlipped) Color(0xFFF8FAFC) else Color(0xFF1E293B)
      val rightSectionColor = if (!isFlipped) Color(0xFF1E293B) else Color(0xFFF8FAFC)

      Box(
        modifier = Modifier
          .width(totalWidth * animatedFraction)
          .fillMaxHeight()
          .background(leftSectionColor)
      )

      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .background(rightSectionColor)
      )
    }

    // Centered / Smart Overlay Label
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = if (isFlipped) "Black" else "White",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = if (animatedFraction > 0.3f) (if (!isFlipped) Color(0xFF0F172A) else Color.White) else (if (!isFlipped) Color.White else Color(0xFF0F172A))
      )

      Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color(0xCC0F172A),
        modifier = Modifier.padding(vertical = 1.dp)
      ) {
        Text(
          text = evalLabel,
          fontSize = 10.sp,
          fontWeight = FontWeight.Black,
          color = Color.White,
          modifier = Modifier.padding(horizontal = 5.dp, vertical = 0.5.dp)
        )
      }

      Text(
        text = if (isFlipped) "White" else "Black",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = if (animatedFraction < 0.7f) (if (!isFlipped) Color.White else Color(0xFF0F172A)) else (if (!isFlipped) Color(0xFF0F172A) else Color.White)
      )
    }
  }
}

@Composable
fun BoardSquare(
  square: Square,
  piece: Piece?,
  theme: BoardTheme,
  pieceStyle: PieceStyle,
  isBlindfoldMode: Boolean,
  isLight: Boolean,
  isSelected: Boolean,
  isLastMove: Boolean,
  isCheckedKing: Boolean,
  isHintFrom: Boolean,
  isHintTo: Boolean,
  legalMove: Move?,
  showRankLabel: Boolean,
  showFileLabel: Boolean,
  rankLabel: String,
  fileLabel: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val baseColor = if (isLight) theme.lightSquare else theme.darkSquare
  val labelColor = if (isLight) theme.darkSquare else theme.lightSquare

  val interactionSource = remember { MutableInteractionSource() }

  // Pulsing hint animation
  val infiniteTransition = rememberInfiniteTransition(label = "hintPulse")
  val hintPulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 0.9f,
    animationSpec = infiniteRepeatable(
      animation = tween(600),
      repeatMode = RepeatMode.Reverse
    ),
    label = "hintAlpha"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(baseColor)
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
      )
      .testTag("square_${square.row}_${square.col}"),
    contentAlignment = Alignment.Center
  ) {
    // 1. Last move background overlay
    if (isLastMove) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(theme.lastMoveColor)
      )
    }

    // 2. Selected square background overlay
    if (isSelected) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(theme.selectedColor)
      )
    }

    // 3. Checked king alert highlight
    if (isCheckedKing) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(theme.checkColor)
      )
    }

    // 4. Tactical Hint Highlight
    if (isHintFrom || isHintTo) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0xFF38BDF8).copy(alpha = hintPulseAlpha * 0.55f))
          .border(2.5.dp, Color(0xFF0284C7).copy(alpha = hintPulseAlpha), RoundedCornerShape(2.dp))
      )
    }

    // 5. Rank & File coordinates
    if (showRankLabel) {
      Text(
        text = rankLabel,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        color = labelColor,
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(start = 2.5.dp, top = 1.5.dp)
      )
    }
    if (showFileLabel) {
      Text(
        text = fileLabel,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        color = labelColor,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 2.5.dp, bottom = 1.5.dp)
      )
    }

    // 6. Chess Piece
    if (piece != null) {
      ChessPieceView(
        piece = piece,
        pieceStyle = pieceStyle,
        isBlindfoldMode = isBlindfoldMode
      )
    }

    // 7. Legal Move Indicators
    if (legalMove != null) {
      if (piece == null && !legalMove.isEnPassant) {
        Box(
          modifier = Modifier
            .size(13.dp)
            .background(theme.legalMoveDotColor, CircleShape)
        )
      } else {
        Box(
          modifier = Modifier
            .fillMaxSize(0.85f)
            .border(3.5.dp, Color(0xE0EF4444), CircleShape)
        )
      }
    }
  }
}
