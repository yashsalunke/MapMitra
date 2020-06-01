package com.mapmitra.mapmitra.activities

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.ads.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationSettingsRequest.Builder
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.mapmitra.mapmitra.R
import com.mapmitra.mapmitra.directionhelpers.FetchURL
import com.mapmitra.mapmitra.directionhelpers.TaskLoadedCallback
import com.mapmitra.mapmitra.models.Obstacles
import com.mapmitra.mapmitra.utils.ConnectionManager
import kotlinx.android.synthetic.main.content_map.*
import java.io.IOException
import java.util.*
import kotlin.math.abs


class MapsActivity : FragmentActivity() , OnMapReadyCallback , TaskLoadedCallback {


    private var adView : AdView? = null
    private var mMap : GoogleMap? = null
    private var mFusedLocationProviderClient : FusedLocationProviderClient? = null
    private var placesClient : PlacesClient? = null
    var mLastKnownLocation : Location? = null
    private var mapView : View? = null
    private val firestoreDB = FirebaseFirestore.getInstance()
    private var currentPolyline : Polyline? = null
    private var searchView : SearchView? = null
    private var mapFragment : SupportMapFragment? = null
    private var requestPH = 0
    private var requestSP = 0
    private lateinit var notificationManager : NotificationManager
    private lateinit var notificationChannel : NotificationChannel
    private lateinit var builder : Notification.Builder
    private val channelId = "com.mapmitra.mapmitra"
    private var previousMenuItem : MenuItem? = null

    private lateinit var mAdView : AdView
    private val mAppUnitId : String by lazy {

        "ca-app-pub-2841460454128299~8433974004"
    }

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        if (! ConnectionManager().checkConnectivity(this)) {
            val dialog = AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("No Internet Connection Found.\n Please Connect to the Internet First.")
                .setPositiveButton("Ok") { _ , _ -> }
                .setNegativeButton("Cancel") { _ , _ -> }
            dialog.create()
            dialog.show()
        }



        mAdView = findViewById(R.id.adView)

        initializeBannerAd(mAppUnitId)

        loadBannerAd()


        btnDirection.isEnabled = false
        btnDirection.visibility = View.GONE

        adView = AdView(this)
        adView !!.adSize = AdSize.BANNER
        adView !!.adUnitId = "ca-app-pub-2841460454128299/7554655538"


        MobileAds.initialize(this) {}
        adView = findViewById(R.id.adView)
        val adRequest =
            AdRequest.Builder().build()
        adView !!.loadAd(adRequest)

        adView !!.adListener = object : AdListener() {
            override fun onAdLoaded() {
                //will be executed when ad will be fully Loaded
            }
        }


