/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.apis.appwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import com.example.android.apis.R
import com.example.android.apis.appwidget.ExampleAppWidgetConfigure.Companion.deleteTitlePref
import com.example.android.apis.appwidget.ExampleAppWidgetConfigure.Companion.loadTitlePref

/**
 * A widget provider.  We have a string that we pull from a preference in order to show
 * the configuration settings and the current time when the widget was updated.  We also
 * register a BroadcastReceiver for time-changed and timezone-changed broadcasts, and
 * update then too.
 *
 *
 * See also the following files:
 *
 *  * ExampleAppWidgetConfigure.java
 *  * ExampleBroadcastReceiver.java
 *  * res/layout/appwidget_configure.xml
 *  * res/layout/appwidget_provider.xml
 *  * res/xml/appwidget_provider.xml
 *
 */
class ExampleAppWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(TAG, "onUpdate")
        // For each widget that needs an update, get the text that we should display:
        //   - Create a RemoteViews object for it
        //   - Set the text in the RemoteViews object
        //   - Tell the AppWidgetManager to show that views object for the widget.
        val numAppWidgetIds = appWidgetIds.size
        for (i in 0 until numAppWidgetIds) {
            val appWidgetId = appWidgetIds[i]
            val titlePrefix = loadTitlePref(context, appWidgetId)
            updateAppWidget(context, appWidgetManager, appWidgetId, titlePrefix)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        Log.d(TAG, "onDeleted")
        // When the user deletes the widget, delete the preference associated with it.
        val numAppWidgetIds = appWidgetIds.size
        for (i in 0 until numAppWidgetIds) {
            deleteTitlePref(context, appWidgetIds[i])
        }
    }

    override fun onEnabled(context: Context) {
        Log.d(TAG, "onEnabled")
        // When the first widget is created, register for the TIMEZONE_CHANGED and TIME_CHANGED
        // broadcasts.  We don't want to be listening for these if nobody has our widget active.
        // This setting is sticky across reboots, but that doesn't matter, because this will
        // be called after boot if there is a widget instance for this provider.
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
                ComponentName("com.example.android.apis", ".appwidget.ExampleBroadcastReceiver"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP)
    }

    override fun onDisabled(context: Context) {
        // When the first widget is created, stop listening for the TIMEZONE_CHANGED and
        // TIME_CHANGED broadcasts.
        Log.d(TAG, "onDisabled")
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
                ComponentName("com.example.android.apis", ".appwidget.ExampleBroadcastReceiver"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP)
    }

    companion object {
        // log tag
        private const val TAG = "ExampleAppWidgetProvide"
        @JvmStatic
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                            appWidgetId: Int, titlePrefix: String) {
            Log.d(TAG, "updateAppWidget appWidgetId=$appWidgetId titlePrefix=$titlePrefix")
            // Getting the string this way allows the string to be localized.  The format
            // string is filled in using java.util.Formatter-style format strings.
            val text: CharSequence = context.getString(R.string.appwidget_text_format,
                    loadTitlePref(context, appWidgetId),
                    "0x" + java.lang.Long.toHexString(SystemClock.elapsedRealtime()))

            // Construct the RemoteViews object.  It takes the package name (in our case, it's our
            // package, but it needs this because on the other side it's the widget host inflating
            // the layout from our package).
            val views = RemoteViews(context.packageName, R.layout.appwidget_provider)
            views.setTextViewText(R.id.appwidget_text, text)

            // Tell the widget manager
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}