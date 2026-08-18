package com.example.chess.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.data.MatchRecordEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchHistorySheet(
  matches: List<MatchRecordEntity>,
  onReplayMatch: (MatchRecordEntity) -> Unit,
  onDeleteMatch: (Long) -> Unit,
  onClearAllHistory: () -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var selectedFilter by remember { mutableStateOf("ALL") }
  var showClearConfirmDialog by remember { mutableStateOf(false) }

  val filteredMatches = remember(matches, selectedFilter) {
    when (selectedFilter) {
      "AI" -> matches.filter { it.gameMode.contains("Computer", ignoreCase = true) || it.gameMode.contains("AI", ignoreCase = true) }
      "2P" -> matches.filter { it.gameMode.contains("Player", ignoreCase = true) || it.gameMode.contains("Pass", ignoreCase = true) }
      "WINS" -> matches.filter { it.winnerColor == "WHITE" || (it.winnerColor == "BLACK" && it.playerColor.equals("Black", ignoreCase = true)) }
      "DRAWS" -> matches.filter { it.winnerColor == "DRAW" }
      else -> matches
    }
  }

  // Calculate statistics
  val totalGames = matches.size
  val wins = matches.count {
    (it.playerColor.equals("White", ignoreCase = true) && it.winnerColor == "WHITE") ||
      (it.playerColor.equals("Black", ignoreCase = true) && it.winnerColor == "BLACK")
  }
  val draws = matches.count { it.winnerColor == "DRAW" }
  val losses = totalGames - wins - draws
  val winRate = if (totalGames > 0) ((wins.toFloat() / totalGames) * 100).toInt() else 0

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier.fillMaxHeight(0.92f)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .testTag("match_history_sheet")
    ) {
      // Header Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.History,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Match History",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Black
            )
            Text(
              text = "$totalGames recorded matches",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Row {
          if (matches.isNotEmpty()) {
            IconButton(
              onClick = { showClearConfirmDialog = true },
              modifier = Modifier.testTag("clear_history_button")
            ) {
              Icon(
                Icons.Default.DeleteSweep,
                contentDescription = "Clear All History",
                tint = MaterialTheme.colorScheme.error
              )
            }
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Overview Stats Card
      if (totalGames > 0) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Performance Overview",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "$winRate% Win Rate",
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Black,
                  color = MaterialTheme.colorScheme.primary
                )
              }

              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBadge(label = "Wins", count = wins, color = Color(0xFF10B981))
                StatBadge(label = "Draws", count = draws, color = Color(0xFF0EA5E9))
                StatBadge(label = "Losses", count = maxOf(0, losses), color = Color(0xFFEF4444))
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
              progress = { if (totalGames > 0) (wins.toFloat() / totalGames) else 0f },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
              color = Color(0xFF10B981),
              trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Strip
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChip(
            selected = selectedFilter == "ALL",
            onClick = { selectedFilter = "ALL" },
            label = { Text("All ($totalGames)", fontSize = 11.sp) }
          )
          FilterChip(
            selected = selectedFilter == "AI",
            onClick = { selectedFilter = "AI" },
            leadingIcon = { Icon(Icons.Default.Computer, contentDescription = null, modifier = Modifier.size(12.dp)) },
            label = { Text("vs AI", fontSize = 11.sp) }
          )
          FilterChip(
            selected = selectedFilter == "2P",
            onClick = { selectedFilter = "2P" },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp)) },
            label = { Text("2-Player", fontSize = 11.sp) }
          )
          FilterChip(
            selected = selectedFilter == "WINS",
            onClick = { selectedFilter = "WINS" },
            leadingIcon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(12.dp)) },
            label = { Text("Wins ($wins)", fontSize = 11.sp) }
          )
          FilterChip(
            selected = selectedFilter == "DRAWS",
            onClick = { selectedFilter = "DRAWS" },
            leadingIcon = { Icon(Icons.Default.Handshake, contentDescription = null, modifier = Modifier.size(12.dp)) },
            label = { Text("Draws ($draws)", fontSize = 11.sp) }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))
      }

      // Match List or Empty State
      if (filteredMatches.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = if (totalGames == 0) "No Match History Yet" else "No matches found for this filter",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (totalGames == 0) "Play games vs AI or Pass & Play to save full match records automatically!" else "Try selecting another filter chip above.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 24.dp)
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredMatches, key = { it.id }) { match ->
            MatchHistoryItemCard(
              match = match,
              onReplay = {
                onReplayMatch(match)
                onDismiss()
              },
              onCopyPgn = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Chess PGN", match.pgn)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "PGN copied to clipboard!", Toast.LENGTH_SHORT).show()
              },
              onDelete = { onDeleteMatch(match.id) }
            )
          }
          item {
            Spacer(modifier = Modifier.height(16.dp))
          }
        }
      }
    }
  }

  // Clear Confirmation Dialog
  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
      title = { Text("Clear Match History?", fontWeight = FontWeight.Bold) },
      text = { Text("Are you sure you want to permanently delete all $totalGames saved match records? This action cannot be undone.") },
      confirmButton = {
        Button(
          onClick = {
            onClearAllHistory()
            showClearConfirmDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Clear All")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun StatBadge(
  label: String,
  count: Int,
  color: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = color.copy(alpha = 0.15f),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "$count",
        fontWeight = FontWeight.Black,
        fontSize = 15.sp,
        color = color
      )
      Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = color
      )
    }
  }
}

@Composable
fun MatchHistoryItemCard(
  match: MatchRecordEntity,
  onReplay: () -> Unit,
  onCopyPgn: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()) }
  val formattedDate = remember(match.timestamp) { dateFormat.format(Date(match.timestamp)) }

  val resultColor = when (match.winnerColor) {
    "WHITE" -> Color(0xFFEAB308)
    "BLACK" -> Color(0xFF6366F1)
    else -> Color(0xFF0EA5E9)
  }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Top Bar: Game Mode & Date
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
          ) {
            Text(
              text = match.gameMode + if (match.difficulty != null) " (${match.difficulty})" else "",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
          ) {
            Text(
              text = match.timeControl,
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Text(
          text = formattedDate,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Main Result Info
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .background(resultColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = match.resultHeadline,
              fontWeight = FontWeight.Black,
              fontSize = 15.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = match.resultReason,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          if (match.openingName != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (match.openingEco != null) {
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                  Text(
                    text = match.openingEco,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                  )
                }
                Spacer(modifier = Modifier.width(4.dp))
              }
              Text(
                text = match.openingName,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }

        // Moves & Captures summary
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "${match.totalMoves} moves",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Played as ${match.playerColor}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(8.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row {
          IconButton(
            onClick = onCopyPgn,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              Icons.Default.ContentCopy,
              contentDescription = "Copy PGN",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }

          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              Icons.Default.Delete,
              contentDescription = "Delete Match",
              tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Button(
          onClick = onReplay,
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier.height(34.dp)
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "Replay Match", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
