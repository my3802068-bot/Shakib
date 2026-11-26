package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity
import com.example.data.model.CloudPhoneEntity
import com.example.data.model.DeviceBrand

object HomeShortcutHelper {
    const val EXTRA_PHONE_ID = "extra_cloud_phone_id"
    const val EXTRA_OPEN_MINI_DESKTOP = "extra_open_mini_desktop"
    const val EXTRA_APP_ID = "extra_virtual_app_id"
    const val EXTRA_APP_URL = "extra_virtual_app_url"

    fun pinCloudPhoneShortcut(
        context: Context,
        phone: CloudPhoneEntity,
        openAsMiniDesktop: Boolean = true,
        appId: String? = null,
        appLabel: String? = null,
        appUrl: String? = null
    ): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            return false
        }

        val brand = DeviceBrand.fromId(phone.brand)
        val shortcutId = buildString {
            append("cloud_phone_${phone.id}")
            if (openAsMiniDesktop) append("_mini")
            if (!appId.isNullOrBlank()) append("_$appId")
            if (!appUrl.isNullOrBlank()) append("_${appUrl.hashCode()}")
        }

        val label = when {
            !appLabel.isNullOrBlank() -> "$appLabel • ${brand.displayName}"
            openAsMiniDesktop -> "${phone.name} (Mini)"
            else -> phone.name
        }.take(24)

        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_PHONE_ID, phone.id)
            putExtra(EXTRA_OPEN_MINI_DESKTOP, openAsMiniDesktop)
            if (!appId.isNullOrBlank()) putExtra(EXTRA_APP_ID, appId)
            if (!appUrl.isNullOrBlank()) putExtra(EXTRA_APP_URL, appUrl)
        }

        val badgeBitmap = renderShortcutBadge(
            brand = brand,
            tagText = if (openAsMiniDesktop) "MINI" else "14 OS",
            titleChar = (appLabel ?: phone.name).firstOrNull()?.uppercaseChar()?.toString() ?: "C"
        )

        val shortcutInfo = ShortcutInfoCompat.Builder(context, shortcutId)
            .setShortLabel(label)
            .setLongLabel("${phone.name} • ${phone.modelName} (${phone.androidVersion})")
            .setIcon(IconCompat.createWithBitmap(badgeBitmap))
            .setIntent(intent)
            .build()

        return ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }

    private fun renderShortcutBadge(
        brand: DeviceBrand,
        tagText: String,
        titleChar: String
    ): Bitmap {
        val size = 192
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val primaryInt = if (brand == DeviceBrand.INFINIX) 0xFF00E676.toInt() else 0xFFFFC400.toInt()
        val secondaryInt = if (brand == DeviceBrand.INFINIX) 0xFF00E5FF.toInt() else 0xFFFF6E40.toInt()

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, size.toFloat(), size.toFloat(),
                0xFF0B1021.toInt(), 0xFF17213B.toInt(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(RectF(8f, 8f, size - 8f, size - 8f), 44f, 44f, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = primaryInt
        }
        canvas.drawRoundRect(RectF(12f, 12f, size - 12f, size - 12f), 40f, 40f, borderPaint)

        // Mini phone silhouette
        val phoneRect = RectF(48f, 34f, size - 48f, size - 44f)
        val phonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                phoneRect.left, phoneRect.top, phoneRect.right, phoneRect.bottom,
                primaryInt, secondaryInt, Shader.TileMode.CLAMP
            )
            alpha = 55
        }
        canvas.drawRoundRect(phoneRect, 18f, 18f, phonePaint)

        val phoneStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = secondaryInt
        }
        canvas.drawRoundRect(phoneRect, 18f, 18f, phoneStroke)

        val letterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 54f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(titleChar, size / 2f, size / 2f + 12f, letterPaint)

        val tagBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryInt
        }
        canvas.drawRoundRect(RectF(42f, size - 42f, size - 42f, size - 14f), 14f, 14f, tagBg)

        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF070B14.toInt()
            textSize = 18f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(tagText, size / 2f, size - 22f, tagPaint)

        return bitmap
    }
}
