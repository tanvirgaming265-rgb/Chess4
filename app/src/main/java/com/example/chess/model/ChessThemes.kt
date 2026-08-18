package com.example.chess.model

import androidx.compose.ui.graphics.Color

enum class BoardTheme(
  val title: String,
  val lightSquare: Color,
  val darkSquare: Color,
  val selectedColor: Color,
  val legalMoveDotColor: Color,
  val lastMoveColor: Color,
  val checkColor: Color
) {
  CLASSIC_TOURNAMENT(
    title = "Tournament Green",
    lightSquare = Color(0xFFEEEED2),
    darkSquare = Color(0xFF769656),
    selectedColor = Color(0x99F6F669),
    legalMoveDotColor = Color(0x66000000),
    lastMoveColor = Color(0x80F5F682),
    checkColor = Color(0xDDF87171)
  ),
  GRANDMASTER_WALNUT(
    title = "Warm Walnut",
    lightSquare = Color(0xFFF0D9B5),
    darkSquare = Color(0xFFB58863),
    selectedColor = Color(0x99CDA66A),
    legalMoveDotColor = Color(0x665C3D1E),
    lastMoveColor = Color(0x80E2B872),
    checkColor = Color(0xDDEF4444)
  ),
  MIDNIGHT_OBSIDIAN(
    title = "Midnight Slate",
    lightSquare = Color(0xFF94A3B8),
    darkSquare = Color(0xFF334155),
    selectedColor = Color(0x9938BDF8),
    legalMoveDotColor = Color(0x880284C7),
    lastMoveColor = Color(0x800EA5E9),
    checkColor = Color(0xDDF43F5E)
  ),
  CYBER_NEON(
    title = "Cyber Neon",
    lightSquare = Color(0xFF38BDF8),
    darkSquare = Color(0xFF1E1B4B),
    selectedColor = Color(0x99A855F7),
    legalMoveDotColor = Color(0x88E0E7FF),
    lastMoveColor = Color(0x80818CF8),
    checkColor = Color(0xDDF43F5E)
  ),
  LUXURY_GLASS(
    title = "Onyx & Silver",
    lightSquare = Color(0xFFE2E8F0),
    darkSquare = Color(0xFF475569),
    selectedColor = Color(0x99FBBF24),
    legalMoveDotColor = Color(0x770F172A),
    lastMoveColor = Color(0x80FDE047),
    checkColor = Color(0xDDFB7185)
  ),
  ROYAL_CRIMSON(
    title = "Royal Crimson",
    lightSquare = Color(0xFFFCE7F3),
    darkSquare = Color(0xFF9D174D),
    selectedColor = Color(0x99F472B6),
    legalMoveDotColor = Color(0x77831843),
    lastMoveColor = Color(0x80FBCFE8),
    checkColor = Color(0xDDEF4444)
  )
}

enum class PieceStyle(val title: String) {
  STAUNTON_CLASSIC("Staunton Classic"),
  MODERN_MINIMAL("Modern Minimal"),
  GOLD_AND_SILVER("Gold & Silver"),
  RETRO_PIXEL("Retro Club")
}

enum class CoordinatesDisplay(val title: String) {
  INSIDE("Inside Board"),
  NONE("Hidden")
}

enum class TimeControl(
  val title: String,
  val initialSeconds: Int,
  val incrementSeconds: Int,
  val category: String
) {
  UNLIMITED("Unlimited", 0, 0, "Casual"),
  BULLET_1_0("1 min", 60, 0, "Bullet"),
  BULLET_2_1("2 | 1", 120, 1, "Bullet"),
  BLITZ_3_0("3 min", 180, 0, "Blitz"),
  BLITZ_3_2("3 | 2", 180, 2, "Blitz"),
  BLITZ_5_0("5 min", 300, 0, "Blitz"),
  RAPID_10_0("10 min", 600, 0, "Rapid"),
  RAPID_15_10("15 | 10", 900, 10, "Rapid"),
  CLASSICAL_30_0("30 min", 1800, 0, "Classical")
}
