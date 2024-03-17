package com.example.simon_yisus

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.simon_yisus.SimonSaysGame.SimonSaysGame

class MainActivity : AppCompatActivity(), HexagonClickListener, SimonGameCallback {
    private lateinit var soundPool: SoundPool
    private lateinit var soundMap: Map<String, Int>
    private lateinit var simonButtonView: SimonButtonView
    private lateinit var simonSaysGame: SimonSaysGame
    private var bestScore = 0
    private var actualScore = 0
    private lateinit var bestScoreTextView: TextView
    private lateinit var actualScoreTextView: TextView

    private var playerInputSequence = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize SoundPool
        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        // Load sound resources
        loadSounds()

        // Initialize other components
        simonButtonView = findViewById(R.id.simonButtonView)
        simonButtonView.setHexagonClickListener(this)

        simonSaysGame = SimonSaysGame(this, soundPool, soundMap)

        bestScoreTextView = findViewById(R.id.bestScoreTextView)
        actualScoreTextView = findViewById(R.id.actualScoreTextView)

        loadBestScore()
        updateScoreViews()

        simonSaysGame.setSimonButtonView(simonButtonView)
        simonSaysGame.startNewGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }

    override fun onHexagonClick(color: String) {
        Log.d("SimonButtonView", "Button clicked: $color")
        playerInputSequence.add(color) // Add color to player input sequence
        simonSaysGame.checkColor(color)
        playSound(color)
        if (simonSaysGame.isPlayerSequenceComplete()) {
            // If player sequence is complete, check against generated sequence
            val success = simonSaysGame.checkPlayerSequence()
            if (success) {
                // Player sequence is correct, start a new round
                simonSaysGame.addToSequence()
                simonSaysGame.playSequence()
            } else {
                // Player sequence is incorrect, end the game
                endGame()
            }
        }
    }

    override fun onSequenceCompleted() {
    }

    override fun onPlayerTurnComplete(success: Boolean) {
        if (!success) {
            endGame()
        } else {
            actualScore++
            updateScoreViews()
            playerInputSequence.clear()
        }
    }

    override fun onGameOver() {
        endGame() // Handle game over event
    }

    private fun updateScoreViews() {
        bestScoreTextView.text = "Best Score: $bestScore"
        actualScoreTextView.text = "Actual Score: $actualScore"
    }

    private fun loadSounds() {
        soundMap = mapOf(
            "blue" to soundPool.load(this, R.raw.blue_sound, 1),
            "red" to soundPool.load(this, R.raw.red_sound, 1),
            "yellow" to soundPool.load(this, R.raw.yellow_sound, 1),
            "green" to soundPool.load(this, R.raw.green_sound, 1),
            "orange" to soundPool.load(this, R.raw.orange_sound, 1),
            "purple" to soundPool.load(this, R.raw.purple_sound, 1)
        )
    }

    private fun loadBestScore() {
        // Load best score from SharedPreferences or other storage
        bestScore = 0 // Example value
    }

    private fun saveBestScore() {
        // Save best score to SharedPreferences or other storage
        bestScore = actualScore.coerceAtLeast(bestScore)
    }

    private fun endGame() {
        saveBestScore()
        Toast.makeText(this, "Game Over!", Toast.LENGTH_SHORT).show()
        actualScore = 0
        updateScoreViews()
        simonSaysGame.startNewGame()
    }

    private fun playSound(color: String) {
        val soundId = soundMap[color]
        soundId?.let {
            soundPool.play(it, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }

}