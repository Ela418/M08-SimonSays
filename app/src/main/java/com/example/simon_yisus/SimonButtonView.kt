package com.example.simon_yisus

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.cos
import kotlin.math.sin

interface HexagonClickListener {
    fun onHexagonClick(color: String)
}

interface SimonGameCallback {
    fun onSequenceCompleted()
    fun onPlayerTurnComplete(success: Boolean)
    fun onGameOver()
}


class SimonButtonView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    companion object {
        private const val FLASH_DURATION = 300L // Duration of the flash animation in milliseconds
    }

    private val hexagons = mutableListOf<Hexagon>()
    private val touchAreas = mutableListOf<TouchArea>()
    private val colors = listOf("blue", "red", "yellow", "green", "orange", "purple")
    private var hexagonClickListener: HexagonClickListener? = null
    private var distanceToOuter = 0f
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f // Adjust outline width as needed
        color = Color.BLACK // Outline color for outer hexagons
    }

    init {
        // Initialize paints and other properties
        buttonPaint.color = Color.BLACK
        outlinePaint.color = Color.BLACK
        distanceToOuter = width / 5f // Set default distance
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Ensure proper initialization of hexagons list
        if (hexagons.isEmpty()) {
            initializeHexagons(width, height)
        }

        // Draw center hexagon
        val centerX = width / 2f
        val centerY = height / 2f
        val centerRadius = width / 6f
        val outerRadius = width / 6f
        canvas.drawHexagon(centerX, centerY, centerRadius, buttonPaint)
        canvas.drawHexagonOutline(centerX, centerY, centerRadius, outlinePaint)

        // Set the color for the center hexagon
        buttonPaint.color = Color.BLACK

        // Draw surrounding hexagons
        setDistanceToOuter(outerRadius * 2)

        // Calculate the angle step for each hexagon
        val angleStep = 2 * Math.PI / 6

        for (i in colors.indices) {
            val angle = i * Math.PI / 3
            val x = centerX + distanceToOuter * Math.cos(angle).toFloat()
            val y = centerY + distanceToOuter * Math.sin(angle).toFloat()

            // Draw the hexagon
            buttonPaint.color = getColor(colors[i])
            canvas.drawHexagon(x, y, outerRadius, buttonPaint)
            canvas.drawHexagonOutline(x, y, outerRadius, outlinePaint)

        }
    }

    private fun setDistanceToOuter(fl: Float) {
        distanceToOuter = fl
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Update the hexagon positions when the view size changes
        initializeHexagons(w, h)
    }

    private fun initializeHexagons(viewWidth: Int, viewHeight: Int) {
        hexagons.clear()
        touchAreas.clear()

        val centerX = viewWidth / 2f
        val centerY = viewHeight / 2f
        val radius = viewWidth / 6f

        // Draw center black hexagon
        val centerHexagon = Hexagon(centerX, centerY, "black")
        hexagons.add(centerHexagon)

        val x = centerX + distanceToOuter
        val y = centerY + distanceToOuter
        val color = "black"

        val touchArea = TouchArea(
            x - radius, // Left
            y - radius, // Top
            x + radius, // Right
            y + radius, // Bottom
           color
        )
        touchAreas.add(touchArea)


        setDistanceToOuter(radius * 2)
        // Draw surrounding hexagons
        val angleStep = 2 * Math.PI / 6
        for (i in colors.indices) {
            val angle = i * Math.PI / 3
            val x = centerX + distanceToOuter * cos(angle).toFloat()
            val y = centerY + distanceToOuter * sin(angle).toFloat()
            val color = colors[i]
            val hexagon = Hexagon(x, y, color)
            hexagons.add(hexagon)

            // Add listener to outer hexagons
            // Calculate the touch area for the hexagon
            val touchRadius = viewWidth / 6f // Adjust touch radius as needed
            val touchArea = TouchArea(
                x - touchRadius, // Left
                y - touchRadius, // Top
                x + touchRadius, // Right
                y + touchRadius, // Bottom
                color
            )
            Log.d("SimonButtonView", "$touchArea")
            touchAreas.add(touchArea)

        }
        invalidate() // Redraw the view
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val touchX = event.x
            val touchY = event.y

            // Find which hexagon was touched among the surrounding ones
            for (touchArea in touchAreas.subList(
                1,
                touchAreas.size
            )) { // Exclude the center hexagon
                if (touchX >= touchArea.left && touchX <= touchArea.right &&
                    touchY >= touchArea.top && touchY <= touchArea.bottom
                ) {
                    hexagonClickListener?.onHexagonClick(touchArea.color)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    fun setHexagonClickListener(listener: HexagonClickListener) {
        this.hexagonClickListener = listener
    }

    private fun getColor(colorName: String): Int {
        return when (colorName) {
            "blue" -> Color.BLUE
            "red" -> Color.RED
            "yellow" -> Color.YELLOW
            "green" -> Color.GREEN
            "orange" -> Color.rgb(255, 165, 0)
            "purple" -> Color.rgb(128, 0, 128)
            else -> Color.BLACK
        }
    }

    private fun Canvas.drawHexagon(
        centerX: Float,
        centerY: Float,
        radius: Float,
        paint: Paint
    ) {
        val path = Path()
        val angleStep = 2 * Math.PI / 6
        for (i in 0 until 6) {
            val x = centerX + radius * cos(i * angleStep).toFloat()
            val y = centerY + radius * sin(i * angleStep).toFloat()
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        drawPath(path, paint)
    }

    private fun Canvas.drawHexagonOutline(
        centerX: Float,
        centerY: Float,
        radius: Float,
        paint: Paint
    ) {
        val path = Path()
        paint.color = Color.BLACK
        val angleStep = 2 * Math.PI / 6
        for (i in 0 until 6) {
            val x = centerX + radius * cos(i * angleStep).toFloat()
            val y = centerY + radius * sin(i * angleStep).toFloat()
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        drawPath(path, paint)
    }

    fun flashButton(color: String) {
        val originalColor = getColor(color)
        val flashColor = Color.WHITE

        // Animate the color change
        val animator = ValueAnimator.ofArgb(originalColor, flashColor)
        animator.addUpdateListener { valueAnimator ->
            buttonPaint.color = valueAnimator.animatedValue as Int
            invalidate()
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = FLASH_DURATION
        animator.repeatMode = ValueAnimator.REVERSE
        animator.repeatCount = 1
        animator.start()
    }

    private inner class Hexagon(
        var centerX: Float,
        var centerY: Float,
        val color: String
    )

    private inner class TouchArea(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float,
        val color: String
    )
}