        val bottomNavigation : BottomNavigationView = findViewById(R.id.btm_nav)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            onBottomNavigationClicked(item)

        }


        searchView = findViewById(R.id.sv_location)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment !!.getMapAsync(this)
        mapView = mapFragment !!.view
        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@MapsActivity)
        Places.initialize(this@MapsActivity , getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
        AutocompleteSessionToken.newInstance()
        if (ActivityCompat.checkSelfPermission(
                this@MapsActivity ,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            Toast.makeText(this , "Request Not Found" , Toast.LENGTH_LONG).show()
        }


    }

    private fun initializeBannerAd(appUnitId : String) {

        MobileAds.initialize(this , appUnitId)

    }

    private fun loadBannerAd() {

        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    override fun onActivityResult(requestCode : Int , resultCode : Int , data : Intent?) {
        super.onActivityResult(requestCode , resultCode , data)
        if (requestCode == 51 && resultCode == Activity.RESULT_OK) {
            getCurrentLocation()
        }
    }

    override fun onMapReady(googleMap : GoogleMap) {
        mMap = googleMap
        mMap !!.isMyLocationEnabled = true
        mMap !!.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap !!.isTrafficEnabled = true
        mMap !!.uiSettings.isMyLocationButtonEnabled = false

        title = "MapMitra"

        btnDirection.isEnabled = false

        if (mapView != null && mapView !!.findViewById<View?>("1".toInt()) != null) {
            val locationButton =
                (mapView !!.findViewById<View>("1".toInt()).parent as View).findViewById<View>("2".toInt())
            val layoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP , 0)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM , RelativeLayout.TRUE)
            layoutParams.setMargins(0 , 0 , 40 , 40)


        }

        // check if GPS is enabled or not & then request user to enable it
        val locationRequest : LocationRequest = LocationRequest.create()
        locationRequest.interval = 1
        locationRequest.fastestInterval = 0
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder : Builder = Builder().addLocationRequest(locationRequest)
        val settingsClient : SettingsClient? = LocationServices.getSettingsClient(this@MapsActivity)
        val task : Task<LocationSettingsResponse> =
            settingsClient !!.checkLocationSettings(builder.build())
        task.addOnSuccessListener(this@MapsActivity) { getCurrentLocation() }
        task.addOnFailureListener(this@MapsActivity) { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(this@MapsActivity , 51)
                } catch (e1 : SendIntentException) {
                    e1.printStackTrace()
                }
            }
        }




        searchView !!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query : String) : Boolean {
                val location = searchView !!.query.toString()
                var addressList : List<Address>? = null
                val geocoder = Geocoder(this@MapsActivity)
                try {
                    addressList = geocoder.getFromLocationName(location , 1)
                } catch (e : IOException) {
                    e.printStackTrace()
                }
                try {
                    val address = addressList !![0]

                    val latLng =
                        LatLng(
                            address.latitude ,
                            address.longitude
                        )
                    mMap !!.addMarker(MarkerOptions().position(latLng).title(location))
                    mMap !!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng , 14f))

                    btnDirection.visibility = View.VISIBLE
                    btnDirection.isEnabled = true
                    btnDirection.setOnClickListener {
                        mFusedLocationProviderClient !!.lastLocation
                            .addOnSuccessListener { location ->
                                if (location != null) {
                                    var dest = LatLng(location.latitude , location.longitude)
                                    FetchURL(this@MapsActivity).execute(
                                        getUrl(
                                            dest ,
                                            latLng ,
                                            "driving"
                                        ) , "driving"
                                    )
                                    Toast.makeText(
                                        this@MapsActivity ,
                                        "Currently Not Available" ,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }


                } catch (e : IndexOutOfBoundsException) {
                    e.printStackTrace()
                    Toast.makeText(this@MapsActivity , "Place Not Found" , Toast.LENGTH_LONG).show()

                }

                return false
            }

            override fun onQueryTextChange(newText : String) : Boolean {
                return false
            }
        })
        btnDirection.isEnabled = false
        btnDirection.visibility = View.GONE
        getCurrentLocation()
        callObstacles()
        callChecker()

    }

    private fun onBottomNavigationClicked(item : MenuItem) : Boolean {

        if (previousMenuItem != null) {
            previousMenuItem?.isChecked = false
        }

        item.isCheckable = true
        item.isChecked = true
        previousMenuItem = item


        when (item.itemId) {
            R.id.addMarker -> {
                val builder = AlertDialog.Builder(this@MapsActivity)
                builder.setTitle("Choose Type")
                    .setMessage("Which type of Obstacle do you want to add. ")
                    .setNegativeButton("SpeedBreaker") { _ , _ ->
                        addObstacle("SPeedBreaker")
                    }
                    .setPositiveButton("Pothole") { _ , _ ->
                        addObstacle("Pothole")
                    }
                    .show()
                item.isChecked = true
            }

            R.id.myLocation -> {
                getCurrentLocation()
                item.isChecked = true
            }

            R.id.profile -> {
                startActivity(Intent(this@MapsActivity , ProfileActivity::class.java))
                item.isChecked = true
            }


        }
        return true

    }

    private fun callObstacles() {
        showObstacles("Pothole")
        showObstacles("SpeedBreaker")

    }

    private fun showObstacles(type : String) {

        var collection = if (type == "Pothole") "Potholes" else "SpeedBreakers"

        firestoreDB.collection(collection)
            .addSnapshotListener { snapshot , exception ->
                if (exception != null || snapshot == null) {
                    Log.e("MapsActivity" , "Exception while fetching $collection" , exception)
                    return@addSnapshotListener
                }

                val obstaclesList = snapshot.toObjects(Obstacles::class.java)
                for (obstacle in obstaclesList) {
                    var position : LatLng = LatLng(obstacle.latitude , obstacle.longitude)
                    var title = getAddress(position.latitude , position.longitude)
                    if (type == "Pothole") {
                        mMap !!.addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(title)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

                        )
                    } else {
                        mMap !!.addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(title)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        )
                    }
                }
            }

    }

    private fun getAddress(lat : Double , lng : Double) : String? {
        var add : String? = null
        val geocoder = Geocoder(this@MapsActivity , Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(lat , lng , 1)
            val obj = addresses[0]

            add = obj.getAddressLine(0)
            Log.v("IGA" , "$add")


        } catch (e : IOException) {

            e.printStackTrace()
            Toast.makeText(this , e.message , Toast.LENGTH_SHORT).show()
        }
        return add
    }

    private fun callChecker() {
        checkObstacle("Pothole")
        checkObstacle("SpeedBreaker")
    }

    private fun checkObstacle(type : String) {
        mFusedLocationProviderClient !!.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    var lat = location.latitude
                    var lon = location.longitude

                    var collection = if (type == "Pothole") "Potholes" else "SpeedBreakers"

                    firestoreDB.collection(collection)
                        .addSnapshotListener { snapshot , exception ->
                            if (exception != null || snapshot == null) {
                                Log.e(
                                    "MapsActivity" ,
                                    "Exception while fetching $collection" ,
                                    exception
                                )
                                return@addSnapshotListener
                            }

                            val obstaclesList = snapshot.toObjects(Obstacles::class.java)

                            for (obstacle in obstaclesList) {
                                if ((abs(obstacle.latitude - lat) < 0.000250) && abs(obstacle.longitude - lon) < 0.000250) {
                                    Toast.makeText(
                                        this ,
                                        "Obstacle Ahead\n Be Alert." ,
                                        Toast.LENGTH_LONG
                                    ).show()
                                    addNotification(type)

                                    val builder = AlertDialog.Builder(this@MapsActivity)
                                    builder.setTitle("Obstacle Ahead!!")
                                        .setMessage("Be Alert!! $type is Ahead!! ")
                                        .setPositiveButton("OK") { _ , _ -> }
                                        .show()

                                    val v : Vibrator =
                                        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        v.vibrate(
                                            VibrationEffect.createOneShot(
                                                2000 ,
                                                VibrationEffect.DEFAULT_AMPLITUDE
                                            )
                                        )
                                    } else {
                                        //deprecated in API 26
                                        v.vibrate(2000)
                                    }
                                }
                            }

                        }

                }
            }
    }

    private fun addNotification(type : String) {
        val intent = Intent(this , MapsActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this , 0 , intent , PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel =
                NotificationChannel(channelId , "$type Ahead" , NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) ,
                null
            )
            notificationChannel.enableVibration(true)
            notificationChannel.lockscreenVisibility
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this , channelId)
                .setContentTitle("$type Ahead")
                .setContentText("Be alert! $type is Ahead.")
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        this.resources ,
                        R.drawable.ic_launcher
                    )
                )
                .setContentIntent(pendingIntent)
        } else {
            builder = Notification.Builder(this)
                .setContentTitle("$type Ahead")
                .setContentText("Be alert! $type is Ahead.")
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        this.resources ,
                        R.drawable.ic_launcher
                    )
                )
                .setContentIntent(pendingIntent)
        }

        notificationManager.notify(1234 , builder.build())
    }

    private fun addObstacle(type : String) {

        var request = if (type == "SpeedBreaker") requestSP else requestPH
        var collection = if (type == "SpeedBreaker") "SpeedBreakers" else "Potholes"

        getCurrentLocation()
        var lat = mLastKnownLocation !!.latitude
        var lon = mLastKnownLocation !!.longitude
        var obstacle = Obstacles(lat , lon)

        when {
            request < 10 -> {
                request += 1
                Toast.makeText(this , "Request sent Successfully" , Toast.LENGTH_SHORT).show()
            }
            request == 10 -> {
                firestoreDB.collection(collection).add(obstacle)
                    .addOnSuccessListener {
                        Toast.makeText(this , "Marker Added Successfully. " , Toast.LENGTH_SHORT)
                            .show()
                        request = 0
                    }.addOnFailureListener {
                        Toast.makeText(
                            this ,
                            "Problem Adding Marker, Try Again Later" ,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            else -> {
                Toast.makeText(this , "Marker Already Added. " , Toast.LENGTH_SHORT).show()
            }
        }

        if (type == "SpeedBreaker") {
            requestSP = request
        } else {
            requestPH = request
        }
    }

    private fun getCurrentLocation() {
        mFusedLocationProviderClient !!.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    mapFragment !!.getMapAsync {
                        mLastKnownLocation = location
                        mMap !!.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    mLastKnownLocation !!.latitude ,
                                    mLastKnownLocation !!.longitude
                                ) , 20f
                            )
                        )
                    }
                }
            }
    }

    private fun getUrl(origin : LatLng , dest : LatLng , directionMode : String) : String? {
        // Origin of route
        val strOrigin = "origin=" + origin.latitude + "," + origin.longitude
        // Destination of route
        val strDest = "destination=" + dest.latitude + "," + dest.longitude
        // Mode
        val mode = "mode=$directionMode"
        // Building the parameters to the web service
        val parameters = "$strOrigin&$strDest&$mode"
        // Output format
        val output = "json"
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=" + getString(
            R.string.google_maps_key
        )
    }

    override fun onTaskDone(vararg values : Any?) {
        if (currentPolyline != null) currentPolyline?.remove()
        currentPolyline = mMap !!.addPolyline(values[0] as PolylineOptions?)
    }




}



