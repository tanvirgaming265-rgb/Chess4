package com.example.chess.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceStyle
import com.example.chess.model.PieceType

@Composable
fun ChessPieceView(
  piece: Piece,
  pieceStyle: PieceStyle = PieceStyle.STAUNTON_CLASSIC,
  isBlindfoldMode: Boolean = false,
  modifier: Modifier = Modifier
) {
  if (isBlindfoldMode) {
    // In blindfold mode, show a very subtle indicator dot or nothing to train visualization
    Box(
      modifier = modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .background(
            if (piece.color == PieceColor.WHITE) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.35f),
            CircleShape
          )
      )
    }
    return
  }

  val isWhite = piece.color == PieceColor.WHITE

  val (textColor, shadowColor, fontSize) = when (pieceStyle) {
    PieceStyle.STAUNTON_CLASSIC -> {
      val text = if (isWhite) Color(0xFFFFFFFF) else Color(0xFF1E293B)
      val shadow = if (isWhite) Color(0xCC000000) else Color(0x99FFFFFF)
      Triple(text, shadow, 38.sp)
    }
    PieceStyle.GOLD_AND_SILVER -> {
      val text = if (isWhite) Color(0xFFFDE047) else Color(0xFF0F172A)
      val shadow = if (isWhite) Color(0xFF854D0E) else Color(0xFF94A3B8)
      Triple(text, shadow, 38.sp)
    }
    PieceStyle.MODERN_MINIMAL -> {
      val text = if (isWhite) Color(0xFFF8FAFC) else Color(0xFF090D16)
      val shadow = if (isWhite) Color(0x88000000) else Color(0x66FFFFFF)
      Triple(text, shadow, 36.sp)
    }
    PieceStyle.RETRO_PIXEL -> {
      val text = if (isWhite) Color(0xFFE2E8F0) else Color(0xFF1E1B4B)
      val shadow = if (isWhite) Color(0xAA334155) else Color(0xAA818CF8)
      Triple(text, shadow, 37.sp)
    }
  }

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = piece.unicodeSymbol,
      fontSize = fontSize,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.SansSerif,
      textAlign = TextAlign.Center,
      style = TextStyle(
        color = textColor,
        shadow = Shadow(
          color = shadowColor,
          blurRadius = if (isWhite) 8f else 4f
        )
      ),
      modifier = Modifier.padding(1.dp)
    )
  }
}
