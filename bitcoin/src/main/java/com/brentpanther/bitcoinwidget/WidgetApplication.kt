package com.brentpanther.bitcoinwidget

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import com.brentpanther.bitcoinwidget.receiver.WidgetBroadcastReceiver

class WidgetApplication : Application() {

    fun <T : WidgetProvider> getWidgetIds(className: Class<T>): IntArray {
        val name = ComponentName(this, className)
        return AppWidgetManager.getInstance(this).getAppWidgetIds(name)
    }
    val widgetIds: IntArray
        get() {
            return widgetProviders.map { getWidgetIds(it).toList() }.flatten().toIntArray()
        }

    val widgetProviders = listOf(WidgetProvider::class.java, ValueWidgetProvider::class.java, WidgetProvider1x1::class.java, ValueWidgetProvider1x1::class.java)

    fun getWidgetType(widgetId: Int): WidgetType {
        val widgetInfo = AppWidgetManager.getInstance(this).getAppWidgetInfo(widgetId)
            ?: return WidgetType.PRICE
        return when (widgetInfo.provider.className) {
            WidgetProvider::class.qualifiedName -> WidgetType.PRICE
            ValueWidgetProvider::class.qualifiedName -> WidgetType.VALUE
            WidgetProvider1x1::class.qualifiedName -> WidgetType.PRICE
            ValueWidgetProvider1x1::class.qualifiedName -> WidgetType.VALUE
            else -> throw IllegalArgumentException()
        }
    }

    fun isWidget1x1(widgetId: Int): Boolean {
        val widgetInfo = AppWidgetManager.getInstance(this).getAppWidgetInfo(widgetId)
            ?: return false
        return when (widgetInfo.provider.className) {
            WidgetProvider1x1::class.qualifiedName -> true
            ValueWidgetProvider1x1::class.qualifiedName -> true
            else -> false
        }
    }

    fun getActualWidgetSize(widgetId: Int): Pair<Int, Int> {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val options = appWidgetManager.getAppWidgetOptions(widgetId)
        val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
        val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0)
        return Pair(width, height)
    }

    fun isWidgetSquare(widgetId: Int): Boolean {
        val (width, height) = getActualWidgetSize(widgetId)
        if (width == 0 || height == 0) {
            return isWidget1x1(widgetId)
        }
        val ratio = width.toFloat() / height.toFloat()
        return ratio in 0.8f..1.2f
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        registerReceiver(WidgetBroadcastReceiver(), IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED))
        WidgetUpdater.updateDisplays(this)
    }

    companion object {

        lateinit var instance: WidgetApplication
            private set

        fun Int.dpToPx() = this * Resources.getSystem().displayMetrics.density
    }

}