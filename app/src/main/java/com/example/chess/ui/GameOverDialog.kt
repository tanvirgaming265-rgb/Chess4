package com.example.chess.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.chess.model.GameOutcome
import com.example.chess.model.GameState
import com.example.chess.model.PieceColor

@Composable
fun GameOverDialog(
  gameState: GameState,
  onPlayAgain: () -> Unit,
  onReviewBoard: () -> Unit,
  onUndoLastMove: (() -> Unit)? = null,
  onDismiss: () -> Unit
) {
  val outcome = gameState.outcome ?: return

  val isCheckmate = outcome is GameOutcome.Checkmate
  val headline = outcome.headline
  val description = outcome.description

  val accentColor = when (outcome) {
    is GameOutcome.Checkmate -> if (outcome.winner == PieceColor.WHITE) Color(0xFFEAB308) else Color(0xFF6366F1)
    else -> Color(0xFF0EA5E9)
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 12.dp,
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .testTag("game_over_dialog")
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Outcome Badge Icon
        Box(
          modifier = Modifier
            .size(72.dp)
            .background(
              Brush.radialGradient(
                listOf(accentColor.copy(alpha = 0.25f), Color.Transparent)
              ),
              CircleShape
            )
            .background(accentColor.copy(alpha = 0.15f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isCheckmate) Icons.Default.EmojiEvents else Icons.Default.Handshake,
            contentDescription = "Game Over Trophy",
            tint = accentColor,
            modifier = Modifier.size(38.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Headline: "White Wins!", "Black Wins!", or "Game Drawn!"
        Text(
          text = headline,
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Black,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("game_outcome_headline")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle description
        Text(
          text = description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Match summary card
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "${gameState.fullmoveNumber}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Total Moves",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "${gameState.capturedByWhite.size + gameState.capturedByBlack.size}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Pieces Captured",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Actions: Play Again, Undo & Continue, Review Board
        Button(
          onClick = onPlayAgain,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("play_again_button")
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Play Again", fontWeight = FontWeight.Bold)
        }

        if (onUndoLastMove != null && gameState.moveHistory.isNotEmpty()) {
          Spacer(modifier = Modifier.height(8.dp))
          Button(
            onClick = onUndoLastMove,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp)
              .testTag("undo_game_over_button")
          ) {
            Icon(
              Icons.AutoMirrored.Filled.Undo,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Take Back Move & Continue",
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
          onClick = onReviewBoard,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("review_board_button")
        ) {
          Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Review Board", fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}
