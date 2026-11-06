package dev.tom.cascade.ui

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WidgetArea(host: AppWidgetHost, ids: List<Int>, onStale: (Int) -> Unit) {
    val context = LocalContext.current
    val manager = remember { AppWidgetManager.getInstance(context) }
    LaunchedEffect(ids) {
        ids.filter { manager.getAppWidgetInfo(it) == null }.forEach(onStale)
    }
    if (ids.isEmpty()) return
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        ids.forEach { id ->
            val info = manager.getAppWidgetInfo(id) ?: return@forEach
            androidx.compose.runtime.key(id) {
                AndroidView(
                    factory = { host.createView(context, id, info) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
