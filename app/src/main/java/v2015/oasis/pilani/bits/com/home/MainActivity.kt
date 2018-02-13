package v2015.oasis.pilani.bits.com.home

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.drawer_container.*
import v2015.oasis.pilani.bits.com.home.GlobalData.tinyDb
import v2015.oasis.pilani.bits.com.home.databinding.ActivityMainBinding
import v2015.oasis.pilani.bits.com.home.events.EventFragment
import v2015.oasis.pilani.bits.com.home.events.IfOpenThenCloseListener
import v2015.oasis.pilani.bits.com.home.myaccount.MyAccountFragment
import v2015.oasis.pilani.bits.com.home.myaccount.ProfileFragment
import v2015.oasis.pilani.bits.com.home.navbar_items.*
import v2015.oasis.pilani.bits.com.home.selfiecontest.SelfieContestFragment


interface tabListener{ fun onTabSelected(tabId: Int) }

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    lateinit var typefaceBold: Typeface
    lateinit var drawerLayout: DrawerLayout
    lateinit var hamburger: ImageView
    lateinit var headerText: TextView
    lateinit var filter: ImageView
    lateinit var headerBar: FrameLayout
    lateinit var backgroundImage: ImageView
    val selectedColor = Color.parseColor("#B50CFC")
    val deselectedColor = Color.parseColor("#202020")
    var mGoogleMap: GoogleMap? = null
    var mLocationRequest: LocationRequest? = null
    var mGoogleApiClient: GoogleApiClient? = null
    var mLastLocation: Location? = null
    var mCurrLocationMarker: Marker? = null
    var closeListener: IfOpenThenCloseListener? = null
    var closeListenerSelfie: IfOpenThenCloseListener? = null
    var closeListenerN2O: IfOpenThenCloseListener? = null
    private val TIME_INTERVAL = 2000 // # milliseconds, desired time passed between two back presses.
    private var mBackPressed: Long = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        GlobalData.mainActivity = this

        drawerLayout = findViewById(R.id.drawerLayout) as DrawerLayout
        hamburger = findViewById(R.id.hamburger) as ImageView
        headerText = findViewById(R.id.headerText) as TextView
        filter = findViewById(R.id.filter) as ImageView
        headerBar = findViewById(R.id.headerBar) as FrameLayout
        backgroundImage = findViewById(R.id.backgroundImage) as ImageView


        typefaceBold = Typeface.createFromAsset(assets, "fonts/bold.otf")
        binding.typeface = typefaceBold

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        Picasso.with(this).load(R.drawable.oasis_logo).fit().centerInside().into(oasisLogo)
        Picasso.with(this).load(R.drawable.nav_drawer_background).fit().into(drawerContainerBackground)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        bottomBar.selectTabWithId(R.id.tab_events)

        val listener = object : tabListener {
            override fun onTabSelected(tabId: Int) {
                when(tabId) {
                    R.id.tab_my_account -> {
                        Log.e(GlobalData.TAG, "Logged ${tinyDb.getBoolean("Logged")}")
                        if (tinyDb.getBoolean("Logged")){
                            val transaction = supportFragmentManager.beginTransaction()
                            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            transaction.replace(R.id.fragmentContainer, ProfileFragment()).commit()
                        }else{
                            val transaction = supportFragmentManager.beginTransaction()
                            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            transaction.replace(R.id.fragmentContainer, MyAccountFragment()).commit()
                        }
                    }
                    R.id.tab_selfie_contest -> {
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        transaction.replace(R.id.fragmentContainer, SelfieContestFragment()).commit()
                    }
                    R.id.tab_events -> {
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        transaction.replace(R.id.fragmentContainer, EventFragment()).commit()
                    }
                    R.id.tab_maps -> {
                        headerText.text = " MAP "
                        filter.visibility = View.INVISIBLE
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        val mapFragment = SupportMapFragment()
                        transaction.replace(R.id.fragmentContainer, mapFragment).commit()
                        mapFragment.getMapAsync(this@MainActivity)
                    }
                    R.id.tab_info -> {
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        val ongoingEvents = EventFragment()
                        ongoingEvents.arguments = Bundle()
                        transaction.replace(R.id.fragmentContainer, ongoingEvents).commit()
                    }
                }
            }
        }

        bottomBar.setOnTabSelectListener{ listener.onTabSelected(it) }
        bottomBar.setOnTabReselectListener{ listener.onTabSelected(it) }

        hamburger.setOnClickListener {
            if (drawerLayout.isDrawerOpen(Gravity.START))
                drawerLayout.closeDrawer(Gravity.START)
            else
                drawerLayout.openDrawer(Gravity.START)
        }

        val navBarItems = arrayOf(about, userProfile, contact, sponsors, developers, notifications, settings, n2o_voting, emergency)

        about.setOnClickListener {
            select(about, navBarItems)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            transaction.replace(R.id.fragmentContainer, AboutFragment()).commit()
            drawerLayout.closeDrawer(Gravity.START)
        }

        userProfile.setOnClickListener{
            select(userProfile, navBarItems)
            bottomBar.selectTabWithId(R.id.tab_my_account)
            drawerLayout.closeDrawer(Gravity.START)
        }

        contact.setOnClickListener {
            select(contact, navBarItems)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            transaction.replace(R.id.fragmentContainer, ContactFragment()).commit()
            drawerLayout.closeDrawer(Gravity.START)
        }

        sponsors.setOnClickListener {
            select(sponsors, navBarItems)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            transaction.replace(R.id.fragmentContainer, SponsorsFragment()).commit()
            drawerLayout.closeDrawer(Gravity.START)
        }

        developers.setOnClickListener {
            select(developers, navBarItems)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            transaction.replace(R.id.fragmentContainer, DevelopersFragment()).commit()
            drawerLayout.closeDrawer(Gravity.START)
        }

        notifications.setOnClickListener {
            select(notifications, navBarItems)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            transaction.replace(R.id.fragmentContainer, NotificationsFragment()).commit()
            drawerLayout.closeDrawer(Gravity.START)
        }

        settings.setOnClickListener {
            select(settings, navBarItems)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            transaction.replace(R.id.fragmentContainer, SettingsFragment()).commit()
            drawerLayout.closeDrawer(Gravity.START)
        }

        n2o_voting.setOnClickListener {
            select(n2o_voting, navBarItems)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            transaction.replace(R.id.fragmentContainer, N2OVotingFragment()).commit()
            drawerLayout.closeDrawer(Gravity.START)
        }

        emergency.setOnClickListener {
            select(emergency, navBarItems)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            transaction.replace(R.id.fragmentContainer, EmergencyFragment()).commit()
            drawerLayout.closeDrawer(Gravity.START)
        }

        if (intent.extras != null){
            if (intent.extras.containsKey("calledBy")){
                select(notifications, navBarItems)
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, NotificationsFragment()).commit()
            }
            if (intent.extras.containsKey("innerData")){
                val eventFragment = EventFragment()
                eventFragment.arguments = intent.extras
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, eventFragment).commit()
            }
        }else{
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, EventFragment()).commit()
        }


    }

    override fun onPause() {
        super.onPause()
        try {
            if (mGoogleApiClient != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        val bool = closeListener?.ifOpenThenClose() ?: false || closeListenerSelfie?.ifOpenThenClose() ?: false || closeListenerN2O?.ifOpenThenClose() ?: false
        if (bool == null || bool == false){
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
            {
                super.onBackPressed()
                return
            }
            else { Toast.makeText(getBaseContext(), "Tap back button in order to exit", Toast.LENGTH_SHORT).show(); }

            mBackPressed = System.currentTimeMillis()
        }
    }

    fun select(view: TextView, allViews: Array<TextView>){
        for (i in allViews)
            i.setTextColor(deselectedColor)

        view.setTextColor(selectedColor)
    }


    override fun onMapReady(googleMap: GoogleMap) {

        mGoogleMap = googleMap
        //mGoogleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient()
                mGoogleMap?.isMyLocationEnabled = true
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        }
        else {
            buildGoogleApiClient()
            mGoogleMap?.isMyLocationEnabled = true
        }

//        LatLng me=new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
        val gymg = LatLng(28.359211, 75.590495)
        val medc =  LatLng(28.357417, 75.591219)
        val srground = LatLng(28.365923,75.587759)
        val anc = LatLng(28.360346, 75.589632)
        val sac = LatLng(28.360710, 75.585639)
        val fd3 = LatLng(28.363988, 75.586274)
        val clocktower = LatLng(28.363906, 75.586980)
        val fd2 = LatLng(28.364059, 75.587873)
        val uco = LatLng(28.363257, 75.590715)
        val icici = LatLng(28.357139, 75.590436)
        val axis = LatLng(28.361605, 75.585046)
        val fk = LatLng(28.361076, 75.585457)
        val ltc = LatLng(28.365056, 75.590092)
        val nab = LatLng(28.362228, 75.587346)
        val swimmingPool = LatLng(28.3607699,75.5913962)


        val cameraPosition = CameraPosition.Builder().
                target(clocktower).
                tilt(60f).
                zoom(17f).
                bearing(0f).
                build()

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        googleMap.addMarker(MarkerOptions().position(anc).title("ANC").snippet("All Night Canteen"))
        googleMap.addMarker(MarkerOptions().position(sac).title("SAC").snippet("Student Activity Center"))
        googleMap.addMarker(MarkerOptions().position(fd3).title("FD3").snippet("Faculty Division-III(31xx-32xx)"))
        googleMap.addMarker(MarkerOptions().position(clocktower).title("Clock Tower").snippet("Auditorium"))
        googleMap.addMarker(MarkerOptions().position(fd2).title("FD2").snippet("Faculty Division-II(21xx-22xx)"))
        googleMap.addMarker(MarkerOptions().position(uco).title("UCO Bank ATM"))
        googleMap.addMarker(MarkerOptions().position(icici).title("ICICI ATM"))
        googleMap.addMarker(MarkerOptions().position(axis).title("AXIS Bank ATM"))
        googleMap.addMarker(MarkerOptions().position(fk).title("FoodKing").snippet("Restaurant"))
        googleMap.addMarker(MarkerOptions().position(ltc).title("LTC").snippet("Lecture Theater Complex(510x)"))
        googleMap.addMarker(MarkerOptions().position(nab).title("NAB").snippet("New Academic Block(60xx-61xx)"))
        googleMap.addMarker(MarkerOptions().position(gymg).title("GYMG").snippet("Gym Grounds"))
        googleMap.addMarker(MarkerOptions().position(medc).title("MedC").snippet("Medical Center"))
        googleMap.addMarker(MarkerOptions().position(srground).title("SR Grounds").snippet("SR Bhawan Grounds"))
        googleMap.addMarker(MarkerOptions().position(swimmingPool).title("Swimming Pool").snippet("Bits Swimming Pool"))
//        mMap.addMarker(new MarkerOptions().position(me).title("You are here!").snippet("Consider yourself located"));
//        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.isBuildingsEnabled = true
    }

    @Synchronized protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        mGoogleApiClient?.connect()
    }

    override fun onConnected(bundle: Bundle?) {
        mLocationRequest = LocationRequest()
        mLocationRequest?.setInterval(1000)
        mLocationRequest?.setFastestInterval(1000);
        mLocationRequest?.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        try {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker?.remove()
        }

        //Place current location marker
        val latLng = LatLng(location.getLatitude(), location.getLongitude())
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
        mCurrLocationMarker = mGoogleMap?.addMarker(markerOptions)

        //move map camera
        //mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11f))

    }

    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, i ->
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(this@MainActivity,
                                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                                    MY_PERMISSIONS_REQUEST_LOCATION)
                        })
                        .create()
                        .show()


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                   permissions: Array<String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient()
                        }
                        mGoogleMap?.setMyLocationEnabled(true)
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }

}
