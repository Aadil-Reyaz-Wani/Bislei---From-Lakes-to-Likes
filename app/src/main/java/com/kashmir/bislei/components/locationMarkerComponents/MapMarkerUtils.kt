package com.kashmir.bislei.components.locationMarkerComponents

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun createEnhancedFishingMarker(
    context: Context,
    hotspotCount: Int,
    primaryColor: Int,
    surfaceColor: Int
): BitmapDescriptor {
    return try {
        // Marker dimensions - larger for better visibility
        val markerWidth = 120
        val markerHeight = 160

        val bitmap = Bitmap.createBitmap(markerWidth, markerHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw main marker pin
        drawModernMarkerPin(canvas, markerWidth, markerHeight, primaryColor, surfaceColor)

        // Draw fishing icon inside the pin
        drawFishingIcon(canvas, markerWidth, markerHeight, surfaceColor)

        // Add hotspot badge if count > 0
        if (hotspotCount > 0) {
            drawHotspotBadge(canvas, markerWidth, markerHeight, hotspotCount)
        }

        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback to default marker
        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
    }
}

private fun drawModernMarkerPin(
    canvas: Canvas,
    width: Int,
    height: Int,
    primaryColor: Int,
    surfaceColor: Int
) {
    val pinWidth = width * 0.8f
    val pinHeight = height * 0.75f
    val centerX = width / 2f
    val topY = height * 0.1f

    // Shadow paint
    val shadowPaint = Paint().apply {
        color = Color.argb(60, 0, 0, 0)
        style = Paint.Style.FILL
        isAntiAlias = true
        maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
    }

    // Main pin paint
    val pinPaint = Paint().apply {
        color = primaryColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Border paint
    val borderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    // Create pin path (teardrop shape)
    val pinPath = Path().apply {
        val radius = pinWidth / 2f

        // Circle part
        addCircle(centerX, topY + radius, radius, Path.Direction.CW)

        // Triangle part (pointing down)
        moveTo(centerX - radius * 0.3f, topY + radius * 1.7f)
        lineTo(centerX, height * 0.9f)
        lineTo(centerX + radius * 0.3f, topY + radius * 1.7f)
        close()
    }

    // Draw shadow (slightly offset)
    canvas.save()
    canvas.translate(4f, 4f)
    canvas.drawPath(pinPath, shadowPaint)
    canvas.restore()

    // Draw main pin
    canvas.drawPath(pinPath, pinPaint)
    canvas.drawPath(pinPath, borderPaint)
}

/**
 * Draws a fishing-themed icon inside the marker
 */
private fun drawFishingIcon(canvas: Canvas, width: Int, height: Int, surfaceColor: Int) {
    val centerX = width / 2f
    val centerY = height * 0.25f
    val iconSize = width * 0.25f

    val iconPaint = Paint().apply {
        color = surfaceColor
        style = Paint.Style.FILL
        isAntiAlias = true
        strokeWidth = 4f
    }

    // Draw simple fish shape
    val fishPath = Path().apply {
        // Fish body (oval)
        addOval(
            centerX - iconSize * 0.6f,
            centerY - iconSize * 0.3f,
            centerX + iconSize * 0.3f,
            centerY + iconSize * 0.3f,
            Path.Direction.CW
        )

        // Fish tail
        moveTo(centerX + iconSize * 0.3f, centerY)
        lineTo(centerX + iconSize * 0.7f, centerY - iconSize * 0.2f)
        lineTo(centerX + iconSize * 0.7f, centerY + iconSize * 0.2f)
        close()
    }

    canvas.drawPath(fishPath, iconPaint)

    // Draw fish eye
    canvas.drawCircle(
        centerX - iconSize * 0.2f,
        centerY - iconSize * 0.1f,
        iconSize * 0.08f,
        Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }
    )
}

/**
 * Draws a modern hotspot badge with count
 */
private fun drawHotspotBadge(canvas: Canvas, width: Int, height: Int, count: Int) {
    val badgeRadius = width * 0.18f
    val badgeX = width * 0.75f
    val badgeY = height * 0.15f

    // Badge background with gradient effect
    val badgePaint = Paint().apply {
        shader = RadialGradient(
            badgeX, badgeY, badgeRadius,
            intArrayOf(Color.rgb(255, 87, 87), Color.rgb(220, 53, 69)),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Badge border
    val badgeBorderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    // Draw badge circle
    canvas.drawCircle(badgeX, badgeY, badgeRadius, badgePaint)
    canvas.drawCircle(badgeX, badgeY, badgeRadius, badgeBorderPaint)

    // Badge text
    val textSize = if (count < 10) badgeRadius * 0.8f else badgeRadius * 0.65f
    val textPaint = Paint().apply {
        color = Color.WHITE
        this.textSize = textSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        setShadowLayer(2f, 1f, 1f, Color.argb(100, 0, 0, 0))
    }

    val textY = badgeY + (textPaint.descent() - textPaint.ascent()) / 2 - textPaint.descent()
    canvas.drawText(count.toString(), badgeX, textY, textPaint)
}

/**
 * Legacy function for backward compatibility
 * @deprecated Use createEnhancedFishingMarker instead
 */
@Deprecated("Use createEnhancedFishingMarker for better design")
fun createHotspotMarker(context: Context, baseIconRes: Int, hotspotCount: Int): BitmapDescriptor {
    return try {
        val baseIcon = ContextCompat.getDrawable(context, baseIconRes)
            ?: throw IllegalArgumentException("Resource not found: $baseIconRes")

        val width = baseIcon.intrinsicWidth
        val height = baseIcon.intrinsicHeight
        baseIcon.setBounds(0, 0, width, height)

        val scaledWidth = (width * 1.5f).toInt().coerceAtLeast(100)
        val scaledHeight = (height * 1.5f).toInt().coerceAtLeast(150)

        val bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.save()
        val scaleFactorX = scaledWidth.toFloat() / width
        val scaleFactorY = scaledHeight.toFloat() / height
        canvas.scale(scaleFactorX, scaleFactorY)
        baseIcon.draw(canvas)
        canvas.restore()

        if (hotspotCount > 0) {
            val badgeRadius = scaledWidth * 0.15f
            val badgeX = scaledWidth * 0.65f
            val badgeY = scaledHeight * 0.15f

            val badgePaint = Paint().apply {
                color = android.graphics.Color.RED
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(badgeX, badgeY, badgeRadius, badgePaint)

            val textSize = if (hotspotCount < 10) {
                badgeRadius * 1.2f
            } else {
                badgeRadius
            }

            val textPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                this.textSize = textSize
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

            val textY = badgeY + (textPaint.descent() - textPaint.ascent()) / 2 - textPaint.descent()
            canvas.drawText(hotspotCount.toString(), badgeX, textY, textPaint)
        }

        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        e.printStackTrace()
        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
    }
}