package com.mapmitra.mapmitra.aboutUs

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mapmitra.mapmitra.R
import com.mapmitra.mapmitra.adapters.DevsAdapter
import com.mapmitra.mapmitra.models.Developers
import kotlinx.android.synthetic.main.activity_about_us.*

private const val TAG = "AboutUs"

class AboutUs : AppCompatActivity() {

    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var developers: MutableList<Developers>
    private lateinit var adapter: DevsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        developers = mutableListOf()
        adapter = DevsAdapter(this , developers)

        rvDeveloper.adapter = adapter
        rvDeveloper.layoutManager = LinearLayoutManager(this)


        firestoreDB = FirebaseFirestore.getInstance()


        val devReference = firestoreDB
            .collection("Developers")
            .orderBy("Name" , Query.Direction.DESCENDING)


        devReference.addSnapshotListener { snapshot , exception ->
            if (exception != null || snapshot == null) {
                Log.e(TAG , "Exception while Fetching Developers" , exception)
                return@addSnapshotListener
            }
            val devsList = snapshot.toObjects(Developers::class.java)
            developers.clear()
            developers.addAll(devsList)
            adapter.notifyDataSetChanged()
            for (dev in devsList) {
                Log.i(TAG , "Developer $dev, ${dev.Email} ")
            }
        }
    }
}
