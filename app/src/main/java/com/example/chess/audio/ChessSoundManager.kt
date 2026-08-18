package com.example.chess.audio

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ChessSoundManager {
  private val scope = CoroutineScope(Dispatchers.Default)
  var isSoundEnabled: Boolean = true

  @Volatile
  private var toneGen: ToneGenerator? = null
  private val lock = Any()

  private fun getToneGen(): ToneGenerator? {
    if (toneGen == null) {
      synchronized(lock) {
        if (toneGen == null) {
          try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 65)
          } catch (_: Throwable) {
            toneGen = null
          }
        }
      }
    }
    return toneGen
  }

  fun playMoveSound() {
    if (!isSoundEnabled) return
    scope.launch {
      try {
        getToneGen()?.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
      } catch (_: Throwable) {}
    }
  }

  fun playCaptureSound() {
    if (!isSoundEnabled) return
    scope.launch {
      try {
        getToneGen()?.startTone(ToneGenerator.TONE_PROP_ACK, 65)
      } catch (_: Throwable) {}
    }
  }

  fun playCheckSound() {
    if (!isSoundEnabled) return
    scope.launch {
      try {
        getToneGen()?.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
      } catch (_: Throwable) {}
    }
  }

  fun playCastleSound() {
    if (!isSoundEnabled) return
    scope.launch {
      try {
        getToneGen()?.startTone(ToneGenerator.TONE_PROP_BEEP, 60)
      } catch (_: Throwable) {}
    }
  }

  fun playVictorySound() {
    if (!isSoundEnabled) return
    scope.launch {
      try {
        getToneGen()?.startTone(ToneGenerator.TONE_SUP_CONFIRM, 250)
      } catch (_: Throwable) {}
    }
  }

  fun playGameOverSound() {
    if (!isSoundEnabled) return
    scope.launch {
      try {
        getToneGen()?.startTone(ToneGenerator.TONE_PROP_NACK, 180)
      } catch (_: Throwable) {}
    }
  }
}
