package com.example.chess.engine

data class ChessOpening(
  val eco: String,
  val name: String,
  val variation: String? = null,
  val movesUci: List<String>
) {
  val fullName: String
    get() = if (variation != null) "$name: $variation" else name
}

object OpeningBook {
  val openings: List<ChessOpening> = listOf(
    // 1. e4 Openings
    ChessOpening("C50", "Italian Game", "Giuoco Piano", listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1c4", "f8c5")),
    ChessOpening("C51", "Italian Game", "Evans Gambit", listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1c4", "f8c5", "b2b4")),
    ChessOpening("C55", "Italian Game", "Two Knights Defense", listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1c4", "g8f6")),
    ChessOpening("C60", "Ruy Lopez", "Spanish Opening", listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1b5")),
    ChessOpening("C65", "Ruy Lopez", "Berlin Defense", listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1b5", "g8f6")),
    ChessOpening("C88", "Ruy Lopez", "Closed Main Line", listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1b5", "a7a6", "b5a4", "g8f6", "e1g1", "f8e7")),
    ChessOpening("C42", "Petrov's Defense", "Russian Game", listOf("e2e4", "e7e5", "g1f3", "g8f6")),
    ChessOpening("C45", "Scotch Game", "Classical Variation", listOf("e2e4", "e7e5", "g1f3", "b8c6", "d2d4", "e5d4", "f3d4")),
    ChessOpening("C44", "King's Knight Opening", null, listOf("e2e4", "e7e5", "g1f3")),
    ChessOpening("C20", "King's Pawn Game", null, listOf("e2e4", "e7e5")),
    
    // Sicilian
    ChessOpening("B90", "Sicilian Defense", "Najdorf Variation", listOf("e2e4", "c7c5", "g1f3", "d7d6", "d2d4", "c5d4", "f3d4", "g8f6", "b1c3", "a7a6")),
    ChessOpening("B70", "Sicilian Defense", "Dragon Variation", listOf("e2e4", "c7c5", "g1f3", "d7d6", "d2d4", "c5d4", "f3d4", "g8f6", "b1c3", "g7g6")),
    ChessOpening("B33", "Sicilian Defense", "Sveshnikov Variation", listOf("e2e4", "c7c5", "g1f3", "b8c6", "d2d4", "c5d4", "f3d4", "g8f6", "b1c3", "e7e5")),
    ChessOpening("B22", "Sicilian Defense", "Alapin Variation", listOf("e2e4", "c7c5", "c2c3")),
    ChessOpening("B20", "Sicilian Defense", null, listOf("e2e4", "c7c5")),

    // French & Caro-Kann
    ChessOpening("C00", "French Defense", null, listOf("e2e4", "e7e6")),
    ChessOpening("C02", "French Defense", "Advance Variation", listOf("e2e4", "e7e6", "d2d4", "d7d5", "e4e5")),
    ChessOpening("C10", "French Defense", "Paulsen / Rubinstein", listOf("e2e4", "e7e6", "d2d4", "d7d5", "b1c3")),
    ChessOpening("B10", "Caro-Kann Defense", null, listOf("e2e4", "c7c6")),
    ChessOpening("B12", "Caro-Kann Defense", "Advance Variation", listOf("e2e4", "c7c6", "d2d4", "d7d5", "e4e5")),
    ChessOpening("B18", "Caro-Kann Defense", "Classical Variation", listOf("e2e4", "c7c6", "d2d4", "d7d5", "b1c3", "d5e4", "c3e4", "c8f5")),

    // Other 1. e4
    ChessOpening("B01", "Scandinavian Defense", null, listOf("e2e4", "d7d5")),
    ChessOpening("B07", "Pirc Defense", null, listOf("e2e4", "d7d6", "d2d4", "g8f6", "b1c3", "g7g6")),
    ChessOpening("C21", "Danish Gambit", null, listOf("e2e4", "e7e5", "d2d4", "e5d4", "c2c3")),
    ChessOpening("C25", "Vienna Game", null, listOf("e2e4", "e7e5", "b1c3")),
    ChessOpening("C30", "King's Gambit", null, listOf("e2e4", "e7e5", "f2f4")),

    // 1. d4 Openings
    ChessOpening("D30", "Queen's Gambit Declined", null, listOf("d2d4", "d7d5", "c2c4", "e7e6")),
    ChessOpening("D35", "Queen's Gambit Declined", "Exchange Variation", listOf("d2d4", "d7d5", "c2c4", "e7e6", "b1c3", "g8f6", "c4d5")),
    ChessOpening("D20", "Queen's Gambit Accepted", null, listOf("d2d4", "d7d5", "c2c4", "d5c4")),
    ChessOpening("D10", "Slav Defense", null, listOf("d2d4", "d7d5", "c2c4", "c7c6")),
    ChessOpening("D02", "London System", null, listOf("d2d4", "d7d5", "g1f3", "g8f6", "c1f4")),
    ChessOpening("D00", "Queen's Pawn Game", null, listOf("d2d4", "d7d5")),
    ChessOpening("E60", "King's Indian Defense", null, listOf("d2d4", "g8f6", "c2c4", "g7g6")),
    ChessOpening("E20", "Nimzo-Indian Defense", null, listOf("d2d4", "g8f6", "c2c4", "e7e6", "b1c3", "f8b4")),
    ChessOpening("E12", "Queen's Indian Defense", null, listOf("d2d4", "g8f6", "c2c4", "e7e6", "g1f3", "b7b6")),
    ChessOpening("A57", "Benko Gambit", null, listOf("d2d4", "g8f6", "c2c4", "c7c5", "d4d5", "b7b5")),
    ChessOpening("A80", "Dutch Defense", null, listOf("d2d4", "f7f5")),
    ChessOpening("A40", "Queen's Pawn Opening", null, listOf("d2d4")),

    // Flank Openings
    ChessOpening("A10", "English Opening", null, listOf("c2c4")),
    ChessOpening("A04", "Réti Opening", null, listOf("g1f3")),
    ChessOpening("A02", "Bird's Opening", null, listOf("f2f4")),
    ChessOpening("A00", "Nimzo-Larsen Attack", null, listOf("b2b3"))
  )

  fun detectOpening(uciMoves: List<String>): ChessOpening? {
    if (uciMoves.isEmpty()) return null

    var bestMatch: ChessOpening? = null
    var maxMatchLength = 0

    for (op in openings) {
      if (uciMoves.size >= op.movesUci.size) {
        val matches = op.movesUci.indices.all { i -> uciMoves[i] == op.movesUci[i] }
        if (matches && op.movesUci.size > maxMatchLength) {
          bestMatch = op
          maxMatchLength = op.movesUci.size
        }
      }
    }

    return bestMatch
  }
}
