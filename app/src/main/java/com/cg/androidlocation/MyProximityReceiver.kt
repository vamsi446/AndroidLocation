package com.cg.androidlocation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat

class MyProximityReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val getting_closer =
            intent?.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false)
        if (getting_closer!!)
        {

            sendNotification("Entered",context)

        }
        else
        {

            sendNotification("Exited",context)
        }


    }
    private fun sendNotification(msg: String,context: Context?) {
        Toast.makeText(context, "Location Proximity Alert!", Toast.LENGTH_SHORT).show()

        val nManager = ContextCompat.getSystemService(
            context!!,
            NotificationManager::class.java
        ) as NotificationManager
        lateinit var builder: Notification.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "test",
                "proximity",
                NotificationManager.IMPORTANCE_HIGH
            )

            nManager.createNotificationChannel(channel)
            builder = Notification.Builder(context, "test")
        } else {
            builder = Notification.Builder(context)
        }
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)


        builder.setContentTitle("Location Proximity Alert!")
        builder.setContentText(msg)
        val i = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 1, i, 0)
        val myNotification = builder.build()
        builder.setContentIntent(pi)
        nManager.notify(1, myNotification)

    }
}