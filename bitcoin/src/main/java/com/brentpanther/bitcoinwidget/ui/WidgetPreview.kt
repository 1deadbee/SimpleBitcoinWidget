package com.brentpanther.bitcoinwidget.ui

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.TextViewCompat
import com.brentpanther.bitcoinwidget.R
import com.brentpanther.bitcoinwidget.db.Widget
import com.brentpanther.bitcoinwidget.strategy.display.WidgetDisplayStrategy
import com.brentpanther.bitcoinwidget.strategy.presenter.ComposePreviewWidgetPresenter

@Composable
fun WidgetPreview(widget: Widget, fixedSize: Boolean, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth()) {
        Box {
            val isSquare = com.brentpanther.bitcoinwidget.WidgetApplication.instance.isWidgetSquare(widget.widgetId)
            val (actualWidth, actualHeight) = com.brentpanther.bitcoinwidget.WidgetApplication.instance.getActualWidgetSize(widget.widgetId)
            val previewHeight: Int
            val previewWidth: Int
            if (actualWidth > 0 && actualHeight > 0) {
                val ratio = actualWidth.toFloat() / actualHeight.toFloat()
                val baseHeight = 80.dp
                previewHeight = R.dimen.widget_preview_height_1x1
                previewWidth = if (ratio >= 1.2f) {
                    R.dimen.widget_preview_width
                } else {
                    R.dimen.widget_preview_width_1x1
                }
            } else {
                previewHeight = if (isSquare) R.dimen.widget_preview_height_1x1 else R.dimen.widget_preview_height
                previewWidth = if (isSquare) R.dimen.widget_preview_width_1x1 else R.dimen.widget_preview_width
            }
            Image(
                painterResource(R.drawable.bg),
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(110.dp)
            )
            if (!LocalInspectionMode.current) {
                AndroidView(
                    factory = { context ->
                        LayoutInflater.from(context).inflate(R.layout.layout_widget_preview, null)
                    },
                    update = {
                        val widgetPresenter = ComposePreviewWidgetPresenter(widget, it)
                        val strategy = WidgetDisplayStrategy.getStrategy(it.context, widget, widgetPresenter)
                        strategy.refresh()
                        it.findViewById<View>(R.id.parent).isClickable = false
                        if (!fixedSize) {
                            val price = it.findViewById<TextView>(R.id.price)
                            TextViewCompat.setAutoSizeTextTypeWithDefaults(
                                price, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .height(dimensionResource(previewHeight))
                        .width(dimensionResource(previewWidth))
                )
            }
        }
    }

}