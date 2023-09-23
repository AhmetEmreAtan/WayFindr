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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.places.ItemClickListener
import com.example.wayfindr.places.LocationCalculator
import com.example.wayfindr.places.PlaceModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class FilterBottomSheetFragment : Fragment() {

    private val locationPermissionCode=123
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerViewPlaces)

    private val firestore = FirebaseFirestore.getInstance()
    private val placesCollection = firestore.collection("places")

    private var isPaidSelected: Boolean = false
    private var isFreeSelected: Boolean = false
    private var selectedCategory: Int = -1
    private var selectedDistance: Int = 0


    private val itemClickListener = object : ItemClickListener {
        override fun onItemClick(position: Int) {

        }
    }

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

        seekBarLocation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedDistance = progress
                val selectedDistanceText = "$selectedDistance km"
                textViewSelectedDistance.text = "Seçilen Mesafe: $selectedDistanceText"

                val locationCalculator = LocationCalculator()

                locationCalculator.getAllPlaceLocations()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val placeLocations = task.result // placeLocations, GeoPoint listesi olarak burada bulunacak
                            if (placeLocations != null && placeLocations.isNotEmpty()) {
                                // Tüm yer konumları placeLocations listesinde bulunuyor
                            } else {
                                // Yer konumları alınamadı
                            }
                        } else {
                            // Veriler çekilirken hata oluştu
                            Log.e("LocationCalculator", "Veri çekme hatası: ${task.exception}")
                        }
                    }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

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

            radioGroupPricing.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.radioButtonPaid -> {
                        filterPlacesByPrice("Ücretli")
                    }
                    R.id.radioButtonFree -> {
                        filterPlacesByPrice("Ücretsiz")
                    }
                    else -> {
                        filterPlacesByPrice("")
                    }
                }
            }

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
                        val latitude = location.latitude
                        val longitude = location.longitude
                        //Log.d("Konum", "Enlem: $latitude, Boylam: $longitude")

                        val selectedDistanceKm = selectedDistance.toDouble()

                    }
                }
                .addOnFailureListener { exception ->
                    // Konum alımı başarısız
                    Log.e("Konum", "Konum alınamadı: ${exception.message}")
                }
        } catch (securityException: SecurityException) {
            // İzin reddedildi
        }
    }

    private fun filterPlacesByPrice(price: String) {

        val query = placesCollection
            .whereEqualTo("placePrice", price)
            .orderBy("placeName")

        query.get()
            .addOnSuccessListener { querySnapshot ->
                val placesList = mutableListOf<PlaceModel>()

                for (document in querySnapshot.documents) {
                    val placeName = document.getString("placeName")
                    val placeDescription = document.getString("placeDescription")
                    val placeImage = document.getString("placeImage")

                    if (placeName != null && placeDescription != null && placeImage != null) {
                        val place = PlaceModel(placeName, placeDescription, placeImage)
                        placesList.add(place)
                    }
                }

                val recyclerView = requireView().findViewById<RecyclerView>(R.id.recyclerViewPlaces)
                val adapter = PlacesAdapter(placesList, itemClickListener)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "placePrice veri çekme işlemi başarısız. Hata: $exception")
            }
    }


}


