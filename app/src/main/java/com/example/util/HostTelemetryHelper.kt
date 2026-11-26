package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class HostMemoryTelemetry(
    val totalRamMb: Long,
    val availableRamMb: Long,
    val usedPercent: Int
)

data class InstalledHostApp(
    val packageName: String,
    val label: String,
    val icon: Drawable?
)

object HostTelemetryHelper {
    fun readMemoryTelemetry(context: Context): HostMemoryTelemetry {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(memInfo)
        val totalMb = (memInfo.totalMem / (1024L * 1024L)).coerceAtLeast(1L)
        val availMb = (memInfo.availMem / (1024L * 1024L)).coerceAtLeast(0L)
        val usedPct = (((totalMb - availMb).toFloat() / totalMb.toFloat()) * 100f)
            .toInt()
            .coerceIn(0, 100)
        return HostMemoryTelemetry(
            totalRamMb = totalMb,
            availableRamMb = availMb,
            usedPercent = usedPct
        )
    }

    fun loadLaunchableHostApps(context: Context): List<InstalledHostApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        val ownPkg = context.packageName
        return resolved
            .asSequence()
            .filter { it.activityInfo.packageName != ownPkg }
            .distinctBy { it.activityInfo.packageName }
            .map { info ->
                InstalledHostApp(
                    packageName = info.activityInfo.packageName,
                    label = info.loadLabel(pm).toString(),
                    icon = runCatching { info.loadIcon(pm) }.getOrNull()
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun launchHostApp(context: Context, packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(launchIntent)
            true
        }.getOrDefault(false)
    }
}
