package com.cg.androidlocation

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.MbmsGroupCallSession
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

var MY_PROXIMITY="com.cg.androidlocation.proximity"
class MainActivity : AppCompatActivity(),LocationListener {
    lateinit var lManager:LocationManager
    lateinit var locT:TextView
    var currentLoc:Location?=null
    var receiver=MyReceiver()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions()
        locT=findViewById(R.id.tv)

        var intentFilter=IntentFilter(MY_PROXIMITY)
        registerReceiver(receiver, intentFilter)
        //get the reference of the LocationManager


        lManager=getSystemService(LOCATION_SERVICE) as LocationManager

        //Location providers - select one
        val providerList=lManager.getProviders(true)
        var providerName=""
        if(providerList.isNotEmpty())
        {
            if(providerList.contains(LocationManager.GPS_PROVIDER))
            {
                providerName=LocationManager.GPS_PROVIDER
            }
            else if(providerList.contains(LocationManager.NETWORK_PROVIDER))
            {
                providerName=LocationManager.NETWORK_PROVIDER
            }
            else
            {
                providerName=providerList[0]
            }
            Toast.makeText(this, "Provider name: $providerName", Toast.LENGTH_SHORT).show()
            Log.d("MainActivity", "Provider: $providerName")

            checkPermissions()


            val loc=lManager.getLastKnownLocation(providerName)
            if(loc!=null)
            {
                updateLocation(loc)

            }
            else
            {
                Toast.makeText(this, "No Location Found", Toast.LENGTH_LONG).show()
            }
            //register location listener
            val time:Long=1000
            val distance:Float=10.0F

            lManager.requestLocationUpdates(providerName, time, distance, this)
            val radius:Float=20000.0f
            val expiration:Long=20000L
            //val bIntent=Intent(this,MyProximityReceiver::class.java)
            val bIntent=Intent(MY_PROXIMITY)
            val pi=PendingIntent.getBroadcast(this, 0, bIntent, 0)
            val myLati:Double=16.5449
            val myLongi:Double=81.5212
            lManager.addProximityAlert(myLati, myLongi, radius, -1, pi)


        }
        else
        {
            Toast.makeText(this, "Pls enable Location", Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateLocation(loc: Location) {
        val latt=loc.latitude
        val longi=loc.longitude
        var distance:Float=0f
        if(currentLoc!=null)
        {
           distance=currentLoc?.distanceTo(loc)!!

        }
        currentLoc=loc

        locT.append("Lattitude : $latt, Longitude: $longi, distance : $distance m\n")
        locT.textAlignment= View.TEXT_ALIGNMENT_CENTER

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add("show on map")
        menu?.add("address")

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.title=="address")
        {
            val address=getAddress()
            Toast.makeText(
                this, "address: $address",
                Toast.LENGTH_SHORT
            ).show()

        }
        else
        {
            val mapIntent=Intent(
                Intent.ACTION_VIEW,
                Uri.parse("geo:${currentLoc?.latitude}, ${currentLoc?.longitude}")
            )
            startActivity(mapIntent)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getAddress(): String {
        val gCoder=Geocoder(this)

        val addressList=gCoder.getFromLocation(currentLoc?.latitude!!, currentLoc?.longitude!!, 10)
        if(addressList.isNotEmpty())
        {
            val addr=addressList[0]
            var strAddress=""
            for(i in 0..addr.maxAddressLineIndex)
            {
                strAddress+=addr.getAddressLine(i)
            }
            return strAddress

        }

        return ""
    }

    fun checkPermissions()
    {
        //min SDK 23
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )!=PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1
            )
        }
        else
        {
            Toast.makeText(this, "Location permission Granted", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(grantResults.isNotEmpty())
        {
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED &&
                    grantResults[1]==PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
            }
            else
            {
                finish()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onLocationChanged(newLoc: Location) {
        updateLocation(newLoc)
    }

    override fun onDestroy() {
        super.onDestroy()
        lManager.removeUpdates(this)
       // unregisterReceiver(receiver)
    }
}
class MyReceiver:BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("onReceive","Entered OnReceive")

        val getting_closer=intent?.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false)
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