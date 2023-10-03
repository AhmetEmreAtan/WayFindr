package com.example.wayfindr

import PlacesAdapter
import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.places.ItemClickListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class FilterBottomSheetFragment : BottomSheetDialogFragment() {

    private val locationPermissionCode=123
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0

    val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerViewPlaces)

    private val firestore = FirebaseFirestore.getInstance()
    private val placesCollection = firestore.collection("places")

    private var isPaidSelected: Boolean = false
    private var isFreeSelected: Boolean = false
    private var selectedCategory: Int = -1
    private var selectedDistance: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_filter_bottom_sheet_fragment, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        return view

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val seekBarLocation: SeekBar = view.findViewById(R.id.seekBarLocation)
        val textViewSelectedDistance: TextView = view.findViewById(R.id.textViewSelectedDistance)
        val resetButton: Button? = view.findViewById(R.id.resetButton)
        val filterButton: Button? = view.findViewById(R.id.filterButton)
        val radioGroupPricing: RadioGroup = view.findViewById(R.id.radioGroupPricing)
        val cardView1: CardView = view.findViewById((R.id.cardview1))
        val cardView2: CardView = view.findViewById((R.id.cardview2))
        val cardView3: CardView = view.findViewById((R.id.cardview3))
        val cardView4: CardView = view.findViewById((R.id.cardview4))
        val cardView5: CardView = view.findViewById((R.id.cardview5))

        val cardViews = arrayOf(cardView1, cardView2, cardView3, cardView4, cardView5)

        for (i in cardViews.indices) {
            val cardView = cardViews[i]

            cardView.setOnClickListener {
                if (selectedCategory != i) {
                    selectedCategory = i
                } else {
                    selectedCategory = -1
                }

                for (j in cardViews.indices) {
                    val card = cardViews[j]
                    card.setCardBackgroundColor(
                        if (selectedCategory == j) resources.getColor(R.color.vintage)
                        else Color.WHITE
                    )
                }
            }
        }



        radioGroupPricing.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioButtonPaid -> {
                    isPaidSelected = true
                    isFreeSelected = false
                }
                R.id.radioButtonFree -> {
                    isPaidSelected = false
                    isFreeSelected = true
                }
                else -> {
                    isPaidSelected = false
                    isFreeSelected = false
                }
            }
        }

        resetButton?.setOnClickListener {
            radioGroupPricing.clearCheck()
            seekBarLocation.progress = 0
            textViewSelectedDistance.text = "Seçilen Mesafe: 0 km"
            if (selectedCategory != -1) {
                cardViews[selectedCategory].setCardBackgroundColor(Color.WHITE)
                selectedCategory = -1
            }

        }

        filterButton?.setOnClickListener{
            seekBarLocation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    selectedDistance = progress
                    val selectedDistanceText = "$selectedDistance km"
                    textViewSelectedDistance.text = "Seçilen Mesafe: $selectedDistanceText"

                    fetchDataLocationFromFirestore(selectedDistance)

                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        val closeButton: ImageView = view.findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.d(TAG, "Back button pressed ")
                    if (isEnabled) {
                        isEnabled = false
                        activity?.supportFragmentManager?.beginTransaction()
                            ?.remove(this@FilterBottomSheetFragment)?.commit()
                    }
                }
            })

        if (hasLocationPermission()) {
            getLocation()
        } else {
            requestLocationPermission()
        }




    }

    // Kullanıcının konum izni olduğunu kontrol edin
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Kullanıcının konum iznini isteyin
    private fun requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            val alertDialog = AlertDialog.Builder(requireContext())
                .setTitle("Konum İzni Gerekli")
                .setMessage("Uygulamamızın konum bilgilerine erişmesi gerekiyor. Bu, yakınınızdaki yerleri bulmamıza yardımcı olur.")
                .setPositiveButton("İzin Ver") { dialog, which ->
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        locationPermissionCode
                    )
                }
                .setNegativeButton("Reddet") { dialog, which ->
                    // İzin reddedildiğinde veya iptal edildiğinde gereken işlemler
                }
                .create()

            alertDialog.show()
        } else {

            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            locationPermissionCode
        )
    }

    // İzin isteği sonucu
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildi
                getLocation()
            } else {
                Snackbar.make(requireView(), "Konum izni reddedildi, bu nedenle belirli özellikler kullanılamayabilir.", Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }

    // Kullanıcının konumunu alma
    private fun getLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        userLatitude = location.latitude
                        userLongitude = location.longitude

                        //Log.d("Konum", "Enlem: $latitude, Boylam: $longitude")

                    }
                }
                .addOnFailureListener { exception ->

                    Log.e("Konum", "Konum alınamadı: ${exception.message}")
                }
        } catch (securityException: SecurityException) {
            // İzin reddedildi
        }
    }

    private fun fetchDataLocationFromFirestore(selectedDistance: Int) {

        val db = FirebaseFirestore.getInstance()
        val placesCollection = db.collection("places")

        val query = placesCollection.orderBy("placeName")

        query.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val placeLocation = document.getString("placeLocation")

                        if (placeLocation != null) {
                            val latLng = parseLatLngFromGoogleMapsUrl(placeLocation)
                            if (latLng != null) {
                                val (latitude, longitude) = latLng

                                // İki konum arasındaki mesafe
                                val distanceInKilometers = calculateDistance(
                                    userLatitude,
                                    userLongitude,
                                    latitude,
                                    longitude
                                )

                                val distanceInKilometersInt = distanceInKilometers.toInt()

                                Log.d("Mesafe: ", "$distanceInKilometersInt km")


                                if (distanceInKilometersInt <= selectedDistance) {

                                    Log.d("Uygun Mekan: ", "Mekan adı: ${document.getString("placeName")}")
                                }
                            }
                        }
                    }
                } else {
                    Log.e("Veri Alınmadı: ", "${task.exception}")
                }
            }
    }



    fun parseLatLngFromGoogleMapsUrl(url: String): Pair<Double, Double>? {
        val regex = "(@[0-9.]+,[0-9.]+)".toRegex()
        val matchResult = regex.find(url)

        if (matchResult != null) {
            val matchValue = matchResult.value.substring(1)
            val parts = matchValue.split(",")
            if (parts.size == 2) {
                val latitude = parts[0].toDoubleOrNull()
                val longitude = parts[1].toDoubleOrNull()
                if (latitude != null && longitude != null) {
                    return Pair(latitude, longitude)
                }
            }
        }

        return null
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = R * c
        return distance
    }

}


