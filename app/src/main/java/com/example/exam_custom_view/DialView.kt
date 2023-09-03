package com.example.exam_custom_view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val RADIUS_OFFSET_LABEL = 30
private const val RADIUS_OFFSET_INDICATOR = -35

private enum class FanSpeed(val label: Int) {
    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.fan_medium),
    HIGH(R.string.fan_high);

    fun next() = when(this) {
       OFF -> LOW
       LOW -> MEDIUM
       MEDIUM -> HIGH
       HIGH -> OFF
    }
}

class DialView @JvmOverloads constructor(context: Context,
                                         attrs: AttributeSet,
                                         defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {

    private var radius = 0.0f
    private var fanSpeed = FanSpeed.OFF
    private val pointPosition: PointF = PointF(0.0f, 0.0f)

    private var fanSpeedColorLow = 0
    private var fanSpeedColorMedium = 0
    private var fanSpeedColorHigh = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.DialView) {
         fanSpeedColorLow = getColor(R.styleable.DialView_dv_fan_color_1, 0)
         fanSpeedColorMedium = getColor(R.styleable.DialView_dv_fan_color_2, 0)
         fanSpeedColorHigh = getColor(R.styleable.DialView_dv_fan_color_3, 0)
        }

        updateContentDescription()

        isClickable = true

        ViewCompat.setAccessibilityDelegate(this, object: AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                val customClick = AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                    AccessibilityNodeInfoCompat.ACTION_CLICK,
                    if (fanSpeed == FanSpeed.HIGH) "Reset"
                    else "Change"
                )
                info.addAction(customClick)
            }

        })
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        radius = (min(width, height)/2.0 * 0.8).toFloat()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color = when (fanSpeed) {
            FanSpeed.OFF -> Color.GRAY
            FanSpeed.LOW -> fanSpeedColorLow
            FanSpeed.MEDIUM -> fanSpeedColorMedium
            FanSpeed.HIGH -> fanSpeedColorHigh
        }
        // Draw the dial.
        canvas?.drawCircle((width/2).toFloat(), (height/2).toFloat(), radius, paint)

        // Draw the indicator circle.
        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYForSpeed(fanSpeed, markerRadius)
        paint.color = Color.BLACK
        canvas?.drawCircle(pointPosition.x, pointPosition.y, radius/12, paint)

        // Draw the text labels.
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (i in FanSpeed.values()) {
           pointPosition.computeXYForSpeed(i, labelRadius)
           val text = resources.getString(i.label)
           canvas?.drawText(text, pointPosition.x, pointPosition.y, paint)
        }
    }

    private fun PointF.computeXYForSpeed(pos: FanSpeed, radius: Float) {
        val startAngle = Math.PI * (9/8.0)
        val angle = startAngle + pos.ordinal * (Math.PI/4)
        x = (radius * cos(angle)).toFloat() + width/2
        y = (radius * sin(angle)).toFloat() + height/2
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true

        fanSpeed = fanSpeed.next()
        contentDescription = resources.getString(fanSpeed.label)
        updateContentDescription()
        invalidate()
        return true
    }

    private fun updateContentDescription() {
        contentDescription = resources.getString(fanSpeed.label)
    }
}