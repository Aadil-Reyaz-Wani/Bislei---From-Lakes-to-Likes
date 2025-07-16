package com.kashmir.bislei.components.locationMarkerComponents

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

/**
 * Creates a custom marker with a badge showing the number of active fishers.
 * This function creates a bitmap with the fishing marker and a red badge with count.
 *
 * @param context Android context to access resources
 * @param baseIconRes Resource ID for the base marker icon (should be a pin shape marker)
 * @param hotspotCount Number to display in the badge (if > 0)
 * @return BitmapDescriptor that can be used as a map marker icon
 */
fun createHotspotMarker(context: Context, @DrawableRes baseIconRes: Int, hotspotCount: Int): BitmapDescriptor {
    try {
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
            // Smaller badge
            val badgeRadius = scaledWidth * 0.15f  // reduced from 0.2f
            val badgeX = scaledWidth * 0.65f       // adjusted position
            val badgeY = scaledHeight * 0.15f      // adjusted position

            val badgePaint = Paint().apply {
                color = android.graphics.Color.RED
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(badgeX, badgeY, badgeRadius, badgePaint)

            val textSize = if (hotspotCount < 10) {
                badgeRadius * 1.2f
            } else {
                badgeRadius     // smaller font for double digits
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

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        e.printStackTrace()
        return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
    }
}
