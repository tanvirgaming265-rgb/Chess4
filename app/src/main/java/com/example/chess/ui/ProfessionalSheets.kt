package com.example.chess.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.chess.engine.ChessOpening
import com.example.chess.engine.GameAnalysisReport
import com.example.chess.engine.MoveQuality
import com.example.chess.engine.OpeningBook
import com.example.chess.model.BoardTheme
import com.example.chess.model.CoordinatesDisplay
import com.example.chess.model.Difficulty
import com.example.chess.model.GameMode
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceStyle
import com.example.chess.model.PieceType
import com.example.chess.model.TimeControl
import com.example.chess.viewmodel.ChessViewModel
import com.example.chess.viewmodel.UndoMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
  viewModel: ChessViewModel,
  currentTheme: BoardTheme,
  currentPieceStyle: PieceStyle,
  currentCoordinatesDisplay: CoordinatesDisplay,
  currentTimeControl: TimeControl,
  currentDifficulty: Difficulty,
  currentGameMode: GameMode,
  currentHumanColor: PieceColor,
  currentUndoMode: UndoMode,
  matchCount: Int,
  soundEnabled: Boolean,
  showEvalBar: Boolean,
  autoQueenEnabled: Boolean,
  blindfoldMode: Boolean,
  showDangerSquare: Boolean,
  showLegalMoveDots: Boolean,
  showLastMoveHighlight: Boolean,
  onDismiss: () -> Unit,
  onOpenMatchHistory: () -> Unit,
  onOpenPgnDialog: () -> Unit,
  onOpenFenDialog: () -> Unit,
  onOpenAnalysis: () -> Unit,
  onOpenOpeningExplorer: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 8.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .verticalScroll(rememberScrollState())
    ) {
      Text(
        text = "Professional Settings",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Grandmaster customization, match history, undo & board options",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Match History Card
      Card(
        onClick = {
          onDismiss()
          onOpenMatchHistory()
        },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth().testTag("settings_history_card")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.QueryStats,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Match History & Analytics",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = "$matchCount saved games with PGN and replay",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
              )
            }
          }
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primary
          ) {
            Text(
              text = "View",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Quick Action Hub (Review Game, Opening Book)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = {
            onDismiss()
            onOpenAnalysis()
          },
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.QueryStats, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Game Review", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = {
            onDismiss()
            onOpenOpeningExplorer()
          },
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Openings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Undo Mode Configuration
      SectionHeader(icon = Icons.Default.RestartAlt, title = "Undo Move Mode")
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        UndoMode.values().forEach { mode ->
          FilterChip(
            selected = mode == currentUndoMode,
            onClick = { viewModel.setUndoMode(mode) },
            label = { Text(mode.title, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          )
        }
      }
      Text(
        text = currentUndoMode.description,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 1. Board Themes
      SectionHeader(icon = Icons.Default.Palette, title = "Board Theme")
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        BoardTheme.values().forEach { theme ->
          val isSelected = theme == currentTheme
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .clickable { viewModel.setBoardTheme(theme) }
              .padding(2.dp)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                  if (isSelected) 3.dp else 1.dp,
                  if (isSelected) MaterialTheme.colorScheme.primary else Color(0x33000000),
                  RoundedCornerShape(8.dp)
                )
            ) {
              Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(theme.lightSquare))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(theme.darkSquare))
              }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = theme.title.substringBefore(" "),
              fontSize = 10.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Piece Set Style
      SectionHeader(icon = Icons.Default.Style, title = "Piece Style")
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        PieceStyle.values().forEach { style ->
          FilterChip(
            selected = style == currentPieceStyle,
            onClick = { viewModel.setPieceStyle(style) },
            label = { Text(style.title, fontSize = 10.sp) },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 3. Time Controls
      SectionHeader(icon = Icons.Default.Timer, title = "Time Control")
      val timeControls = listOf(
        TimeControl.UNLIMITED,
        TimeControl.BULLET_1_0,
        TimeControl.BLITZ_3_0,
        TimeControl.BLITZ_5_0,
        TimeControl.RAPID_10_0,
        TimeControl.CLASSICAL_30_0
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        timeControls.take(3).forEach { tc ->
          FilterChip(
            selected = tc == currentTimeControl,
            onClick = { viewModel.setTimeControl(tc) },
            label = { Text(tc.title, fontSize = 10.sp) },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          )
        }
      }
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        timeControls.drop(3).forEach { tc ->
          FilterChip(
            selected = tc == currentTimeControl,
            onClick = { viewModel.setTimeControl(tc) },
            label = { Text(tc.title, fontSize = 10.sp) },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 4. AI Difficulty
      SectionHeader(icon = Icons.Default.Speed, title = "Engine Level")
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Difficulty.values().forEach { diff ->
          FilterChip(
            selected = diff == currentDifficulty,
            onClick = { viewModel.setDifficulty(diff) },
            label = { Text(diff.title, fontSize = 9.sp, fontWeight = FontWeight.SemiBold) },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 5. Professional Toggles
      SectionHeader(icon = Icons.Default.AutoAwesome, title = "Game Options")
      ToggleRow(
        title = "Sound Effects",
        checked = soundEnabled,
        onCheckedChange = { viewModel.toggleSound() }
      )
      ToggleRow(
        title = "Live Evaluation Bar",
        checked = showEvalBar,
        onCheckedChange = { viewModel.toggleEvalBar() }
      )
      ToggleRow(
        title = "Auto-Queen on Promotion",
        checked = autoQueenEnabled,
        onCheckedChange = { viewModel.toggleAutoQueen() }
      )
      ToggleRow(
        title = "Show Legal Move Dots",
        checked = showLegalMoveDots,
        onCheckedChange = { viewModel.toggleLegalMoveDots() }
      )
      ToggleRow(
        title = "Highlight Last Move",
        checked = showLastMoveHighlight,
        onCheckedChange = { viewModel.toggleLastMoveHighlight() }
      )
      ToggleRow(
        title = "Danger / Check Warning",
        checked = showDangerSquare,
        onCheckedChange = { viewModel.toggleDangerSquare() }
      )
      ToggleRow(
        title = "Blindfold Training Mode",
        checked = blindfoldMode,
        onCheckedChange = { viewModel.toggleBlindfoldMode() }
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 6. Coordinates Display
      SectionHeader(icon = Icons.Default.Visibility, title = "Board Coordinates")
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        CoordinatesDisplay.values().forEach { coord ->
          FilterChip(
            selected = coord == currentCoordinatesDisplay,
            onClick = { viewModel.setCoordinatesDisplay(coord) },
            label = { Text(coord.title, fontSize = 11.sp) },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 7. PGN & FEN Actions
      SectionHeader(icon = Icons.Default.FileDownload, title = "PGN & FEN Tools")
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedButton(
          onClick = {
            onDismiss()
            onOpenPgnDialog()
          },
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Export PGN", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
          onClick = {
            onDismiss()
            onOpenFenDialog()
          },
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("FEN Position", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 8. Draw & Resign Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = {
            onDismiss()
            viewModel.offerDraw()
          },
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Handshake, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Offer Draw", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }

        Button(
          onClick = {
            onDismiss()
            viewModel.resignGame(currentHumanColor)
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "Resign", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun ToggleRow(
  title: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange
    )
  }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = title,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameAnalysisSheet(
  report: GameAnalysisReport?,
  isAnalyzing: Boolean,
  onAnalyzeGame: () -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 8.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .verticalScroll(rememberScrollState())
    ) {
      Text(
        text = "Game Review & Accuracy",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Deep move classification and accuracy rating",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(16.dp))

      if (isAnalyzing) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          CircularProgressIndicator(modifier = Modifier.size(40.dp))
          Spacer(modifier = Modifier.height(12.dp))
          Text("Analyzing full game with Grandmaster engine...", fontSize = 13.sp)
        }
      } else if (report == null) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text("Ready to analyze your played moves and calculate accuracy scores.")
          Spacer(modifier = Modifier.height(14.dp))
          Button(
            onClick = onAnalyzeGame,
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.QueryStats, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Run Game Review")
          }
        }
      } else {
        // Accuracy Score Cards
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          AccuracyCard(
            player = "White",
            accuracy = report.whiteAccuracy,
            color = Color(0xFFF8FAFC),
            textColor = Color(0xFF0F172A),
            modifier = Modifier.weight(1f)
          )
          AccuracyCard(
            player = "Black",
            accuracy = report.blackAccuracy,
            color = Color(0xFF1E293B),
            textColor = Color(0xFFF8FAFC),
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Move Quality Badges Summary
        Text(
          text = "Move Breakdown",
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        listOf(
          MoveQuality.BRILLIANT,
          MoveQuality.GREAT,
          MoveQuality.BEST,
          MoveQuality.EXCELLENT,
          MoveQuality.INACCURACY,
          MoveQuality.MISTAKE,
          MoveQuality.BLUNDER
        ).forEach { quality ->
          val whiteCount = report.whiteMoveCounts[quality] ?: 0
          val blackCount = report.blackMoveCounts[quality] ?: 0

          if (whiteCount > 0 || blackCount > 0) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(18.dp)
                    .background(quality.badgeColor, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = quality.symbol,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = quality.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              }
              Text(
                text = "W: $whiteCount | B: $blackCount",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Move by Move Log
        Text(
          text = "Key Move Analysis",
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
          report.analyzedMoves.take(20).forEach { move ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "${move.moveNumber}. ${if (move.color == PieceColor.BLACK) ".. " else ""}${move.san}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = move.quality.badgeColor
                ) {
                  Text(
                    text = move.quality.title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                  )
                }
              }
              Text(
                text = "eval: %.1f".format(move.evalAfterCentipawns / 100.0),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun AccuracyCard(
  player: String,
  accuracy: Float,
  color: Color,
  textColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = color),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = player,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = textColor.copy(alpha = 0.7f)
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "%.1f%%".format(accuracy),
        fontSize = 22.sp,
        fontWeight = FontWeight.Black,
        color = textColor
      )
      Text(
        text = "Accuracy",
        fontSize = 10.sp,
        color = textColor.copy(alpha = 0.6f)
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpeningExplorerSheet(
  currentOpening: ChessOpening?,
  onLoadOpeningMoves: (List<String>) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 8.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .verticalScroll(rememberScrollState())
    ) {
      Text(
        text = "Opening Explorer",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Master repertoires and recognized lines",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(14.dp))

      if (currentOpening != null) {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primary
              ) {
                Text(
                  text = currentOpening.eco,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Currently Detected Opening",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = currentOpening.fullName,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }

      Text(
        text = "Master Openings Catalog",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
      )
      Spacer(modifier = Modifier.height(8.dp))

      OpeningBook.openings.forEach { op ->
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = op.eco,
                  fontWeight = FontWeight.Black,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = op.fullName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
              }
              Text(
                text = "${op.movesUci.size} moves in mainline",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
fun BoardEditorPalette(
  selectedPiece: Piece?,
  onSelectPiece: (Piece?) -> Unit,
  onClearBoard: () -> Unit,
  onResetStandard: () -> Unit,
  onPlayWhite: () -> Unit,
  onPlayBlack: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(
        text = "Board Editor Setup",
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      // White Pieces
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        listOf(
          PieceType.PAWN,
          PieceType.KNIGHT,
          PieceType.BISHOP,
          PieceType.ROOK,
          PieceType.QUEEN,
          PieceType.KING
        ).forEach { type ->
          val p = Piece(type, PieceColor.WHITE)
          val isSelected = selectedPiece == p
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
              .border(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else Color(0x33000000),
                RoundedCornerShape(8.dp)
              )
              .clickable { onSelectPiece(p) },
            contentAlignment = Alignment.Center
          ) {
            Text(text = p.unicodeSymbol, fontSize = 22.sp, color = Color.White)
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Black Pieces + Eraser
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        listOf(
          PieceType.PAWN,
          PieceType.KNIGHT,
          PieceType.BISHOP,
          PieceType.ROOK,
          PieceType.QUEEN,
          PieceType.KING
        ).forEach { type ->
          val p = Piece(type, PieceColor.BLACK)
          val isSelected = selectedPiece == p
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
              .border(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else Color(0x33000000),
                RoundedCornerShape(8.dp)
              )
              .clickable { onSelectPiece(p) },
            contentAlignment = Alignment.Center
          ) {
            Text(text = p.unicodeSymbol, fontSize = 22.sp, color = Color(0xFF0F172A))
          }
        }

        // Eraser button
        val isEraser = selectedPiece == null
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isEraser) MaterialTheme.colorScheme.errorContainer else Color.Transparent)
            .border(
              if (isEraser) 2.dp else 1.dp,
              if (isEraser) MaterialTheme.colorScheme.error else Color(0x33000000),
              RoundedCornerShape(8.dp)
            )
            .clickable { onSelectPiece(null) },
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Delete, contentDescription = "Eraser", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Control Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onClearBoard,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f)
        ) {
          Text("Clear", fontSize = 11.sp)
        }
        OutlinedButton(
          onClick = onResetStandard,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f)
        ) {
          Text("Standard", fontSize = 11.sp)
        }
        Button(
          onClick = onPlayWhite,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f)
        ) {
          Text("Play White", fontSize = 11.sp)
        }
        Button(
          onClick = onPlayBlack,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f)
        ) {
          Text("Play Black", fontSize = 11.sp)
        }
      }
    }
  }
}

@Composable
fun PgnExportDialog(
  pgnText: String,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 10.dp,
      modifier = Modifier.fillMaxWidth(0.95f)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Export Game PGN",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Standard Portable Game Notation with full move history",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
        ) {
          Text(
            text = pgnText,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            modifier = Modifier
              .padding(12.dp)
              .verticalScroll(rememberScrollState())
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText("Chess PGN", pgnText)
              clipboard.setPrimaryClip(clip)
              Toast.makeText(context, "PGN copied to clipboard!", Toast.LENGTH_SHORT).show()
              onDismiss()
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Copy PGN")
          }

          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text("Close")
          }
        }
      }
    }
  }
}

@Composable
fun FenDialog(
  currentFen: String,
  onLoadFen: (String) -> Boolean,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var inputFen by remember { mutableStateOf(currentFen) }
  var isError by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 10.dp,
      modifier = Modifier.fillMaxWidth(0.95f)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "FEN Board Position",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "View or paste a Forsyth-Edwards Notation string",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = inputFen,
          onValueChange = {
            inputFen = it
            isError = false
          },
          label = { Text("FEN String") },
          isError = isError,
          textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
          ),
          modifier = Modifier.fillMaxWidth()
        )

        if (isError) {
          Text(
            text = "Invalid FEN format. Please verify the string.",
            color = MaterialTheme.colorScheme.error,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText("Chess FEN", currentFen)
              clipboard.setPrimaryClip(clip)
              Toast.makeText(context, "FEN copied to clipboard!", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Copy FEN", fontSize = 11.sp)
          }

          Button(
            onClick = {
              val success = onLoadFen(inputFen)
              if (success) {
                Toast.makeText(context, "Position loaded!", Toast.LENGTH_SHORT).show()
                onDismiss()
              } else {
                isError = true
              }
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Load", fontSize = 11.sp)
          }
        }
      }
    }
  }
}
