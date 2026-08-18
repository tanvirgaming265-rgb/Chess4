package com.example.chess.model

data class TacticalPuzzle(
  val id: String,
  val title: String,
  val category: String,
  val rating: Int,
  val fen: String,
  val playerColor: PieceColor,
  val targetMovesUci: List<String>,
  val description: String,
  val hint: String
)

object TacticalPuzzleDatabase {
  val puzzles: List<TacticalPuzzle> = listOf(
    TacticalPuzzle(
      id = "puzzle_1",
      title = "Morphy's Opera Mate",
      category = "Checkmate in 2",
      rating = 1100,
      fen = "4kb1r/p2n1ppp/4q3/4p1B1/4P3/1Q6/PPP2PPP/2KR4 w k - 0 1",
      playerColor = PieceColor.WHITE,
      targetMovesUci = listOf("b3b8", "d7b8", "d1d8"),
      description = "Sacrifice the Queen on b8 to deliver an unforgettable back-rank checkmate with the rook!",
      hint = "Look for a forcing queen sacrifice on the back rank."
    ),
    TacticalPuzzle(
      id = "puzzle_2",
      title = "Anastasia's Mate",
      category = "Checkmate in 2",
      rating = 1350,
      fen = "5r1k/1p3Npp/8/3N4/8/8/6PP/4R1K1 w - - 0 1",
      playerColor = PieceColor.WHITE,
      targetMovesUci = listOf("e1e8", "f8e8"),
      description = "Punish the pinned defender and exploit the king's trapped position.",
      hint = "Attack the defender of the back rank."
    ),
    TacticalPuzzle(
      id = "puzzle_3",
      title = "The Royal Knight Fork",
      category = "Tactical Fork",
      rating = 1200,
      fen = "r1bqk2r/pppp1ppp/2n5/4p3/1b2P3/2NP1N2/PPP2PPP/R1BQKB1R w KQkq - 0 1",
      playerColor = PieceColor.WHITE,
      targetMovesUci = listOf("c1d2"),
      description = "Neutralize the pin and maintain harmonious piece coordination.",
      hint = "Block the bishop check safely."
    ),
    TacticalPuzzle(
      id = "puzzle_4",
      title = "Smothered Mate Pattern",
      category = "Master Tactics",
      rating = 1500,
      fen = "6k1/5Npp/8/8/8/8/1Q4PP/6K1 w - - 0 1",
      playerColor = PieceColor.WHITE,
      targetMovesUci = listOf("b2b3"),
      description = "Position the queen to create lethal discovered checks with the knight.",
      hint = "Align your queen on the same diagonal/file."
    ),
    TacticalPuzzle(
      id = "puzzle_5",
      title = "Back-Rank Deflection",
      category = "Deflection & Mate",
      rating = 1400,
      fen = "2r3k1/5ppp/8/8/8/8/1Q3PPP/2R3K1 w - - 0 1",
      playerColor = PieceColor.WHITE,
      targetMovesUci = listOf("c1c8"),
      description = "Capture the undefended rook and force immediate mate.",
      hint = "Eliminate the last line of defense on the c-file."
    )
  )
}
