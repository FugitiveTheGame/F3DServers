package com.darkrockstudios.apps.f3dservers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.coroutineScope

class ServerCheckWorker(private val context: Context, params: WorkerParameters)
	: CoroutineWorker(context, params)
{
	companion object
	{
		const val CHANNEL_ID = "SERVER_CHECK"
	}

	override suspend fun doWork(): Result = coroutineScope {
		val serverResult = API.getServers()
		if(serverResult != null && serverResult.isNotEmpty())
		{
			val missingServers = API.missingOfficialServers(serverResult)
			if(missingServers.isNotEmpty())
			{
				postNotification(missingServers)
			}
			Result.success()
		}
		else
		{
			Result.failure()
		}
	}

	private fun createChannel()
	{
		val name = context.getString(R.string.notification_channel_title)
		val importance = NotificationManager.IMPORTANCE_HIGH
		val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
			description = context.getString(R.string.notification_channel_description)
		}

		with(NotificationManagerCompat.from(context)) {
			createNotificationChannel(channel)
		}
	}

	private fun postNotification(missingServers: List<String>)
	{
		createChannel()

		with(NotificationManagerCompat.from(context)) {
			val intent = Intent(context, ServersActivity::class.java).apply {
				flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
			}
			val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

			val title = context.getString(R.string.notification_title)

			var downServers = ""
			missingServers.forEach { server -> downServers += server + "\n" }

			val description = context.getString(R.string.notification_description, missingServers.size, downServers)

			val builder = NotificationCompat.Builder(context, CHANNEL_ID)
					.setSmallIcon(R.drawable.ic_error)
					.setContentTitle(title)
					.setContentText(description)
					.setPriority(NotificationCompat.PRIORITY_HIGH)
					.setContentIntent(pendingIntent)
					.setStyle(
							NotificationCompat.BigTextStyle()
									.bigText(description))
					.setAutoCancel(true)

			notify(1, builder.build())
		}
	}
}
