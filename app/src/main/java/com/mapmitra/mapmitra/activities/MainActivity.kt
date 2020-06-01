package com.mapmitra.mapmitra.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter.withActivity
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mapmitra.mapmitra.R

class MainActivity : AppCompatActivity() {
    private var btnGrant: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(
                this@MainActivity ,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startActivity(Intent(this@MainActivity , MapsActivity::class.java))
            finish()
            return
        }
        btnGrant = findViewById(R.id.btn_grant)
        btnGrant?.setOnClickListener(View.OnClickListener {
            withActivity(this@MainActivity)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        startActivity(Intent(this@MainActivity , MapsActivity::class.java))
                        finish()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        if (response.isPermanentlyDenied) {
                            val builder = AlertDialog.Builder(this@MainActivity)
                            builder.setTitle("Permission Denied")
                                .setMessage("Permission to access loaction is permanently denied. You need to go to settings to allow the permission.")
                                .setNegativeButton("Cancel" , null)
                                .setPositiveButton("OK") { _ , _ ->
                                    val intent = Intent()
                                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    intent.data = Uri.fromParts("package" , packageName , null)
                                }
                                .show()
                        } else {
                            Toast.makeText(
                                this@MainActivity ,
                                "Permission Denied" ,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest ,
                        token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                })
                .check()
        })
    }
}