package com.mapmitra.mapmitra.activities


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.mapmitra.mapmitra.R
import com.mapmitra.mapmitra.login.LoginActivity


class StartActivity : AppCompatActivity() {

    private val TAG = "StartActivity"

    private var adView : AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)



        adView = AdView(this)
        adView!!.adSize = AdSize.BANNER
        adView!!.adUnitId = "ca-app-pub-2841460454128299/4954087511"




        MobileAds.initialize(this){}
        adView = findViewById(R.id.adView)
        val adRequest =
            AdRequest.Builder().build()
        adView!!.loadAd(adRequest)

        adView!!.adListener = object: AdListener() {
            override fun onAdLoaded() {

            }
        }

        //Code to start timer and take action after the timer ends
        Handler().postDelayed({ //Do any action here. Now we are moving to next page
            val mySuperIntent = Intent(this@StartActivity , LoginActivity::class.java)
            startActivity(mySuperIntent)

            //This 'finish()' is for exiting the app when back button pressed from Home page which is ActivityHome
            finish()
        } , 3000)
    }
}