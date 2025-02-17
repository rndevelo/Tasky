package com.mk.tasky.agenda.data.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.mk.tasky.R
import com.mk.tasky.core.navigation.DeepLinks
import com.mk.tasky.core.presentation.MainActivity
import com.mk.tasky.core.util.AgendaItemType

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.extras == null || context == null) return
        val extras = intent.extras!!

        val title = extras.getString(AlarmRegisterImpl.ITEM_TITLE) ?: return
        val description = extras.getString(AlarmRegisterImpl.ITEM_DESCRIPTION) ?: return
        val itemId = extras.getString(AlarmRegisterImpl.ITEM_ID) ?: return
        val itemTypeString = extras.getString(AlarmRegisterImpl.ITEM_TYPE) ?: return
        val itemType = AgendaItemType.valueOf(itemTypeString)

        val channelId = "${itemType}_id"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(itemType, context, channelId)
        }
        showNotification(context, title, description, itemId, channelId, itemType)
    }

    private fun showNotification(
        context: Context,
        title: String,
        description: String,
        itemId: String,
        channelId: String,
        itemType: AgendaItemType
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(R.drawable.tasky_logo)
            .setContentIntent(getPendingIntent(context, itemId, itemType))
            .setAutoCancel(true)
            .build()
        notificationManager.notify(itemId.hashCode(), notification)
    }

    private fun getPendingIntent(
        context: Context,
        itemId: String,
        itemType: AgendaItemType
    ): PendingIntent {
        // TODO: Handle all navigation using SealedClass so parameters aren't written over and over
        val deeplink = when (itemType) {
            AgendaItemType.EVENT -> DeepLinks.EVENT_DETAIL.replace("{id}", itemId)
            AgendaItemType.REMINDER -> DeepLinks.REMINDER_DETAIL.replace("{id}", itemId)
            AgendaItemType.TASK -> DeepLinks.TASK_DETAIL.replace("{id}", itemId)
        }.toUri()
        val intent = Intent(
            Intent.ACTION_VIEW,
            deeplink,
            context,
            MainActivity::class.java
        )
        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(1350, PendingIntent.FLAG_IMMUTABLE)
        }
    }

    private fun createNotificationChannel(
        itemType: AgendaItemType,
        context: Context,
        channelId: String
    ) {
        val channelName = "Reminder for ${itemType.text.asString(context)}"
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
