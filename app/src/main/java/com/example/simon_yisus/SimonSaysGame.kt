package com.example.simon_yisus.SimonSaysGame

import android.media.SoundPool
import android.os.Handler
import com.example.simon_yisus.SimonButtonView
import com.example.simon_yisus.SimonGameCallback

class SimonSaysGame(
    private val gameCallback: SimonGameCallback,
    private val soundPool: SoundPool,
    private val soundMap: Map<String, Int>
) {

    companion object {
        private const val FLASH_DURATION = 450L // Duration of the flash animation in milliseconds
        private const val DELAY_BETWEEN_COLORS =
            1000L // Delay between playing each color in the sequence
    }

    private lateinit var simonButtonView: SimonButtonView
    private val colors = listOf("blue", "red", "yellow", "green", "orange", "purple")
    private val sequence = mutableListOf<String>()
    private val playerSequence = mutableListOf<String>()

    fun setSimonButtonView(view: SimonButtonView) {
        simonButtonView = view
    }

    fun startNewGame() {
        sequence.clear()
        playerSequence.clear()
        addToSequence()
        playSequence()
    }

    fun addToSequence() {
        val nextColor = colors.random()
        sequence.add(nextColor)
        playerSequence.clear()
    }

    fun playSequence() {
        sequence.forEachIndexed { index, color ->
            Handler().postDelayed({
                simonButtonView.flashButton(color)
                playSound(color)
                if (index == sequence.lastIndex) {
                    // Notify the callback when the sequence playback is complete
                    Handler().postDelayed({
                        gameCallback.onSequenceCompleted()
                    }, DELAY_BETWEEN_COLORS)
                }
            }, index * DELAY_BETWEEN_COLORS)
        }
    }

    fun isPlayerSequenceComplete(): Boolean {
        return playerSequence.size == sequence.size
    }

    fun checkPlayerSequence(): Boolean {
        return sequence == playerSequence
    }

    fun checkColor(color: String) {
        playerSequence.add(color)
        if(sequence.size == playerSequence.size) {
            gameCallback.onPlayerTurnComplete(checkPlayerSequence())
        }
    }

    private fun playSound(color: String) {
        val soundId = soundMap[color]
        soundId?.let {
            soundPool.play(it, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }
}